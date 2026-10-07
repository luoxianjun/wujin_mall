package cn.iocoder.yudao.module.forum.service.search;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.controller.app.search.vo.AppPostSearchReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.elasticsearch.post.ForumPostESDO;
import cn.iocoder.yudao.module.forum.dal.elasticsearch.post.ForumPostRepository;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import cn.iocoder.yudao.module.forum.enums.post.PostStatusEnum;
import cn.iocoder.yudao.module.forum.service.post.ForumPostService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ForumSearchServiceImpl implements ForumSearchService {

    @Resource
    private ForumPostRepository postRepository;

    @Resource
    private ForumPostService postService;

    @Resource
    private ForumPostMapper forumPostMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public PageResult<AppPostRespVO> searchPosts(AppPostSearchReqVO reqVO, Long userId) {
        // 构建查询：关键词匹配 + 状态过滤（只查询已通过的帖子）
        // 同时搜索 IK 分析器字段和 standard 分析器子字段，
        // 解决 IK 分析器过滤英文 stop words（如 "are", "the", "is"）导致英文搜索不到的问题
        BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.multiMatchQuery(reqVO.getKeyword(), "title", "content"))
                .should(QueryBuilders.multiMatchQuery(reqVO.getKeyword(), "title.standard", "content.standard"))
                .minimumShouldMatch(1);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(keywordQuery)
                .filter(QueryBuilders.termQuery("status", PostStatusEnum.APPROVED.getStatus()));

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(reqVO.getPageNo() - 1, reqVO.getPageSize()))
                .build();

        SearchHits<ForumPostESDO> searchHits = elasticsearchRestTemplate.search(query, ForumPostESDO.class);

        List<Long> postIds = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(ForumPostESDO::getId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return PageResult.empty();
        }

        // 从 DB 获取完整信息（复用 PostService 的逻辑，保证数据一致性和完整性，如点赞状态等）
        // 这里需要注意，如果 ES 和 DB 数据不一致，可能会有问题。
        // 但通常搜索只返回 ID，详情查 DB 是比较稳妥的做法，尤其是涉及到动态数据（点赞数、是否点赞等）。
        // 不过 PostService 没有批量获取 VO 的接口，可能需要新增或者循环调用。
        // 为了性能，最好是批量查询 DO，然后批量转换 VO。
        // 但 PostService.getPostPage 是查 DB 分页。
        // 这里我们可以直接调用 postService.getPost(id, userId) 循环，或者在 PostService 加一个
        // batchGetPosts。
        // 考虑到性能，循环调用 getPost 可能会有 N+1 问题（虽然 getPost 内部也是单查）。
        // 暂时先循环调用，如果性能有问题再优化。

        List<AppPostRespVO> list = postIds.stream()
                .map(id -> {
                    try {
                        return postService.getPost(id, userId);
                    } catch (Exception e) {
                        // 忽略不存在的帖子（可能 ES 有但 DB 删了）
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResult<>(list, searchHits.getTotalHits());
    }

    @Override
    public int reindexAllPosts() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("forum_post");

        // 1. 删除旧索引
        if (elasticsearchRestTemplate.indexOps(indexCoordinates).exists()) {
            elasticsearchRestTemplate.indexOps(indexCoordinates).delete();
            log.info("[reindexAllPosts] 已删除旧索引 forum_post");
        }

        // 2. 根据 ForumPostESDO 的 @Document/@MultiField 注解重新创建索引和 mapping
        elasticsearchRestTemplate.indexOps(indexCoordinates).create();
        elasticsearchRestTemplate.indexOps(indexCoordinates).putMapping(
                elasticsearchRestTemplate.indexOps(indexCoordinates).createMapping(ForumPostESDO.class)
        );
        log.info("[reindexAllPosts] 已重新创建索引和 mapping");

        // 3. 从 DB 查询所有帖子，批量写入 ES
        List<ForumPostDO> allPosts = forumPostMapper.selectList();
        int count = 0;
        for (ForumPostDO post : allPosts) {
            try {
                ForumPostESDO esDO = new ForumPostESDO();
                esDO.setId(post.getId());
                esDO.setTitle(post.getTitle());
                esDO.setContent(post.getContent());
                esDO.setUserId(post.getUserId());
                esDO.setStatus(post.getStatus());
                esDO.setCreateTime(post.getCreateTime());
                esDO.setCategory(post.getCategory());
                esDO.setCategories(post.getCategories());
                postRepository.save(esDO);
                count++;
            } catch (Exception e) {
                log.warn("[reindexAllPosts] 同步帖子失败，postId={}", post.getId(), e);
            }
        }
        log.info("[reindexAllPosts] 重建索引完成，共同步 {} 条帖子", count);
        return count;
    }
}

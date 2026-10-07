package cn.iocoder.yudao.module.forum.dal.elasticsearch.post;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ForumPostRepository extends ElasticsearchRepository<ForumPostESDO, Long> {
}

package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论坛帖子 Mapper
 */
@Mapper
public interface ForumPostMapper extends BaseMapperX<ForumPostDO> {

        /**
         * 分页查询帖子
         * 
         * 注意：status 过滤逻辑
         * - Web 端（管理后台）：不传 status，返回所有状态的帖子
         * - App 端（小程序）：传 status=1，只返回已通过的帖子
         * 
         * 注意：匿名过滤逻辑
         * - 如果查询的是他人主页（targetUserId != null && targetUserId != currentUserId），则过滤掉匿名帖子
         * - 如果查询的是自己的主页或浏览所有帖子，不过滤匿名帖子
         */
        default PageResult<ForumPostDO> selectPage(AppPostPageReqVO reqVO, Long currentUserId) {
                LambdaQueryWrapperX<ForumPostDO> wrapper = new LambdaQueryWrapperX<ForumPostDO>()
                                .eqIfPresent(ForumPostDO::getStatus, reqVO.getStatus())
                                .eqIfPresent(ForumPostDO::getUserId, reqVO.getUserId())
                                .eqIfPresent(ForumPostDO::getSchool, reqVO.getSchool());
                
                // 如果查询的是他人主页，过滤掉匿名帖子（忽略 reqVO.getAnonymous() 的值）
                if (reqVO.getUserId() != null && currentUserId != null && !reqVO.getUserId().equals(currentUserId)) {
                        // 查看他人主页时，强制排除匿名帖子
                        wrapper.eq(ForumPostDO::getAnonymous, false);
                } else {
                        // 其他情况，使用 reqVO 中的 anonymous 参数
                        wrapper.eqIfPresent(ForumPostDO::getAnonymous, reqVO.getAnonymous());
                }
                
                // 话题查询：判断 categories JSON 数组是否包含指定话题ID
                if (reqVO.getCategory() != null) {
                        wrapper.apply("JSON_CONTAINS(categories, CAST({0} AS JSON))", reqVO.getCategory());
                }
                
                // 关键词搜索：标题或内容包含关键词
                if (reqVO.getKeyword() != null) {
                        wrapper.and(w -> w.like(ForumPostDO::getTitle, reqVO.getKeyword())
                                        .or()
                                        .like(ForumPostDO::getContent, reqVO.getKeyword()));
                }
                
                wrapper.orderByDesc(ForumPostDO::getIsTop)
                                .orderByDesc(reqVO.getOrderBy() != null && reqVO.getOrderBy() == 1,
                                                ForumPostDO::getCreateTime) // 最新
                                .orderByDesc(reqVO.getOrderBy() != null && reqVO.getOrderBy() == 2,
                                                ForumPostDO::getLikeCount) // 热度
                                .orderByDesc(ForumPostDO::getId);

                // 时间范围筛选
                if (reqVO.getDays() != null && reqVO.getDays() > 0) {
                        LocalDateTime startTime = LocalDateTime.now().minusDays(reqVO.getDays());
                        wrapper.ge(ForumPostDO::getCreateTime, startTime);
                }

                return selectPage(reqVO, wrapper);
        }

        /**
         * 查询热点帖子
         */
        @Select("<script>"
                        + "SELECT *, "
                        + "       ((0.1 * view_count) + (3 * like_count) + (4 * comment_count) + (5 * follow_count)) "
                        + "           / POW(TIMESTAMPDIFF(HOUR, create_time, NOW()) + 2, 1.5) AS hot_score "
                        + "FROM forum_post "
                        + "WHERE deleted = 0 AND status = #{status} "
                        + "<if test=\"startTime != null\">"
                        + "    AND create_time &gt;= #{startTime} "
                        + "</if>"
                        + "ORDER BY hot_score DESC, id DESC "
                        + "LIMIT #{size}"
                        + "</script>")
        List<ForumPostDO> selectHotList(@Param("size") int size,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("status") Integer status);

        /**
         * 统计帖子浏览次数总和
         *
         * @param startTime 起始创建时间；为空则统计全部
         * @return 浏览次数
         */
        @Select("<script>"
                        + "SELECT COALESCE(SUM(view_count), 0) "
                        + "FROM forum_post "
                        + "<where>"
                        + "<if test=\"startTime != null\">"
                        + "    create_time &gt;= #{startTime} "
                        + "</if>"
                        + "</where>"
                        + "</script>")
        Long selectSumViewCount(@Param("startTime") LocalDateTime startTime);

}

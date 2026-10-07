package cn.iocoder.yudao.module.forum.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.enums.activity.ActivityStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 论坛活动 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumActivityMapper extends BaseMapperX<ForumActivityDO> {

    Logger log = LoggerFactory.getLogger(ForumActivityMapper.class);

    /**
     * 分页查询活动
     * 状态根据时间动态计算：
     * - 1-报名中：当前时间在报名时间范围内
     * - 2-进行中：当前时间在活动时间范围内
     * - 3-已结束：当前时间已过活动结束时间
     * - 4-已取消：数据库中 status = 4
     */
    default PageResult<ForumActivityDO> selectPage(AppActivityPageReqVO reqVO) {
        return selectPage(reqVO, false);
    }

    /**
     * 用户端分页查询，隐藏活动不会出现在公开列表中。
     */
    default PageResult<ForumActivityDO> selectVisiblePage(AppActivityPageReqVO reqVO) {
        return selectPage(reqVO, true);
    }

    default PageResult<ForumActivityDO> selectPage(AppActivityPageReqVO reqVO, boolean visibleOnly) {
        log.info("[selectPage] 构建查询条件 - category: {}, status: {}, keyword: {}, visibleOnly: {}",
                reqVO.getCategory(), reqVO.getStatus(), reqVO.getKeyword(), visibleOnly);

        LambdaQueryWrapperX<ForumActivityDO> wrapper = new LambdaQueryWrapperX<>();
        LocalDateTime now = LocalDateTime.now();

        if (visibleOnly) {
            wrapper.and(query -> query.isNull(ForumActivityDO::getHidden)
                    .or()
                    .eq(ForumActivityDO::getHidden, Boolean.FALSE));
        }

        // 分类过滤
        if (reqVO.getCategory() != null) {
            wrapper.eq(ForumActivityDO::getCategory, reqVO.getCategory());
        }

        // 发布者过滤
        if (reqVO.getUserId() != null) {
            wrapper.eq(ForumActivityDO::getUserId, reqVO.getUserId());
        }

        // 管理员过滤：adminMemberIds 是 JSON 数组，使用 JSON_CONTAINS 查询
        if (reqVO.getAdminMemberId() != null) {
            wrapper.apply("JSON_CONTAINS(admin_member_ids, {0})", reqVO.getAdminMemberId().toString());
        }

        // 关键词过滤
        if (reqVO.getKeyword() != null && !reqVO.getKeyword().isEmpty()) {
            wrapper.like(ForumActivityDO::getTitle, reqVO.getKeyword());
        }

        // 状态过滤，根据时间动态计算
        if (reqVO.getStatus() != null) {
            Integer status = reqVO.getStatus();
            log.info("[selectPage] 添加状态过滤条件 status={}, 当前时间={}", status, now);

            if (ActivityStatusEnum.SIGN_UP.getStatus().equals(status)) {
                wrapper.isNotNull(ForumActivityDO::getSignUpStartTime)
                        .isNotNull(ForumActivityDO::getSignUpEndTime)
                        .le(ForumActivityDO::getSignUpStartTime, now)
                        .ge(ForumActivityDO::getSignUpEndTime, now)
                        .ne(ForumActivityDO::getStatus, ActivityStatusEnum.CANCELLED.getStatus());
            } else if (ActivityStatusEnum.IN_PROGRESS.getStatus().equals(status)) {
                wrapper.isNotNull(ForumActivityDO::getStartTime)
                        .isNotNull(ForumActivityDO::getEndTime)
                        .le(ForumActivityDO::getStartTime, now)
                        .ge(ForumActivityDO::getEndTime, now)
                        .ne(ForumActivityDO::getStatus, ActivityStatusEnum.CANCELLED.getStatus());
            } else if (ActivityStatusEnum.ENDED.getStatus().equals(status)) {
                wrapper.isNotNull(ForumActivityDO::getEndTime)
                        .lt(ForumActivityDO::getEndTime, now)
                        .ne(ForumActivityDO::getStatus, ActivityStatusEnum.CANCELLED.getStatus());
            } else if (ActivityStatusEnum.CANCELLED.getStatus().equals(status)) {
                wrapper.eq(ForumActivityDO::getStatus, ActivityStatusEnum.CANCELLED.getStatus());
            }
        }

        wrapper.orderByDesc(ForumActivityDO::getCreateTime);

        log.info("[selectPage] SQL WHERE 条件: {}", wrapper.getSqlSegment());

        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询活动并加行锁，用于报名人数更新场景避免并发超额。
     */
    @Select("SELECT * FROM forum_activity WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    ForumActivityDO selectByIdForUpdate(@Param("id") Long id);

    /**
     * 根据 ID 列表查询活动（忽略逻辑删除），用于“我的报名”等场景。
     */
    @Select("<script>" +
            "SELECT * FROM forum_activity WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<ForumActivityDO> selectBatchByIdsIgnoreDeleted(@Param("ids") Collection<Long> ids);

    /**
     * 根据 ID 列表查询活动（忽略逻辑删除），安全封装，支持空集合。
     */
    default List<ForumActivityDO> selectBatchByIdsIgnoreDeletedSafe(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectBatchByIdsIgnoreDeleted(ids);
    }

    /**
     * 热门活动列表，按热度降序，其次创建时间降序。
     */
    default List<ForumActivityDO> selectHotList(int limit) {
        return selectList(new LambdaQueryWrapperX<ForumActivityDO>()
                .orderByDesc(ForumActivityDO::getHot)
                .orderByDesc(ForumActivityDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 用户端热门活动列表，隐藏活动不会出现在公开推荐位中。
     */
    default List<ForumActivityDO> selectVisibleHotList(int limit) {
        return selectList(new LambdaQueryWrapperX<ForumActivityDO>()
                .and(query -> query.isNull(ForumActivityDO::getHidden)
                        .or()
                        .eq(ForumActivityDO::getHidden, Boolean.FALSE))
                .orderByDesc(ForumActivityDO::getHot)
                .orderByDesc(ForumActivityDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 查询未来 2 小时内开始且尚未发送过通知的活动。
     */
    default List<ForumActivityDO> selectActivitiesNeedNotify(LocalDateTime now, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<ForumActivityDO>()
                .isNotNull(ForumActivityDO::getStartTime)
                .gt(ForumActivityDO::getStartTime, now)
                .le(ForumActivityDO::getStartTime, endTime)
                .eq(ForumActivityDO::getStartNotified, false)
                .ne(ForumActivityDO::getStatus, ActivityStatusEnum.CANCELLED.getStatus())
                .orderByAsc(ForumActivityDO::getStartTime));
    }

    /**
     * 更新活动的已通知状态。
     */
    default int updateStartNotified(Long activityId, Boolean notified) {
        return update(ForumActivityDO.builder().startNotified(notified).build(),
                new LambdaQueryWrapperX<ForumActivityDO>().eq(ForumActivityDO::getId, activityId));
    }

}

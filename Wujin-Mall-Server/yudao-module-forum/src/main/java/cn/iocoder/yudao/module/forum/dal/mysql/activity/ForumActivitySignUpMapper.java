package cn.iocoder.yudao.module.forum.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySignUpPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivitySignUpDO;
import cn.iocoder.yudao.module.forum.enums.activity.ApprovalStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 论坛活动报名 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumActivitySignUpMapper extends BaseMapperX<ForumActivitySignUpDO> {

    /**
     * 查询用户是否已报名活动
     */
    default ForumActivitySignUpDO selectByActivityIdAndUserId(Long activityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eq(ForumActivitySignUpDO::getActivityId, activityId)
                .eq(ForumActivitySignUpDO::getUserId, userId));
    }

    /**
     * 查询用户是否已通过审核并进入活动参与池
     */
    default ForumActivitySignUpDO selectApprovedByActivityIdAndUserId(Long activityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eq(ForumActivitySignUpDO::getActivityId, activityId)
                .eq(ForumActivitySignUpDO::getUserId, userId)
                .eq(ForumActivitySignUpDO::getApprovalStatus, ApprovalStatusEnum.APPROVED.getStatus()));
    }

    /**
     * 查询用户是否已报名活动（包含已取消的记录）
     * 用于检查是否允许重新报名
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM forum_activity_sign_up WHERE activity_id = #{activityId} AND user_id = #{userId} AND deleted = 0 LIMIT 1")
    ForumActivitySignUpDO selectByActivityIdAndUserIdIncludeCancelled(
            @org.apache.ibatis.annotations.Param("activityId") Long activityId,
            @org.apache.ibatis.annotations.Param("userId") Long userId);

    /**
     * 分页查询活动报名列表
     */
    default PageResult<ForumActivitySignUpDO> selectPage(AppActivitySignUpPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eqIfPresent(ForumActivitySignUpDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(ForumActivitySignUpDO::getApprovalStatus, reqVO.getApprovalStatus())
                .orderByDesc(ForumActivitySignUpDO::getCreateTime));
    }

    /**
     * 统计活动当前占用名额人数（待审核 + 已通过）
     */
    default Long countByActivityId(Long activityId) {
        return selectCount(new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eq(ForumActivitySignUpDO::getActivityId, activityId)
                .in(ForumActivitySignUpDO::getApprovalStatus,
                        ApprovalStatusEnum.PENDING.getStatus(),
                        ApprovalStatusEnum.APPROVED.getStatus()));
    }

    /**
     * 统计活动已审核通过人数
     */
    default Long countApprovedByActivityId(Long activityId) {
        return selectCount(new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eq(ForumActivitySignUpDO::getActivityId, activityId)
                .eq(ForumActivitySignUpDO::getApprovalStatus, ApprovalStatusEnum.APPROVED.getStatus()));
    }

    /**
     * 分页查询用户的报名列表
     */
    default PageResult<ForumActivitySignUpDO> selectPageByUserId(AppActivitySignUpPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .eq(ForumActivitySignUpDO::getUserId, userId)
                .eqIfPresent(ForumActivitySignUpDO::getApprovalStatus, reqVO.getApprovalStatus())
                .orderByDesc(ForumActivitySignUpDO::getCreateTime));
    }

    /**
     * 物理删除报名记录（避免唯一约束冲突）
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM forum_activity_sign_up WHERE id = #{id}")
    int realDeleteById(@org.apache.ibatis.annotations.Param("id") Long id);

    /**
     * 查询活动已通过审核的报名用户ID列表
     * 
     * @param activityId 活动ID
     * @return 用户ID列表
     */
    default java.util.List<Long> selectApprovedUserIdsByActivityId(Long activityId) {
        List<ForumActivitySignUpDO> list = selectList(new LambdaQueryWrapperX<ForumActivitySignUpDO>()
                .select(ForumActivitySignUpDO::getUserId)
                .eq(ForumActivitySignUpDO::getActivityId, activityId)
                .eq(ForumActivitySignUpDO::getApprovalStatus, ApprovalStatusEnum.APPROVED.getStatus()));
        return list.stream()
                .map(ForumActivitySignUpDO::getUserId)
                .collect(java.util.stream.Collectors.toList());
    }

}

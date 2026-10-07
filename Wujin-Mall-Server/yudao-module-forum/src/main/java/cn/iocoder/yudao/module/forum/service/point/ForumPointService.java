package cn.iocoder.yudao.module.forum.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordExcelVO;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.point.vo.AppPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.point.ForumPointRecordDO;
import cn.iocoder.yudao.module.forum.enums.point.ForumPointBizTypeEnum;

import java.util.List;

/**
 * 论坛积分 Service 接口
 *
 * @author forum
 */
public interface ForumPointService {

    /**
     * 增加积分
     *
     * @param userId 用户ID
     * @param point 积分数量
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    void addPoint(Long userId, Integer point, ForumPointBizTypeEnum bizType, String bizId);

    /**
     * 减少积分
     *
     * @param userId 用户ID
     * @param point 积分数量
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    void reducePoint(Long userId, Integer point, ForumPointBizTypeEnum bizType, String bizId);

    /**
     * 签到积分
     *
     * @param userId 用户ID
     * @param point 积分数量
     * @param bizId 业务ID（签到记录ID）
     */
    void addSignPoint(Long userId, Integer point, String bizId);

    /**
     * 发帖积分
     *
     * @param userId 用户ID
     * @param bizId 业务ID（帖子ID）
     */
    void addPostPoint(Long userId, String bizId);

    /**
     * 评论积分
     *
     * @param userId 用户ID
     * @param bizId 业务ID（评论ID）
     */
    void addCommentPoint(Long userId, String bizId);

    /**
     * 帖子被点赞积分
     *
     * @param userId 用户ID
     * @param bizId 业务ID（帖子ID）
     */
    void addPostLikePoint(Long userId, String bizId);

    /**
     * 活动报名积分
     *
     * @param userId 用户ID
     * @param point 奖励积分数量
     * @param bizId 业务ID（活动ID）
     */
    void addActivitySignUpPoint(Long userId, Integer point, String bizId);

    /**
     * 活动签到积分
     *
     * @param userId 用户ID
     * @param bizId 业务ID（活动ID）
     */
    void addActivityCheckInPoint(Long userId, String bizId);

    /**
     * 分页查询积分记录
     *
     * @param reqVO 分页请求
     * @return 积分记录分页
     */
    PageResult<ForumPointRecordDO> getPointRecordPage(AppPointRecordPageReqVO reqVO);

    /**
     * 获取用户积分余额
     *
     * @param userId 用户ID
     * @return 积分余额
     */
    Integer getUserPoint(Long userId);

    /**
     * 管理员调整积分
     *
     * @param userId 用户ID
     * @param point  调整积分（正数增加，负数减少）
     * @param reason 调整原因
     */
    void adminChangePoint(Long userId, Integer point, String reason);

    /**
     * 导出积分记录列表
     *
     * @param reqVO 查询条件（支持按uid筛选）
     * @return 积分记录列表（Excel格式）
     */
    List<AdminPointRecordExcelVO> getPointRecordExcelList(AdminPointRecordPageReqVO reqVO);

}


package cn.iocoder.yudao.module.forum.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppSystemNoticePageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemNoticeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛系统通知 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumSystemNoticeMapper extends BaseMapperX<ForumSystemNoticeDO> {

    /**
     * 分页查询用户的系统通知
     */
    default PageResult<ForumSystemNoticeDO> selectPage(AppSystemNoticePageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumSystemNoticeDO>()
                .eq(ForumSystemNoticeDO::getUserId, userId)
                .eqIfPresent(ForumSystemNoticeDO::getNoticeType, reqVO.getNoticeType())
                .eqIfPresent(ForumSystemNoticeDO::getReadStatus, reqVO.getReadStatus())
                .orderByDesc(ForumSystemNoticeDO::getCreateTime));
    }

    /**
     * 统计用户未读通知数
     */
    default Long countUnreadByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<ForumSystemNoticeDO>()
                .eq(ForumSystemNoticeDO::getUserId, userId)
                .eq(ForumSystemNoticeDO::getReadStatus, false));
    }

}


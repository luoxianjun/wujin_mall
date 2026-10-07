package cn.iocoder.yudao.module.forum.dal.mysql.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.point.vo.AppPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.point.ForumPointRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛积分记录 Mapper
 */
@Mapper
public interface ForumPointRecordMapper extends BaseMapperX<ForumPointRecordDO> {

    /**
     * 分页查询积分记录
     */
    default PageResult<ForumPointRecordDO> selectPage(AppPointRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumPointRecordDO>()
                .eqIfPresent(ForumPointRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ForumPointRecordDO::getBizType, reqVO.getBizType())
                .orderByDesc(ForumPointRecordDO::getId));
    }

}


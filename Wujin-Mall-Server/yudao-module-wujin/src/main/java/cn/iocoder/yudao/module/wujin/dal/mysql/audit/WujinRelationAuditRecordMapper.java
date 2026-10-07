package cn.iocoder.yudao.module.wujin.dal.mysql.audit;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinRelationAuditRecordMapper extends BaseMapperX<WujinRelationAuditRecordDO> {

    default List<WujinRelationAuditRecordDO> selectList(WujinRelationAuditRecordListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinRelationAuditRecordDO>()
                .eqIfPresent(WujinRelationAuditRecordDO::getSubmissionId, reqVO.getSubmissionId())
                .eqIfPresent(WujinRelationAuditRecordDO::getAuditorId, reqVO.getAuditorId())
                .eqIfPresent(WujinRelationAuditRecordDO::getAction, reqVO.getAction())
                .eqIfPresent(WujinRelationAuditRecordDO::getReason, reqVO.getReason())
                .eqIfPresent(WujinRelationAuditRecordDO::getEffectiveFlag, reqVO.getEffectiveFlag())
                .orderByDesc(WujinRelationAuditRecordDO::getId));
    }
}

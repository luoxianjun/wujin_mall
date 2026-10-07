package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;

import java.util.List;

public interface WujinRelationAuditRecordAdminService {

    Long createAuditRecord(WujinRelationAuditRecordSaveReqVO createReqVO);

    void updateAuditRecord(WujinRelationAuditRecordSaveReqVO updateReqVO);

    void deleteAuditRecord(Long id);

    WujinRelationAuditRecordDO getAuditRecord(Long id);

    List<WujinRelationAuditRecordDO> getAuditRecordList(WujinRelationAuditRecordListReqVO listReqVO);
}

package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.audit.WujinRelationAuditRecordMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationSubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinRelationAuditRecordAdminServiceImpl implements WujinRelationAuditRecordAdminService {

    @Resource
    private WujinRelationAuditRecordMapper auditRecordMapper;
    @Resource
    private WujinMerchantRelationSubmissionMapper submissionMapper;

    @Override
    public Long createAuditRecord(WujinRelationAuditRecordSaveReqVO createReqVO) {
        validateSubmissionExists(createReqVO.getSubmissionId());
        WujinRelationAuditRecordDO record = BeanUtils.toBean(createReqVO, WujinRelationAuditRecordDO.class);
        auditRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void updateAuditRecord(WujinRelationAuditRecordSaveReqVO updateReqVO) {
        validateAuditRecordExists(updateReqVO.getId());
        validateSubmissionExists(updateReqVO.getSubmissionId());
        WujinRelationAuditRecordDO record = BeanUtils.toBean(updateReqVO, WujinRelationAuditRecordDO.class);
        auditRecordMapper.updateById(record);
    }

    @Override
    public void deleteAuditRecord(Long id) {
        validateAuditRecordExists(id);
        auditRecordMapper.deleteById(id);
    }

    @Override
    public WujinRelationAuditRecordDO getAuditRecord(Long id) {
        return auditRecordMapper.selectById(id);
    }

    @Override
    public List<WujinRelationAuditRecordDO> getAuditRecordList(WujinRelationAuditRecordListReqVO listReqVO) {
        return auditRecordMapper.selectList(listReqVO);
    }

    private void validateAuditRecordExists(Long id) {
        if (id == null || auditRecordMapper.selectById(id) == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
    }

    private void validateSubmissionExists(Long submissionId) {
        if (submissionId == null || submissionMapper.selectById(submissionId) == null) {
            throw new IllegalArgumentException("审核记录关联申报不存在");
        }
    }
}

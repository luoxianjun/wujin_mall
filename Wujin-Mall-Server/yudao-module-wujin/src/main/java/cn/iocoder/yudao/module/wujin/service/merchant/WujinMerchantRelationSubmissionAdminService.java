package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;

import java.util.List;

public interface WujinMerchantRelationSubmissionAdminService {

    Long createSubmission(WujinMerchantRelationSubmissionSaveReqVO createReqVO);

    void updateSubmission(WujinMerchantRelationSubmissionSaveReqVO updateReqVO);

    void deleteSubmission(Long id);

    WujinMerchantRelationSubmissionDO getSubmission(Long id);

    List<WujinMerchantRelationSubmissionDO> getSubmissionList(WujinMerchantRelationSubmissionListReqVO listReqVO);
}

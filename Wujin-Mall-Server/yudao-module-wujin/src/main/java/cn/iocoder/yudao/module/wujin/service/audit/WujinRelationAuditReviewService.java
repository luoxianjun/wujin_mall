package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewRespVO;

public interface WujinRelationAuditReviewService {

    WujinRelationAuditReviewRespVO review(WujinRelationAuditReviewReqVO reqVO);
}

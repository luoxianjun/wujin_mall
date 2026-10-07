package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationSubmissionMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.template.WujinIndustryTemplateMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinMerchantRelationSubmissionAdminServiceImpl implements WujinMerchantRelationSubmissionAdminService {

    @Resource
    private WujinMerchantRelationSubmissionMapper submissionMapper;
    @Resource
    private WujinIndustryTemplateMapper templateMapper;

    @Override
    public Long createSubmission(WujinMerchantRelationSubmissionSaveReqVO createReqVO) {
        validateSubmission(createReqVO);
        WujinMerchantRelationSubmissionDO submission = BeanUtils.toBean(createReqVO, WujinMerchantRelationSubmissionDO.class);
        submissionMapper.insert(submission);
        return submission.getId();
    }

    @Override
    public void updateSubmission(WujinMerchantRelationSubmissionSaveReqVO updateReqVO) {
        validateSubmissionExists(updateReqVO.getId());
        validateSubmission(updateReqVO);
        WujinMerchantRelationSubmissionDO submission = BeanUtils.toBean(updateReqVO, WujinMerchantRelationSubmissionDO.class);
        submissionMapper.updateById(submission);
    }

    @Override
    public void deleteSubmission(Long id) {
        validateSubmissionExists(id);
        submissionMapper.deleteById(id);
    }

    @Override
    public WujinMerchantRelationSubmissionDO getSubmission(Long id) {
        return submissionMapper.selectById(id);
    }

    @Override
    public List<WujinMerchantRelationSubmissionDO> getSubmissionList(WujinMerchantRelationSubmissionListReqVO listReqVO) {
        return submissionMapper.selectList(listReqVO);
    }

    private void validateSubmissionExists(Long id) {
        if (id == null || submissionMapper.selectById(id) == null) {
            throw new IllegalArgumentException("商家关系申报不存在");
        }
    }

    private void validateSubmission(WujinMerchantRelationSubmissionSaveReqVO reqVO) {
        if (!WujinLane.PRODUCT.name().equals(reqVO.getProductLane())) {
            throw new IllegalArgumentException("商家发布商品必须归属成品泳道");
        }
        if (templateMapper.selectById(reqVO.getTemplateId()) == null) {
            throw new IllegalArgumentException("商家关系申报模板不存在");
        }
    }
}

package cn.iocoder.yudao.module.forum.service.sign;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleUpdateReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRuleDO;
import cn.iocoder.yudao.module.forum.dal.mysql.sign.ForumSignRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.SIGN_RULE_NOT_EXISTS;

@Service
@Validated
@RequiredArgsConstructor
public class ForumSignRuleServiceImpl implements ForumSignRuleService {

    private final ForumSignRuleMapper signRuleMapper;

    @Override
    public Long createRule(AdminSignRuleCreateReqVO reqVO) {
        ForumSignRuleDO rule = BeanUtil.copyProperties(reqVO, ForumSignRuleDO.class);
        signRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateRule(AdminSignRuleUpdateReqVO reqVO) {
        ForumSignRuleDO exists = signRuleMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception(SIGN_RULE_NOT_EXISTS);
        }
        ForumSignRuleDO updateObj = BeanUtil.copyProperties(reqVO, ForumSignRuleDO.class);
        signRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteRule(Long id) {
        ForumSignRuleDO exists = signRuleMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception(SIGN_RULE_NOT_EXISTS);
        }
        signRuleMapper.deleteById(id);
    }

    @Override
    public AdminSignRuleRespVO getRule(Long id) {
        ForumSignRuleDO rule = signRuleMapper.selectById(id);
        return rule == null ? null : BeanUtil.copyProperties(rule, AdminSignRuleRespVO.class);
    }

    @Override
    public List<AdminSignRuleRespVO> getRuleList(Integer periodType) {
        return signRuleMapper.selectListByPeriodType(periodType).stream()
                .map(rule -> BeanUtil.copyProperties(rule, AdminSignRuleRespVO.class))
                .collect(Collectors.toList());
    }
}

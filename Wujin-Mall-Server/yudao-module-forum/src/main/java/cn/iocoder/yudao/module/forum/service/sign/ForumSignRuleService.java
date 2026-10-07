package cn.iocoder.yudao.module.forum.service.sign;

import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleUpdateReqVO;

import java.util.List;

public interface ForumSignRuleService {

    Long createRule(AdminSignRuleCreateReqVO reqVO);

    void updateRule(AdminSignRuleUpdateReqVO reqVO);

    void deleteRule(Long id);

    AdminSignRuleRespVO getRule(Long id);

    List<AdminSignRuleRespVO> getRuleList(Integer periodType);
}


package cn.iocoder.yudao.module.forum.dal.mysql.sign;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRuleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛签到规则 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumSignRuleMapper extends BaseMapperX<ForumSignRuleDO> {

    default ForumSignRuleDO selectByPeriodAndDays(Integer periodType, Integer days) {
        return selectOne(new LambdaQueryWrapperX<ForumSignRuleDO>()
                .eq(ForumSignRuleDO::getPeriodType, periodType)
                .le(ForumSignRuleDO::getMinDays, days)
                .ge(ForumSignRuleDO::getMaxDays, days));
    }

    default java.util.List<ForumSignRuleDO> selectListByPeriodType(Integer periodType) {
        return selectList(new LambdaQueryWrapperX<ForumSignRuleDO>()
                .eqIfPresent(ForumSignRuleDO::getPeriodType, periodType)
                .orderByAsc(ForumSignRuleDO::getPeriodType, ForumSignRuleDO::getMinDays));
    }
}

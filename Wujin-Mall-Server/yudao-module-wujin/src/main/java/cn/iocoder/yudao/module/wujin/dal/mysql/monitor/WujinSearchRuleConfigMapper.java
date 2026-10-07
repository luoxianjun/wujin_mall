package cn.iocoder.yudao.module.wujin.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinSearchRuleConfigMapper extends BaseMapperX<WujinSearchRuleConfigDO> {

    default List<WujinSearchRuleConfigDO> selectList(WujinSearchRuleConfigListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinSearchRuleConfigDO>()
                .eqIfPresent(WujinSearchRuleConfigDO::getRuleType, reqVO.getRuleType())
                .eqIfPresent(WujinSearchRuleConfigDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinSearchRuleConfigDO::getIndustryCode, reqVO.getIndustryCode())
                .eqIfPresent(WujinSearchRuleConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(WujinSearchRuleConfigDO::getId));
    }
}

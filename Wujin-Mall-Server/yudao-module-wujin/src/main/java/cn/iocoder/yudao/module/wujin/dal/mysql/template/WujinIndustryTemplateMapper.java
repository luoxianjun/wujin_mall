package cn.iocoder.yudao.module.wujin.dal.mysql.template;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinIndustryTemplateMapper extends BaseMapperX<WujinIndustryTemplateDO> {

    default List<WujinIndustryTemplateDO> selectList(WujinIndustryTemplateListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinIndustryTemplateDO>()
                .eqIfPresent(WujinIndustryTemplateDO::getTemplateCode, reqVO.getTemplateCode())
                .likeIfPresent(WujinIndustryTemplateDO::getName, reqVO.getName())
                .eqIfPresent(WujinIndustryTemplateDO::getIndustryCode, reqVO.getIndustryCode())
                .eqIfPresent(WujinIndustryTemplateDO::getProductLane, reqVO.getProductLane())
                .eqIfPresent(WujinIndustryTemplateDO::getStatus, reqVO.getStatus())
                .orderByDesc(WujinIndustryTemplateDO::getId));
    }
}

package cn.iocoder.yudao.module.wujin.dal.mysql.template;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinIndustryTemplateItemMapper extends BaseMapperX<WujinIndustryTemplateItemDO> {

    default List<WujinIndustryTemplateItemDO> selectList(WujinIndustryTemplateItemListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinIndustryTemplateItemDO>()
                .eqIfPresent(WujinIndustryTemplateItemDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(WujinIndustryTemplateItemDO::getEntityId, reqVO.getEntityId())
                .eqIfPresent(WujinIndustryTemplateItemDO::getRelationType, reqVO.getRelationType())
                .eqIfPresent(WujinIndustryTemplateItemDO::getRequiredFlag, reqVO.getRequiredFlag())
                .orderByAsc(WujinIndustryTemplateItemDO::getSort)
                .orderByDesc(WujinIndustryTemplateItemDO::getId));
    }
}

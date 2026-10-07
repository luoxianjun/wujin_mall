package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMappingMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class WujinCategoryMappingAdminServiceImpl implements WujinCategoryMappingAdminService {

    @Resource
    private WujinCategoryMappingMapper mappingMapper;
    @Resource
    private WujinCategoryMapper categoryMapper;

    @Override
    public Long createMapping(WujinCategoryMappingSaveReqVO createReqVO) {
        validateMapping(createReqVO);
        WujinCategoryMappingDO mapping = BeanUtils.toBean(createReqVO, WujinCategoryMappingDO.class);
        mappingMapper.insert(mapping);
        return mapping.getId();
    }

    @Override
    public void updateMapping(WujinCategoryMappingSaveReqVO updateReqVO) {
        validateMappingExists(updateReqVO.getId());
        validateMapping(updateReqVO);
        WujinCategoryMappingDO mapping = BeanUtils.toBean(updateReqVO, WujinCategoryMappingDO.class);
        mappingMapper.updateById(mapping);
    }

    @Override
    public void deleteMapping(Long id) {
        validateMappingExists(id);
        mappingMapper.deleteById(id);
    }

    @Override
    public WujinCategoryMappingDO getMapping(Long id) {
        return mappingMapper.selectById(id);
    }

    @Override
    public List<WujinCategoryMappingDO> getMappingList(WujinCategoryMappingListReqVO listReqVO) {
        return mappingMapper.selectList(listReqVO);
    }

    private void validateMappingExists(Long id) {
        if (id == null || mappingMapper.selectById(id) == null) {
            throw new IllegalArgumentException("五金分类映射不存在");
        }
    }

    private void validateMapping(WujinCategoryMappingSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getSourceLane(), reqVO.getTargetLane())) {
            throw new IllegalArgumentException("跨泳道映射必须连接不同泳道");
        }
        WujinCategoryDO source = categoryMapper.selectById(reqVO.getSourceCategoryId());
        WujinCategoryDO target = categoryMapper.selectById(reqVO.getTargetCategoryId());
        if (source == null || target == null) {
            throw new IllegalArgumentException("分类映射节点不存在");
        }
        if (!Objects.equals(source.getLane(), reqVO.getSourceLane())
                || !Objects.equals(target.getLane(), reqVO.getTargetLane())) {
            throw new IllegalArgumentException("分类映射泳道与节点声明不一致");
        }
    }
}

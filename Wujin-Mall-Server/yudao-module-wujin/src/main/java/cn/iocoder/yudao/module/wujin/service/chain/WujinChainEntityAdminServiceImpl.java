package cn.iocoder.yudao.module.wujin.service.chain;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinChainEntityAdminServiceImpl implements WujinChainEntityAdminService {

    @Resource
    private WujinChainEntityMapper entityMapper;

    @Override
    public Long createEntity(WujinChainEntitySaveReqVO createReqVO) {
        WujinChainEntityDO entity = BeanUtils.toBean(createReqVO, WujinChainEntityDO.class);
        entityMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateEntity(WujinChainEntitySaveReqVO updateReqVO) {
        validateEntityExists(updateReqVO.getId());
        WujinChainEntityDO entity = BeanUtils.toBean(updateReqVO, WujinChainEntityDO.class);
        entityMapper.updateById(entity);
    }

    @Override
    public void deleteEntity(Long id) {
        validateEntityExists(id);
        entityMapper.deleteById(id);
    }

    @Override
    public WujinChainEntityDO getEntity(Long id) {
        return entityMapper.selectById(id);
    }

    @Override
    public List<WujinChainEntityDO> getEntityList(WujinChainEntityListReqVO listReqVO) {
        return entityMapper.selectList(listReqVO);
    }

    private void validateEntityExists(Long id) {
        if (id == null || entityMapper.selectById(id) == null) {
            throw new IllegalArgumentException("产业链实体不存在");
        }
    }
}

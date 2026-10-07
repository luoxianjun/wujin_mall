package cn.iocoder.yudao.module.wujin.service.chain;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class WujinChainEntityRelationAdminServiceImpl implements WujinChainEntityRelationAdminService {

    @Resource
    private WujinChainEntityRelationMapper relationMapper;
    @Resource
    private WujinChainEntityMapper entityMapper;

    @Override
    public Long createRelation(WujinChainEntityRelationSaveReqVO createReqVO) {
        validateRelation(createReqVO);
        WujinChainEntityRelationDO relation = BeanUtils.toBean(createReqVO, WujinChainEntityRelationDO.class);
        relationMapper.insert(relation);
        return relation.getId();
    }

    @Override
    public void updateRelation(WujinChainEntityRelationSaveReqVO updateReqVO) {
        validateRelationExists(updateReqVO.getId());
        validateRelation(updateReqVO);
        WujinChainEntityRelationDO relation = BeanUtils.toBean(updateReqVO, WujinChainEntityRelationDO.class);
        relationMapper.updateById(relation);
    }

    @Override
    public void deleteRelation(Long id) {
        validateRelationExists(id);
        relationMapper.deleteById(id);
    }

    @Override
    public WujinChainEntityRelationDO getRelation(Long id) {
        return relationMapper.selectById(id);
    }

    @Override
    public List<WujinChainEntityRelationDO> getRelationList(WujinChainEntityRelationListReqVO listReqVO) {
        return relationMapper.selectList(listReqVO);
    }

    private void validateRelationExists(Long id) {
        if (id == null || relationMapper.selectById(id) == null) {
            throw new IllegalArgumentException("产业链实体关系不存在");
        }
    }

    private void validateRelation(WujinChainEntityRelationSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getSourceEntityId(), reqVO.getTargetEntityId())) {
            throw new IllegalArgumentException("实体关系不能指向自身");
        }
        if (entityMapper.selectById(reqVO.getSourceEntityId()) == null
                || entityMapper.selectById(reqVO.getTargetEntityId()) == null) {
            throw new IllegalArgumentException("产业链实体关系节点不存在");
        }
    }
}

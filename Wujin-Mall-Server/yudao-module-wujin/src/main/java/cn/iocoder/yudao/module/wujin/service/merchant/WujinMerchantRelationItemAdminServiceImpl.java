package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationItemMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationSubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinMerchantRelationItemAdminServiceImpl implements WujinMerchantRelationItemAdminService {

    @Resource
    private WujinMerchantRelationItemMapper itemMapper;
    @Resource
    private WujinMerchantRelationSubmissionMapper submissionMapper;
    @Resource
    private WujinChainEntityMapper entityMapper;

    @Override
    public Long createItem(WujinMerchantRelationItemSaveReqVO createReqVO) {
        validateItem(createReqVO);
        WujinMerchantRelationItemDO item = BeanUtils.toBean(createReqVO, WujinMerchantRelationItemDO.class);
        itemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateItem(WujinMerchantRelationItemSaveReqVO updateReqVO) {
        validateItemExists(updateReqVO.getId());
        validateItem(updateReqVO);
        WujinMerchantRelationItemDO item = BeanUtils.toBean(updateReqVO, WujinMerchantRelationItemDO.class);
        itemMapper.updateById(item);
    }

    @Override
    public void deleteItem(Long id) {
        validateItemExists(id);
        itemMapper.deleteById(id);
    }

    @Override
    public WujinMerchantRelationItemDO getItem(Long id) {
        return itemMapper.selectById(id);
    }

    @Override
    public List<WujinMerchantRelationItemDO> getItemList(WujinMerchantRelationItemListReqVO listReqVO) {
        return itemMapper.selectList(listReqVO);
    }

    private void validateItemExists(Long id) {
        if (id == null || itemMapper.selectById(id) == null) {
            throw new IllegalArgumentException("商家关系申报项不存在");
        }
    }

    private void validateItem(WujinMerchantRelationItemSaveReqVO reqVO) {
        if (submissionMapper.selectById(reqVO.getSubmissionId()) == null) {
            throw new IllegalArgumentException("商家关系申报不存在");
        }
        if (entityMapper.selectById(reqVO.getEntityId()) == null) {
            throw new IllegalArgumentException("商家关系申报实体不存在");
        }
    }
}

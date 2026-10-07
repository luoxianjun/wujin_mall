package cn.iocoder.yudao.module.wujin.service.chain;

import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;

import java.util.List;

public interface WujinChainEntityAdminService {

    Long createEntity(WujinChainEntitySaveReqVO createReqVO);

    void updateEntity(WujinChainEntitySaveReqVO updateReqVO);

    void deleteEntity(Long id);

    WujinChainEntityDO getEntity(Long id);

    List<WujinChainEntityDO> getEntityList(WujinChainEntityListReqVO listReqVO);
}

package cn.iocoder.yudao.module.wujin.service.chain;

import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;

import java.util.List;

public interface WujinChainEntityRelationAdminService {

    Long createRelation(WujinChainEntityRelationSaveReqVO createReqVO);

    void updateRelation(WujinChainEntityRelationSaveReqVO updateReqVO);

    void deleteRelation(Long id);

    WujinChainEntityRelationDO getRelation(Long id);

    List<WujinChainEntityRelationDO> getRelationList(WujinChainEntityRelationListReqVO listReqVO);
}

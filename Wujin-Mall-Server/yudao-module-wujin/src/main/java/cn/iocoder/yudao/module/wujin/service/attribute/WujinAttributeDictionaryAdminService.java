package cn.iocoder.yudao.module.wujin.service.attribute;

import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionarySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;

import java.util.List;

public interface WujinAttributeDictionaryAdminService {

    Long createAttribute(WujinAttributeDictionarySaveReqVO createReqVO);

    void updateAttribute(WujinAttributeDictionarySaveReqVO updateReqVO);

    void deleteAttribute(Long id);

    WujinAttributeDictionaryDO getAttribute(Long id);

    List<WujinAttributeDictionaryDO> getAttributeList(WujinAttributeDictionaryListReqVO listReqVO);

    /**
     * 获得指定泳道启用的属性定义（包含三泳道通用属性），供商家发布向导填写标准属性
     */
    List<WujinAttributeDictionaryDO> getEnabledAttributeList(String lane);
}

package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchRespVO;

public interface WujinAppSearchService {

    WujinAppSearchRespVO search(WujinAppSearchReqVO reqVO);
}

package cn.iocoder.yudao.module.wujin.service.trace;

import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphRespVO;

public interface WujinAppTraceService {

    WujinTraceGraphRespVO getTraceGraph(WujinTraceGraphReqVO reqVO);
}

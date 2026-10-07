package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailRespVO;

public interface WujinAppDetailService {

    WujinEntityDetailRespVO getEntityDetail(WujinEntityDetailReqVO reqVO);
}

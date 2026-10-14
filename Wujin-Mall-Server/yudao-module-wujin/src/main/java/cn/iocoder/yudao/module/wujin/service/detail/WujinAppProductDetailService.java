package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinProductDetailRespVO;

/**
 * 五金商品详情 Service：基于商城商品、供应能力索引、标准属性与产业链关系组装真实详情
 */
public interface WujinAppProductDetailService {

    /**
     * 获得商品详情；商品不存在或已下架时返回 exists=false
     *
     * @param productId 商城商品 SPU 编号
     */
    WujinProductDetailRespVO getProductDetail(Long productId);
}

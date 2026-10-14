package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductPageRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductReqVO;

import java.util.Map;

/**
 * 五金泳道商品 Service：每个商品只归属一个泳道，同一搜索词在成品、加工、原材料泳道下互不重复
 *
 * 商品归属泳道按其供应能力索引判断，优先级为成品 &gt; 加工 &gt; 原材料；未建立供应能力索引的商城商品视为成品。
 * 加工、原材料泳道除直接匹配关键词外，还会沿产业链关系查找关键词对应成品的上游工艺、材料。
 */
public interface WujinLaneProductService {

    WujinLaneProductPageRespVO getLaneProductPage(WujinLaneProductReqVO reqVO);

    /**
     * 统计关键词在三个泳道下的商品数量，key 为泳道名
     */
    Map<String, Integer> countLaneProducts(String keyword, Long sourceProductId);
}

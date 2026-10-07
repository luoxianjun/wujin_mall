package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuCreateReqDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商品 SPU API 接口
 */
public interface ProductSpuApi {

    /**
     * 创建 SPU
     *
     * @param createReqDTO SPU 创建请求
     * @return SPU 编号
     */
    Long createSpu(ProductSpuCreateReqDTO createReqDTO);

    /**
     * 批量查询 SPU 数组
     *
     * @param ids SPU 编号列表
     * @return SPU 数组
     */
    List<ProductSpuRespDTO> getSpuList(Collection<Long> ids);

    /**
     * 批量查询 SPU MAP
     *
     * @param ids SPU 编号列表
     * @return SPU MAP
     */
    default Map<Long, ProductSpuRespDTO> getSpuMap(Collection<Long> ids) {
        return convertMap(getSpuList(ids), ProductSpuRespDTO::getId);
    }

    /**
     * 批量查询 SPU 数组，并且校验 SPU 是否有效。
     *
     * @param ids SPU 编号列表
     * @return SPU 数组
     */
    List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids);

    /**
     * 获得 SPU
     *
     * @param id SPU 编号
     * @return SPU
     */
    ProductSpuRespDTO getSpu(Long id);

}

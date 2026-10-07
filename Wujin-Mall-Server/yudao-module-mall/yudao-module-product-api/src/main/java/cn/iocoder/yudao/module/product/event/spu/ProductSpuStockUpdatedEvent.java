package cn.iocoder.yudao.module.product.event.spu;

public class ProductSpuStockUpdatedEvent {

    private final Long spuId;
    private final String spuName;
    private final Integer stockIncrCount;
    private final Integer newStock;

    public ProductSpuStockUpdatedEvent(Long spuId, String spuName, Integer stockIncrCount, Integer newStock) {
        this.spuId = spuId;
        this.spuName = spuName;
        this.stockIncrCount = stockIncrCount;
        this.newStock = newStock;
    }

    public Long getSpuId() {
        return spuId;
    }

    public String getSpuName() {
        return spuName;
    }

    public Integer getStockIncrCount() {
        return stockIncrCount;
    }

    public Integer getNewStock() {
        return newStock;
    }
}

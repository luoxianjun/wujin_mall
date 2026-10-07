package cn.iocoder.yudao.module.product.event.spu;

public class ProductSpuStatusUpdatedEvent {

    private final Long spuId;
    private final String spuName;
    private final Integer oldStatus;
    private final Integer newStatus;

    public ProductSpuStatusUpdatedEvent(Long spuId, String spuName, Integer oldStatus, Integer newStatus) {
        this.spuId = spuId;
        this.spuName = spuName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Long getSpuId() {
        return spuId;
    }

    public String getSpuName() {
        return spuName;
    }

    public Integer getOldStatus() {
        return oldStatus;
    }

    public Integer getNewStatus() {
        return newStatus;
    }
}

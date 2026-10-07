package cn.iocoder.yudao.module.wujin.merchant;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;

public class WujinMerchantRelationDraft {

    private final String entityId;
    private final WujinRelationType relationType;
    private final boolean fromTemplate;

    public WujinMerchantRelationDraft(String entityId, WujinRelationType relationType, boolean fromTemplate) {
        this.entityId = entityId;
        this.relationType = relationType;
        this.fromTemplate = fromTemplate;
    }

    public String getEntityId() {
        return entityId;
    }

    public WujinRelationType getRelationType() {
        return relationType;
    }

    public boolean isFromTemplate() {
        return fromTemplate;
    }
}

package cn.iocoder.yudao.module.wujin.merchant;

import java.util.Collections;
import java.util.List;

public class WujinIndustryTemplate {

    private final String id;
    private final List<String> requiredMaterialEntityIds;
    private final List<String> requiredProcessEntityIds;

    public WujinIndustryTemplate(String id, List<String> requiredMaterialEntityIds, List<String> requiredProcessEntityIds) {
        this.id = id;
        this.requiredMaterialEntityIds = requiredMaterialEntityIds == null ? Collections.emptyList() : requiredMaterialEntityIds;
        this.requiredProcessEntityIds = requiredProcessEntityIds == null ? Collections.emptyList() : requiredProcessEntityIds;
    }

    public String getId() {
        return id;
    }

    public List<String> getRequiredMaterialEntityIds() {
        return requiredMaterialEntityIds;
    }

    public List<String> getRequiredProcessEntityIds() {
        return requiredProcessEntityIds;
    }
}

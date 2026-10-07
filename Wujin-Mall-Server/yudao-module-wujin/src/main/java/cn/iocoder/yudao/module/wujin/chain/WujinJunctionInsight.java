package cn.iocoder.yudao.module.wujin.chain;

import java.util.Collections;
import java.util.List;

public class WujinJunctionInsight {

    private final boolean junction;
    private final List<String> industries;

    public WujinJunctionInsight(boolean junction, List<String> industries) {
        this.junction = junction;
        this.industries = industries == null ? Collections.emptyList() : industries;
    }

    public boolean isJunction() {
        return junction;
    }

    public List<String> getIndustries() {
        return industries;
    }
}

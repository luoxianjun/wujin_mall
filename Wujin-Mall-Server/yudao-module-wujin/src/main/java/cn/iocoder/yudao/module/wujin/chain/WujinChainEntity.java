package cn.iocoder.yudao.module.wujin.chain;

import cn.iocoder.yudao.module.wujin.search.WujinLane;

import java.util.Collections;
import java.util.List;

public class WujinChainEntity {

    private final String id;
    private final String name;
    private final WujinLane lane;
    private final List<String> industries;

    public WujinChainEntity(String id, String name, WujinLane lane, List<String> industries) {
        this.id = id;
        this.name = name;
        this.lane = lane;
        this.industries = industries == null ? Collections.emptyList() : industries;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WujinLane getLane() {
        return lane;
    }

    public List<String> getIndustries() {
        return industries;
    }
}

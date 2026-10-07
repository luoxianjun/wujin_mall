package cn.iocoder.yudao.module.wujin.search;

import java.util.Objects;

public class WujinCategoryMapping {

    private final String id;
    private final String path;
    private final WujinLane lane;
    private final int weight;

    public WujinCategoryMapping(String id, String path, WujinLane lane, int weight) {
        this.id = id;
        this.path = path;
        this.lane = lane;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public WujinLane getLane() {
        return lane;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WujinCategoryMapping)) {
            return false;
        }
        WujinCategoryMapping that = (WujinCategoryMapping) o;
        return weight == that.weight
                && Objects.equals(id, that.id)
                && Objects.equals(path, that.path)
                && lane == that.lane;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, lane, weight);
    }
}

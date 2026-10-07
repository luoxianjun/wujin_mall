package cn.iocoder.yudao.module.wujin.audit;

public class WujinSimilarEntity {

    private final String entityId;
    private final String name;
    private final int similarity;

    public WujinSimilarEntity(String entityId, String name, int similarity) {
        this.entityId = entityId;
        this.name = name;
        this.similarity = similarity;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public int getSimilarity() {
        return similarity;
    }
}

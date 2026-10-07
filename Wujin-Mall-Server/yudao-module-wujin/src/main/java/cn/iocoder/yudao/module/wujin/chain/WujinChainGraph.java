package cn.iocoder.yudao.module.wujin.chain;

import java.util.Collections;
import java.util.List;

public class WujinChainGraph {

    private final List<WujinChainEntity> entities;
    private final List<WujinChainRelation> relations;

    public WujinChainGraph(List<WujinChainEntity> entities, List<WujinChainRelation> relations) {
        this.entities = entities == null ? Collections.emptyList() : entities;
        this.relations = relations == null ? Collections.emptyList() : relations;
    }

    public List<WujinChainEntity> getEntities() {
        return entities;
    }

    public List<WujinChainRelation> getRelations() {
        return relations;
    }

    public WujinChainEntity getEntity(String entityId) {
        for (WujinChainEntity entity : entities) {
            if (entity.getId().equals(entityId)) {
                return entity;
            }
        }
        return null;
    }
}

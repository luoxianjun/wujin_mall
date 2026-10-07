package cn.iocoder.yudao.module.wujin.chain;

public class WujinChainRelation {

    private final WujinChainEntity source;
    private final WujinChainEntity target;
    private final WujinRelationType relationType;
    private final String industryContext;

    public WujinChainRelation(WujinChainEntity source, WujinChainEntity target,
                              WujinRelationType relationType, String industryContext) {
        this.source = source;
        this.target = target;
        this.relationType = relationType;
        this.industryContext = industryContext;
    }

    public WujinChainEntity getSource() {
        return source;
    }

    public WujinChainEntity getTarget() {
        return target;
    }

    public WujinRelationType getRelationType() {
        return relationType;
    }

    public String getIndustryContext() {
        return industryContext;
    }
}

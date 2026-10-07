package cn.iocoder.yudao.module.wujin.controller.app.trace.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户 App - 五金溯源图谱 Request VO")
public class WujinTraceGraphReqVO {

    @Schema(description = "产业链实体编号")
    private Long id;

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "当前泳道：PRODUCT/PROCESS/MATERIAL")
    private String lane;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "来源关键词")
    private String sourceKeyword;

    @Schema(description = "行业上下文")
    private String industry;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    public void setSourceKeyword(String sourceKeyword) {
        this.sourceKeyword = sourceKeyword;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}

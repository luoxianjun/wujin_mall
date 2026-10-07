package cn.iocoder.yudao.module.wujin.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 五金行业模板项批量迁移 Request VO")
public class WujinIndustryTemplateItemBatchMigrateReqVO {

    @Schema(description = "模板项编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模板项编号列表不能为空")
    private List<Long> ids;

    @Schema(description = "目标模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标模板编号不能为空")
    private Long targetTemplateId;

    @Schema(description = "覆盖关系类型，为空时保留原值", example = "REQUIRES_PROCESS")
    private String relationType;

    @Schema(description = "覆盖是否必填，为空时保留原值", example = "false")
    private Boolean requiredFlag;

    @Schema(description = "覆盖权重，为空时保留原值", example = "80")
    private Integer weight;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Long getTargetTemplateId() {
        return targetTemplateId;
    }

    public void setTargetTemplateId(Long targetTemplateId) {
        this.targetTemplateId = targetTemplateId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Boolean getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Boolean requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}

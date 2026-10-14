package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 五金分类同级排序 Request VO")
public class WujinCategorySortReqVO {

    @Schema(description = "父分类编号，根节点为 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @Schema(description = "按新顺序排列的同级分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "[3, 48]")
    @NotEmpty(message = "分类编号不能为空")
    private List<Long> ids;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}

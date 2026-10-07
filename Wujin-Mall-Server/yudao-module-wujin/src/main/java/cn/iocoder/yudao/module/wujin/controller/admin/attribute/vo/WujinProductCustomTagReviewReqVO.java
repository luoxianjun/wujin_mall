package cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金商品自定义标签审核 Request VO")
public class WujinProductCustomTagReviewReqVO {

    @NotNull(message = "标签编号不能为空")
    private Long id;
    /**
     * 审核动作：APPROVE/REJECT
     */
    @NotBlank(message = "审核动作不能为空")
    private String action;
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

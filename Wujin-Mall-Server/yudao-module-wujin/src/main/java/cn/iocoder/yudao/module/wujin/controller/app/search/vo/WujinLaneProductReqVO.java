package cn.iocoder.yudao.module.wujin.controller.app.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Schema(description = "用户 App - 五金泳道商品分页 Request VO")
public class WujinLaneProductReqVO {

    public static final String SORT_DEFAULT = "DEFAULT";
    public static final String SORT_PRICE_ASC = "PRICE_ASC";
    public static final String SORT_PRICE_DESC = "PRICE_DESC";
    public static final String SORT_SALES = "SALES";

    @Schema(description = "搜索关键词；分类、品牌筛选时保持原关键词", requiredMode = Schema.RequiredMode.REQUIRED, example = "轮胎")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Schema(description = "泳道：PRODUCT/PROCESS/MATERIAL，默认 PRODUCT", example = "PRODUCT")
    private String lane;

    @Schema(description = "五金分类编号，筛选该分类及其子分类", example = "3")
    private Long categoryId;

    @Schema(description = "品牌编号", example = "2000")
    private Long brandId;

    @Schema(description = "来源商品编号，用于从商品详情进入上游泳道", example = "9001")
    private Long sourceProductId;

    @Schema(description = "排序：DEFAULT/PRICE_ASC/PRICE_DESC/SALES", example = "DEFAULT")
    private String sort;

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码最小为 1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数，最大 50", example = "10")
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 50, message = "每页条数最大为 50")
    private Integer pageSize = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

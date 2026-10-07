package cn.iocoder.yudao.module.wujin.controller.app.detail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金详情 Response VO")
public class WujinEntityDetailRespVO {

    @Schema(description = "实体编号")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "标签")
    private String entityLabel;

    @Schema(description = "模块标题")
    private String sectionTitle;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "参考价")
    private String price;

    @Schema(description = "起订量")
    private String moq;

    @Schema(description = "规格")
    private String spec;

    @Schema(description = "认证")
    private String certification;

    @Schema(description = "详情模块")
    private List<DetailSection> sections;

    @Schema(description = "跨行业提示")
    private List<String> crossIndustryTips;

    @Schema(description = "行业对比")
    private List<IndustryComparison> industryComparison;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityLabel() {
        return entityLabel;
    }

    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMoq() {
        return moq;
    }

    public void setMoq(String moq) {
        this.moq = moq;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public List<DetailSection> getSections() {
        return sections;
    }

    public void setSections(List<DetailSection> sections) {
        this.sections = sections;
    }

    public List<String> getCrossIndustryTips() {
        return crossIndustryTips;
    }

    public void setCrossIndustryTips(List<String> crossIndustryTips) {
        this.crossIndustryTips = crossIndustryTips;
    }

    public List<IndustryComparison> getIndustryComparison() {
        return industryComparison;
    }

    public void setIndustryComparison(List<IndustryComparison> industryComparison) {
        this.industryComparison = industryComparison;
    }

    public static class DetailSection {
        private String label;
        private String value;

        public DetailSection() {
        }

        public DetailSection(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class IndustryComparison {
        private String industry;
        private String standard;
        private String note;

        public IndustryComparison() {
        }

        public IndustryComparison(String industry, String standard, String note) {
            this.industry = industry;
            this.standard = standard;
            this.note = note;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getStandard() {
            return standard;
        }

        public void setStandard(String standard) {
            this.standard = standard;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}

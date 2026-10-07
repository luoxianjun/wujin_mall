package cn.iocoder.yudao.module.wujin.controller.app.trace.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金溯源图谱 Response VO")
public class WujinTraceGraphRespVO {

    @Schema(description = "图谱标题")
    private String title;

    @Schema(description = "图谱摘要")
    private String summary;

    @Schema(description = "输出节点名称")
    private String outputName;

    @Schema(description = "输出节点补充信息")
    private String outputMeta;

    @Schema(description = "当前批次")
    private TraceBatch currentBatch;

    @Schema(description = "图谱节点")
    private List<TraceNode> nodes;

    @Schema(description = "图谱关系")
    private List<TraceEdge> edges;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getOutputMeta() {
        return outputMeta;
    }

    public void setOutputMeta(String outputMeta) {
        this.outputMeta = outputMeta;
    }

    public TraceBatch getCurrentBatch() {
        return currentBatch;
    }

    public void setCurrentBatch(TraceBatch currentBatch) {
        this.currentBatch = currentBatch;
    }

    public List<TraceNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TraceNode> nodes) {
        this.nodes = nodes;
    }

    public List<TraceEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<TraceEdge> edges) {
        this.edges = edges;
    }

    public static class TraceNode {
        private String id;
        private String lane;
        private String name;
        private String description;

        public TraceNode() {
        }

        public TraceNode(String id, String lane, String name, String description) {
            this.id = id;
            this.lane = lane;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class TraceEdge {
        private String from;
        private String fromName;
        private String to;
        private String toName;
        private String relation;

        public TraceEdge() {
        }

        public TraceEdge(String from, String fromName, String to, String toName, String relation) {
            this.from = from;
            this.fromName = fromName;
            this.to = to;
            this.toName = toName;
            this.relation = relation;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getToName() {
            return toName;
        }

        public void setToName(String toName) {
            this.toName = toName;
        }

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }
    }

    public static class TraceBatch {
        private String batchNo;
        private String productionDate;
        private String factory;
        private String qualityStatus;

        public TraceBatch() {
        }

        public TraceBatch(String batchNo, String productionDate, String factory, String qualityStatus) {
            this.batchNo = batchNo;
            this.productionDate = productionDate;
            this.factory = factory;
            this.qualityStatus = qualityStatus;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        public String getQualityStatus() {
            return qualityStatus;
        }

        public void setQualityStatus(String qualityStatus) {
            this.qualityStatus = qualityStatus;
        }
    }
}

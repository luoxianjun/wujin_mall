package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 腾讯 IM 账号导入结果 VO")
@Data
public class TencentImAccountImportRespVO {

    @Schema(description = "导入总数", example = "100")
    private long total;

    @Schema(description = "成功数量", example = "95")
    private long successCount;

    @Schema(description = "失败数量", example = "5")
    private long failedCount;

    @Schema(description = "失败详情（最多返回前 50 条）")
    private List<FailureItem> failures;

    @Data
    public static class FailureItem {

        @Schema(description = "用户编号", example = "1024")
        private Long userId;

        @Schema(description = "IM Identifier", example = "1024")
        private String identifier;

        @Schema(description = "腾讯 IM 错误码", example = "70402")
        private Integer errorCode;

        @Schema(description = "错误信息", example = "参数非法，请检查必填字段是否填充")
        private String errorInfo;
    }

}

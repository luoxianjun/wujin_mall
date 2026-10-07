package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 腾讯 IM 账号删除结果 VO")
@Data
public class TencentImAccountDeleteRespVO {

    @Schema(description = "请求总数", example = "2")
    private long total;

    @Schema(description = "成功数量", example = "1")
    private long successCount;

    @Schema(description = "失败数量", example = "1")
    private long failedCount;

    @Schema(description = "账号删除结果明细")
    private List<ResultItem> items;

    @Data
    public static class ResultItem {

        @Schema(description = "用户编号（可为空）", example = "10086")
        private Long userId;

        @Schema(description = "IM Identifier / UserID", example = "10086")
        private String identifier;

        @Schema(description = "错误码，0 代表成功", example = "0")
        private Integer resultCode;

        @Schema(description = "错误信息", example = "Err_TLS_PT_Open_Login_Account_Not_Exist")
        private String resultInfo;
    }

}

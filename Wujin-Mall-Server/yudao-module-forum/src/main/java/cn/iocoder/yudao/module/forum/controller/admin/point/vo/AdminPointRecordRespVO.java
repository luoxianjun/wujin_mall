package cn.iocoder.yudao.module.forum.controller.admin.point.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分记录响应 VO")
@Data
public class AdminPointRecordRespVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "小明")
    private String nickname;

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @Schema(description = "业务编号", example = "post123")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "积分标题", example = "发布帖子")
    private String title;

    @Schema(description = "积分描述", example = "发布帖子获得10积分")
    private String description;

    @Schema(description = "变动积分", example = "10")
    private Integer point;

    @Schema(description = "变动后积分余额", example = "120")
    private Integer totalPoint;

    @Schema(description = "创建时间", example = "2024-05-01 08:00:00")
    private LocalDateTime createTime;
}

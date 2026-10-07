package cn.iocoder.yudao.module.forum.controller.admin.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理后台 - 编辑活动请求 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - 编辑活动请求 VO")
@Data
public class AdminActivityUpdateReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long id;

    @Schema(description = "活动标题", example = "校园马拉松")
    private String title;

    @Schema(description = "活动描述", example = "欢迎参加...")
    private String description;

    @Schema(description = "管理员 memberId 列表", example = "[1001, 1002]")
    private List<Long> adminMemberIds;

    @Schema(description = "活动封面图", example = "https://example.com/cover.jpg")
    private String coverImage;

    @Schema(description = "活动详情图片列表", example = "[\"https://example.com/1.jpg\"]")
    private List<String> detailImages;

    @Schema(description = "活动分类", example = "2")
    private Integer category;

    @Schema(description = "活动地点", example = "体育馆")
    private String location;

    @Schema(description = "活动地点经度", example = "113.12345")
    private Double longitude;

    @Schema(description = "活动地点纬度", example = "22.12345")
    private Double latitude;

    @Schema(description = "活动开始时间", example = "2024-01-01 10:00:00")
    private String startTime;

    @Schema(description = "活动结束时间", example = "2024-01-01 12:00:00")
    private String endTime;

    @Schema(description = "报名开始时间", example = "2024-01-01 08:00:00")
    private String signUpStartTime;

    @Schema(description = "报名结束时间", example = "2024-01-01 09:00:00")
    private String signUpEndTime;

    @Schema(description = "签到开始时间", example = "2024-01-01 09:30:00")
    private String checkInStartTime;

    @Schema(description = "签到结束时间", example = "2024-01-01 10:30:00")
    private String checkInEndTime;

    @Schema(description = "签到距离限制（米）", example = "100")
    private Integer checkInDistance;

    @Schema(description = "签到方式：1-自助签到；2-定位签到；3-扫码签到", example = "1")
    private Integer checkInType;

    @Schema(description = "报名人数限制，0表示不限制", example = "100")
    private Integer maxParticipants;

    @Schema(description = "是否需要审核报名", example = "false")
    private Boolean needApproval;

    @Schema(description = "是否仅本校可见", example = "false")
    private Boolean schoolOnly;

    @Schema(description = "是否热门：1-是，0-否", example = "0")
    private Integer hot;

    @Schema(description = "报名是否需要积分", example = "false")
    private Boolean needPoint;

    @Schema(description = "报名所需积分", example = "10")
    private Integer pointAmount;

    @Schema(description = "报名要求", example = "需携带学生证，提前10分钟到场")
    private String requirements;

    @Schema(description = "是否允许未实名用户报名", example = "true")
    private Boolean allowUnverified;

    @Schema(description = "自定义报名字段配置JSON", example = "[{\"key\":\"name\",\"label\":\"姓名\",\"type\":\"input\",\"required\":true}]")
    private String customFields;

    @Schema(description = "是否显示报名人数", example = "true")
    private Boolean showParticipantCount;

    @Schema(description = "跳转小程序appId", example = "wx1234567890")
    private String redirectAppId;

    @Schema(description = "跳转小程序页面路径", example = "pages/index/index")
    private String redirectAppPath;

    @Schema(description = "跳转小程序名称（按钮显示文案）", example = "打开XX小程序")
    private String redirectAppName;

    @Schema(description = "是否隐藏：true-隐藏，false-展示", example = "false")
    private Boolean hidden;

}

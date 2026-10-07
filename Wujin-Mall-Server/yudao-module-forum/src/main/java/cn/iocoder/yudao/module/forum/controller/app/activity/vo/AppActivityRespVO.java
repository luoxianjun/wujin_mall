package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动响应 VO")
@Data
public class AppActivityRespVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "发布人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "管理员 memberId 列表", example = "[1001, 1002]")
    private List<Long> adminMemberIds;

    @Schema(description = "管理员昵称列表", example = "[\"管理员小明\", \"管理员小红\"]")
    private List<String> adminMemberNicknames;

    @Schema(description = "管理员UID列表", example = "[\"U1001\", \"U1002\"]")
    private List<String> adminMemberUids;

    @Schema(description = "发布人UID", example = "U123456")
    private String uid;

    @Schema(description = "发布人昵称", example = "张三")
    private String nickname;

    @Schema(description = "发布人头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "校园马拉松")
    private String title;

    @Schema(description = "活动描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "欢迎参加...")
    private String description;

    @Schema(description = "活动封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/cover.jpg")
    private String coverImage;

    @Schema(description = "活动详情图片列表", example = "[\"https://example.com/1.jpg\"]")
    private List<String> detailImages;

    @Schema(description = "活动分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer category;

    @Schema(description = "活动分类名称", example = "文体活动")
    private String categoryName;

    @Schema(description = "活动地点", requiredMode = Schema.RequiredMode.REQUIRED, example = "体育馆")
    private String location;

    @Schema(description = "活动地点经度", example = "113.12345")
    private Double longitude;

    @Schema(description = "活动地点纬度", example = "22.12345")
    private Double latitude;

    @Schema(description = "活动开始时间", example = "2024-01-01 10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", example = "2024-01-01 12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "报名开始时间", example = "2024-01-01 08:00:00")
    private LocalDateTime signUpStartTime;

    @Schema(description = "报名结束时间", example = "2024-01-01 09:00:00")
    private LocalDateTime signUpEndTime;

    @Schema(description = "签到开始时间", example = "2024-01-01 09:30:00")
    private LocalDateTime checkInStartTime;

    @Schema(description = "签到结束时间", example = "2024-01-01 10:30:00")
    private LocalDateTime checkInEndTime;

    @Schema(description = "签到距离限制（米）", example = "100")
    private Integer checkInDistance;

    @Schema(description = "签到方式：1-自助签到；2-定位签到；3-扫码签到", example = "1")
    private Integer checkInType;

    @Schema(description = "报名人数限制", example = "100")
    private Integer maxParticipants;

    @Schema(description = "当前报名人数", example = "50")
    private Integer currentParticipants;

    @Schema(description = "是否需要审核报名", example = "false")
    private Boolean needApproval;

    @Schema(description = "是否仅本校可见", example = "false")
    private Boolean schoolOnly;

    @Schema(description = "活动状态", example = "1")
    private Integer status;

    @Schema(description = "活动状态名称", example = "报名中")
    private String statusName;

    @Schema(description = "浏览次数", example = "100")
    private Integer viewCount;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "是否热门：1-是，0-否", example = "0")
    private Integer hot;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "报名是否需要积分", example = "false")
    private Boolean needPoint;

    @Schema(description = "报名所需积分", example = "10")
    private Integer pointAmount;

    @Schema(description = "报名要求", example = "需携带学生证，提前10分钟到场")
    private String requirements;

    @Schema(description = "是否允许未实名用户报名", example = "true")
    private Boolean allowUnverified;

    @Schema(description = "当前用户是否已报名", example = "true")
    private Boolean signedUp;

    @Schema(description = "当前用户报名状态：0-待审核，1-已通过，2-已拒绝，3-已取消", example = "1")
    private Integer approvalStatus;

    @Schema(description = "审核备注（拒绝原因）", example = "资料不完整")
    private String approvalRemark;

    @Schema(description = "当前用户是否已签到", example = "false")
    private Boolean checkedIn;

    @Schema(description = "当前用户报名备注信息", example = "姓名: 张三, 联系方式: 13800138000, 备注: 无")
    private String signUpRemark;

    @Schema(description = "自定义报名字段配置JSON", example = "[{\"key\":\"name\",\"label\":\"姓名\",\"type\":\"input\",\"required\":true}]")
    private String customFields;

    // ========== 基于时间动态计算的状态字段 ==========

    @Schema(description = "是否可以报名（当前时间在报名时间范围内且未取消）", example = "true")
    private Boolean canSignUp;

    @Schema(description = "是否可以签到（当前时间在签到时间范围内且未取消）", example = "true")
    private Boolean canCheckIn;

    @Schema(description = "活动是否正在进行中", example = "false")
    private Boolean isOngoing;

    @Schema(description = "活动是否已结束", example = "false")
    private Boolean isEnded;

    @Schema(description = "活动是否已取消", example = "false")
    private Boolean isCancelled;

    @Schema(description = "报名是否已截止（报名结束时间已过）", example = "false")
    private Boolean signUpClosed;

    @Schema(description = "是否显示报名人数", example = "true")
    private Boolean showParticipantCount;

    @Schema(description = "是否隐藏：true-隐藏，false-展示", example = "false")
    private Boolean hidden;

    @Schema(description = "跳转小程序appId", example = "wx1234567890")
    private String redirectAppId;

    @Schema(description = "跳转小程序页面路径", example = "pages/index/index")
    private String redirectAppPath;

    @Schema(description = "跳转小程序名称（按钮显示文案）", example = "打开XX小程序")
    private String redirectAppName;

    @Schema(description = "Whether the activity has a quiz", example = "true")
    private Boolean hasQuiz;

    @Schema(description = "Quiz activity id", example = "1001")
    private Long quizActivityId;

    @Schema(description = "Whether the current user can join the quiz", example = "true")
    private Boolean canJoinQuiz;

    @Schema(description = "Quiz config status", example = "ENABLED")
    private String quizStatus;

    @Schema(description = "Quiz question count", example = "20")
    private Integer quizQuestionCount;

    @Schema(description = "Quiz max attempts", example = "3")
    private Integer quizMaxAttempts;

    @Schema(description = "Quiz attempts already used by the current user", example = "1")
    private Integer quizUsedAttempts;

    @Schema(description = "Quiz duration in seconds", example = "1200")
    private Integer quizDurationSeconds;

    @Schema(description = "Quiz leaderboard size", example = "10")
    private Integer quizLeaderboardSize;

    @Schema(description = "Quiz answer reveal mode", example = "AFTER_SUBMIT")
    private String quizAnswerRevealMode;

    @Schema(description = "Quiz reward rules")
    private List<QuizRewardRuleRespVO> quizRewardRules;

    @Schema(description = "Current user reward progress for this quiz")
    private List<QuizRewardRecordRespVO> quizRewardRecords;

    @Schema(description = "Quiz reward claim guide")
    private String quizRewardClaimGuide;

    @Schema(description = "Current user's default shipping address for physical rewards")
    private QuizDefaultAddressRespVO quizDefaultAddress;

    @Data
    public static class QuizRewardRuleRespVO {

        @Schema(description = "Rank start", example = "1")
        private Integer rankStart;

        @Schema(description = "Rank end", example = "3")
        private Integer rankEnd;

        @Schema(description = "Reward type", example = "PHYSICAL")
        private String rewardType;

        @Schema(description = "Point amount", example = "100")
        private Integer pointAmount;

        @Schema(description = "Reward name", example = "Quiz champion gift box")
        private String rewardName;
    }

    @Data
    public static class QuizRewardRecordRespVO {

        @Schema(description = "Reward type", example = "PHYSICAL")
        private String rewardType;

        @Schema(description = "Point amount", example = "100")
        private Integer pointAmount;

        @Schema(description = "Reward name", example = "Quiz champion gift box")
        private String rewardName;

        @Schema(description = "Reward status", example = "PENDING_FULFILLMENT")
        private String status;

        @Schema(description = "Distributed at")
        private LocalDateTime distributedAt;
    }

    @Data
    public static class QuizDefaultAddressRespVO {

        @Schema(description = "Address id", example = "1")
        private Long id;

        @Schema(description = "Receiver name", example = "张三")
        private String name;

        @Schema(description = "Receiver mobile", example = "13800138000")
        private String mobile;

        @Schema(description = "Detail address", example = "教学楼 A 座 201")
        private String detailAddress;
    }

}

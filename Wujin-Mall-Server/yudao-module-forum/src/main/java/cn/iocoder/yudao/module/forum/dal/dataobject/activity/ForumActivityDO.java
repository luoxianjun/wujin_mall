package cn.iocoder.yudao.module.forum.dal.dataobject.activity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 论坛活动 DO
 *
 * @author forum
 */
@TableName("forum_activity")
@KeySequence("forum_activity_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumActivityDO extends BaseDO {

    /**
     * 活动ID
     */
    @TableId
    private Long id;

    /**
     * 发布人，关联 member_user.id
     */
    private Long userId;

    /**
     * 管理员 memberId 列表，JSON 数组
     */
    private String adminMemberIds;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动封面图
     */
    private String coverImage;

    /**
     * 活动详情图片列表，JSON格式
     */
    private String detailImages;

    /**
     * 活动分类：1-学术讲座，2-文体活动，3-社团活动，4-志愿服务，5-其他
     */
    private Integer category;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 活动地点经度
     */
    private Double longitude;

    /**
     * 活动地点纬度
     */
    private Double latitude;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报名开始时间
     */
    private LocalDateTime signUpStartTime;

    /**
     * 报名结束时间
     */
    private LocalDateTime signUpEndTime;

    /**
     * 签到开始时间
     */
    private LocalDateTime checkInStartTime;

    /**
     * 签到结束时间
     */
    private LocalDateTime checkInEndTime;

    /**
     * 签到距离限制（米）
     */
    private Integer checkInDistance;

    /**
     * 签到方式：1-自助签到；2-定位签到；3-扫码签到
     */
    private Integer checkInType;

    /**
     * 报名人数限制，0表示不限制
     */
    private Integer maxParticipants;

    /**
     * 当前报名人数
     */
    private Integer currentParticipants;

    /**
     * 是否需要审核报名
     */
    private Boolean needApproval;

    /**
     * 是否仅本校可见
     */
    private Boolean schoolOnly;

    /**
     * 活动状态：0-草稿，1-报名中，2-进行中，3-已结束，4-已取消
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否热门：1-是，0-否
     */
    private Integer hot;

    /**
     * 报名是否需要积分
     */
    private Boolean needPoint;

    /**
     * 报名所需积分
     */
    private Integer pointAmount;

    /**
     * 报名要求
     */
    private String requirements;

    /**
     * 是否允许未实名用户报名
     */
    private Boolean allowUnverified;

    /**
     * 自定义报名字段配置，JSON格式
     * 示例: [{"key":"name","label":"姓名","type":"input","required":true},...]
     */
    private String customFields;

    /**
     * 是否显示报名人数
     */
    private Boolean showParticipantCount;

    /**
     * 是否已发送活动开始提醒
     */
    private Boolean startNotified;

    /**
     * 跳转小程序appId
     */
    private String redirectAppId;

    /**
     * 跳转小程序页面路径
     */
    private String redirectAppPath;

    /**
     * 跳转小程序名称（按钮显示文案）
     */
    private String redirectAppName;

    /**
     * 是否隐藏：true-隐藏（不展示在列表），false-展示
     */
    private Boolean hidden;

}

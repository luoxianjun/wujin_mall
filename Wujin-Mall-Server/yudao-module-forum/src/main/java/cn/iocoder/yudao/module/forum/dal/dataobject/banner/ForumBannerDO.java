package cn.iocoder.yudao.module.forum.dal.dataobject.banner;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Banner 配置 DO
 *
 * @author forum
 */
@TableName("forum_banner")
@KeySequence("forum_banner_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumBannerDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * Banner 标题
     */
    private String title;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接
     *
     * 枚举 {@link cn.iocoder.yudao.module.forum.enums.banner.BannerTargetTypeEnum}
     */
    private Integer targetType;

    /**
     * 跳转目标ID（帖子ID/活动ID/用户ID）
     */
    private String targetId;

    /**
     * 跳转链接（外部链接时使用）
     */
    private String targetUrl;

    /**
     * 排序值，越大越靠前
     */
    private Integer sort;

    /**
     * 状态：0=禁用 1=启用
     */
    private Integer status;

    /**
     * 生效时间
     */
    private LocalDateTime startTime;

    /**
     * 失效时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;

}

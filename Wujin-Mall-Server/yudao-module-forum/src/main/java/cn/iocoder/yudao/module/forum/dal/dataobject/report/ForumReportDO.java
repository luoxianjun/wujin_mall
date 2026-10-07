package cn.iocoder.yudao.module.forum.dal.dataobject.report;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛举报 DO
 *
 * @author forum
 */
@TableName("forum_report")
@KeySequence("forum_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumReportDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 举报人用户 ID
     */
    private Long userId;

    /**
     * 举报类型：1 帖子，2 评论，3 用户
     */
    private Integer reportType;

    /**
     * 被举报对象 ID（帖子ID/评论ID/用户ID）
     */
    private Long targetId;

    /**
     * 举报原因类型
     */
    private Integer reasonType;

    /**
     * 举报原因描述
     */
    private String reasonText;

    /**
     * 举报图片，JSON 格式存储
     */
    private String images;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 处理状态：0 待处理，1 已处理，2 已驳回
     */
    private Integer status;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理人 ID
     */
    private Long handleUserId;

}


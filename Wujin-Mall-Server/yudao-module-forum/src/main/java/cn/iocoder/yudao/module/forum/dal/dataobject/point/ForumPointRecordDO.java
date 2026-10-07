package cn.iocoder.yudao.module.forum.dal.dataobject.point;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.forum.enums.point.ForumPointBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛积分记录 DO
 *
 * 一条记录对应一次积分变动。
 */
@TableName("forum_point_record")
@KeySequence("forum_point_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumPointRecordDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户编号，对应 member_user.id
     */
    private Long userId;
    /**
     * 业务编号，如帖子 ID、活动 ID
     */
    private String bizId;
    /**
     * 业务类型
     *
     * 枚举 {@link ForumPointBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 积分标题
     */
    private String title;
    /**
     * 积分描述
     */
    private String description;
    /**
     * 变动积分，正数表示获得，负数表示消耗
     */
    private Integer point;
    /**
     * 变动后的积分余额
     */
    private Integer totalPoint;

}


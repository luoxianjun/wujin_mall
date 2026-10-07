package cn.iocoder.yudao.module.forum.dal.dataobject.sign;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 论坛签到记录 DO
 *
 * @author forum
 */
@TableName("forum_sign_record")
@KeySequence("forum_sign_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumSignRecordDO extends BaseDO {

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
     * 签到日期
     */
    private LocalDate signDate;

    /**
     * 连续签到天数
     */
    private Integer continuousDays;

    /**
     * 获得的积分
     */
    private Integer point;

    /**
     * 签到备注
     */
    private String remark;

}


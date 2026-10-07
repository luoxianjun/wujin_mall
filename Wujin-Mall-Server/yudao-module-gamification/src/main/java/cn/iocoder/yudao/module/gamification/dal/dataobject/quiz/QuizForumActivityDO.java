package cn.iocoder.yudao.module.gamification.dal.dataobject.quiz;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("forum_activity")
@Data
public class QuizForumActivityDO {

    @TableId
    private Long id;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String customFields;
}

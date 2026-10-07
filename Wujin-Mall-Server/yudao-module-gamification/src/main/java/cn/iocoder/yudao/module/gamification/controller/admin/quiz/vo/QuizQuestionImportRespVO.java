package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin quiz import response")
@Data
@Builder
public class QuizQuestionImportRespVO {

    @Schema(description = "Success count", example = "10")
    private Integer successCount;

    @Schema(description = "Failure count", example = "2")
    private Integer failureCount;

    @Schema(description = "Failure messages")
    private List<String> failureMessages;
}

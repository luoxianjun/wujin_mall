package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Admin quiz attempt dynamic export data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptExportData {

    private List<List<String>> head;

    private List<List<Object>> rows;

}

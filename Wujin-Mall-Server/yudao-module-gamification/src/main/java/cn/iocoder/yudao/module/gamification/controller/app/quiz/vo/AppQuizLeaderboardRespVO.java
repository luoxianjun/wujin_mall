package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App quiz leaderboard response")
@Data
public class AppQuizLeaderboardRespVO {

    private Integer leaderboardSize;
    private List<RankItem> rankings;
    private RankItem myRank;

    @Data
    public static class RankItem {
        private Integer rank;
        private Long userId;
        private String nickname;
        private Integer score;
        private Long elapsedMillis;
        private LocalDateTime submittedAt;
    }
}

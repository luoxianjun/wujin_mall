package cn.iocoder.yudao.module.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 论坛模块邮件配置
 *
 * @author codex
 */
@Data
@Component
@ConfigurationProperties("forum.mail")
public class ForumMailProperties {

    /**
     * 是否启用发送邮件
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * 学校邮箱验证邮件配置
     */
    private SchoolEmailProperties schoolEmail = new SchoolEmailProperties();

    @Data
    public static class SchoolEmailProperties {

        /**
         * 邮件主题
         */
        private String subject = "【学生论坛】学校邮箱验证码";

        /**
         * 邮件内容模板，内置变量：${code}、${email}、${minutes}
         */
        private String template = "您好！您的学校邮箱验证码为：${code}，有效期 ${minutes} 分钟。请勿泄露给他人。";

        /**
         * 发件人邮箱（为空则使用 spring.mail.username）
         */
        private String from;

        /**
         * 是否以 HTML 格式发送
         */
        private Boolean html = Boolean.FALSE;
    }

}

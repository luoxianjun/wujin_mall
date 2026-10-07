package cn.iocoder.yudao.framework.security.core.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 简化 APP 场景的登录校验，等价于 {@code @PreAuthorize("isAuthenticated()")}
 *
 * <p>目前论坛等 App 端的控制器只是希望用户完成认证后才允许访问，
 * 使用该注解可以避免每个方法重复编写相同的 SpEL 表达式。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("isAuthenticated()")
public @interface PreAuthenticated {
}

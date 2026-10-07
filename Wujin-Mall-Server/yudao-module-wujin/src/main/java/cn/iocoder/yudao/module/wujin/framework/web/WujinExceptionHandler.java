package cn.iocoder.yudao.module.wujin.framework.web;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 五金模块业务校验异常处理
 *
 * 五金服务使用 {@link IllegalArgumentException} 表达业务校验失败（如必填属性缺失、状态流转不合法），
 * 全局异常处理会将其当作系统异常返回 500，这里仅对五金控制器转换为 400 并保留提示文案。
 */
@RestControllerAdvice(basePackages = "cn.iocoder.yudao.module.wujin.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WujinExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<?> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        log.warn("[illegalArgumentExceptionHandler] {}", ex.getMessage());
        String message = ex.getMessage() == null || ex.getMessage().trim().isEmpty()
                ? BAD_REQUEST.getMsg() : ex.getMessage();
        return CommonResult.error(BAD_REQUEST.getCode(), message);
    }
}

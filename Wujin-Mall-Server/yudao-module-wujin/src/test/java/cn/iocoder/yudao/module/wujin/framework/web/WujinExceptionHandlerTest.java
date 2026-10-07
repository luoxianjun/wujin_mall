package cn.iocoder.yudao.module.wujin.framework.web;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WujinExceptionHandlerTest {

    private final WujinExceptionHandler handler = new WujinExceptionHandler();

    @Test
    void illegalArgumentKeepsBusinessMessageAsBadRequest() {
        CommonResult<?> result = handler.illegalArgumentExceptionHandler(
                new IllegalArgumentException("标准属性「规格型号」为必填项"));

        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("标准属性「规格型号」为必填项", result.getMsg());
    }

    @Test
    void blankMessageFallsBackToBadRequestMessage() {
        CommonResult<?> result = handler.illegalArgumentExceptionHandler(new IllegalArgumentException());

        assertEquals(BAD_REQUEST.getMsg(), result.getMsg());
    }

    @Test
    void adviceOnlyAppliesToWujinControllers() {
        RestControllerAdvice advice = WujinExceptionHandler.class.getAnnotation(RestControllerAdvice.class);

        assertArrayEquals(new String[]{"cn.iocoder.yudao.module.wujin.controller"}, advice.basePackages());
    }
}

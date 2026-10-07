package cn.iocoder.yudao.module.im.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * IM 模块错误码
 *
 * IM 模块，使用 1-120-000-000 段
 *
 * @author codex
 */
public interface ErrorCodeConstants {

    // ========== 腾讯 IM 账号 1-120-001-000 ==========
    ErrorCode TENCENT_IM_CONFIG_INVALID = new ErrorCode(1_120_001_000, "腾讯 IM 配置未正确设置");
    ErrorCode TENCENT_IM_REQUEST_FAILED = new ErrorCode(1_120_001_001, "腾讯 IM 请求失败");
    ErrorCode TENCENT_IM_ACCOUNT_IMPORT_FAILED = new ErrorCode(1_120_001_002, "导入腾讯 IM 账号失败");

}

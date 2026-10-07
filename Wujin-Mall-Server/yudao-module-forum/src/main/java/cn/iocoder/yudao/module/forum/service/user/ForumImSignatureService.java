package cn.iocoder.yudao.module.forum.service.user;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.config.TencentImProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.IM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.IM_USER_SIG_GENERATE_FAIL;

/**
 * 腾讯 IM UserSig 生成服务
 *
 * @author codex
 */
@Service
@Slf4j
public class ForumImSignatureService {

    private static final long DEFAULT_EXPIRE_SECONDS = 604800L;

    @Resource
    private TencentImProperties imProperties;

    /**
     * 生成指定用户的 UserSig
     *
     * @param userId IM 用户 ID
     * @return UserSig 信息
     */
    public ImUserSig generateUserSig(String userId) {
        validateConfig();
        long expireSeconds = getExpireSeconds();
        String sig = genTLSSignature(imProperties.getSdkAppId(), userId, expireSeconds, imProperties.getSecretKey());
        if (StrUtil.isBlank(sig)) {
            log.error("[generateUserSig][生成失败，userId={}]", userId);
            throw ServiceExceptionUtil.exception(IM_USER_SIG_GENERATE_FAIL);
        }
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expireSeconds);
        return new ImUserSig(sig, expireTime);
    }

    /**
     * 判断 UserSig 是否需要刷新
     * 提前 24 小时刷新，避免客户端使用时已过期
     */
    public boolean isExpired(LocalDateTime expireTime) {
        if (expireTime == null) {
            return true;
        }
        // 提前 24 小时刷新，确保客户端有足够的使用时间
        LocalDateTime refreshThreshold = LocalDateTime.now().plusHours(24);
        return expireTime.isBefore(refreshThreshold);
    }

    public boolean isEnabled() {
        log.debug("im enabled: {}, sdkAppId: {}, secretKey: {}", imProperties.getEnabled(), imProperties.getSdkAppId(),
                imProperties.getSecretKey());
        return Boolean.TRUE.equals(imProperties.getEnabled())
                && imProperties.getSdkAppId() != null
                && StrUtil.isNotBlank(imProperties.getSecretKey());
    }

    public Long getSdkAppId() {
        return imProperties.getSdkAppId();
    }

    private long getExpireSeconds() {
        return imProperties.getExpireSeconds() != null ? imProperties.getExpireSeconds() : DEFAULT_EXPIRE_SECONDS;
    }

    private void validateConfig() {
        if (!isEnabled()) {
            log.error("[validateConfig][IM 配置缺失或未启用]");
            throw ServiceExceptionUtil.exception(IM_CONFIG_INVALID);
        }
    }

    private String genTLSSignature(long sdkAppId, String userId, long expire, String secretKey) {
        long currTime = System.currentTimeMillis() / 1000;
        Map<String, Object> sigDoc = new LinkedHashMap<>();
        sigDoc.put("TLS.ver", "2.0");
        sigDoc.put("TLS.identifier", userId);
        sigDoc.put("TLS.sdkappid", sdkAppId);
        sigDoc.put("TLS.expire", expire);
        sigDoc.put("TLS.time", currTime);

        String sig = hmacSha256(sdkAppId, userId, currTime, expire, secretKey);
        sigDoc.put("TLS.sig", sig);

        Deflater compressor = new Deflater();
        compressor.setInput(JsonUtils.toJsonString(sigDoc).getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return base64EncodeUrl(Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength));
    }

    private String hmacSha256(long sdkAppId, String userId, long currTime, long expire, String secretKey) {
        StringBuilder contentToBeSigned = new StringBuilder()
                .append("TLS.identifier:").append(userId).append('\n')
                .append("TLS.sdkappid:").append(sdkAppId).append('\n')
                .append("TLS.time:").append(currTime).append('\n')
                .append("TLS.expire:").append(expire).append('\n');
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(byteSig);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.error("[hmacSha256][签名异常，userId={}]", userId, ex);
            throw ServiceExceptionUtil.exception(IM_USER_SIG_GENERATE_FAIL);
        }
    }

    private String base64EncodeUrl(byte[] input) {
        byte[] base64 = Base64.getEncoder().encode(input);
        for (int i = 0; i < base64.length; i++) {
            if (base64[i] == '+') {
                base64[i] = '*';
            } else if (base64[i] == '/') {
                base64[i] = '-';
            } else if (base64[i] == '=') {
                base64[i] = '_';
            }
        }
        return new String(base64, StandardCharsets.UTF_8);
    }

    @Data
    @AllArgsConstructor
    public static class ImUserSig {
        private String sig;
        private LocalDateTime expireTime;
    }

}

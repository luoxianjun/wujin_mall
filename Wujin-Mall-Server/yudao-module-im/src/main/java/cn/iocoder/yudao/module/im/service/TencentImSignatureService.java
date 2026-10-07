package cn.iocoder.yudao.module.im.service;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.config.TencentImProperties;
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

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_REQUEST_FAILED;

/**
 * 腾讯 IM UserSig 生成服务
 *
 * @author codex
 */
@Service
@Slf4j
public class TencentImSignatureService {

    private static final long DEFAULT_EXPIRE_SECONDS = 604800L;

    @Resource
    private TencentImProperties properties;

    /**
     * 生成指定 identifier 的 UserSig
     *
     * @param identifier IM 账号
     * @return UserSig
     */
    public String generateUserSig(String identifier) {
        validateConfig();
        long expireSeconds = getExpireSeconds();
        String sig = genTLSSignature(properties.getSdkAppId(), identifier, expireSeconds, properties.getSecretKey());
        if (StrUtil.isBlank(sig)) {
            log.error("[generateUserSig][生成失败，identifier={}]", identifier);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
        return sig;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getEnabled())
                && properties.getSdkAppId() != null
                && StrUtil.isNotBlank(properties.getSecretKey())
                && StrUtil.isNotBlank(properties.getAdminIdentifier());
    }

    public Long getSdkAppId() {
        return properties.getSdkAppId();
    }

    public long getExpireSeconds() {
        return properties.getExpireSeconds() != null ? properties.getExpireSeconds() : DEFAULT_EXPIRE_SECONDS;
    }

    public LocalDateTime calculateExpireTime() {
        return LocalDateTime.now().plusSeconds(getExpireSeconds());
    }

    private void validateConfig() {
        if (!isEnabled()) {
            log.error("[validateConfig][IM 配置缺失或未启用]");
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
    }

    private String genTLSSignature(long sdkAppId, String identifier, long expire, String secretKey) {
        long currTime = System.currentTimeMillis() / 1000;
        Map<String, Object> sigDoc = new LinkedHashMap<>();
        sigDoc.put("TLS.ver", "2.0");
        sigDoc.put("TLS.identifier", identifier);
        sigDoc.put("TLS.sdkappid", sdkAppId);
        sigDoc.put("TLS.expire", expire);
        sigDoc.put("TLS.time", currTime);

        String sig = hmacSha256(sdkAppId, identifier, currTime, expire, secretKey);
        sigDoc.put("TLS.sig", sig);

        Deflater compressor = new Deflater();
        compressor.setInput(JsonUtils.toJsonString(sigDoc).getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return base64EncodeUrl(Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength));
    }

    private String hmacSha256(long sdkAppId, String identifier, long currTime, long expire, String secretKey) {
        StringBuilder contentToBeSigned = new StringBuilder()
                .append("TLS.identifier:").append(identifier).append('\n')
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
            log.error("[hmacSha256][签名异常，identifier={}]", identifier, ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
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

}

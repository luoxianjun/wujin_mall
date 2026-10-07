package cn.iocoder.yudao.module.im.client.tencent;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBaseResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBlacklistAddResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBlacklistDeleteResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBlacklistGetResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImProfileSetResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImAccountDeleteResp;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImAccountImportReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImAccountDeleteReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistAddReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistDeleteReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistGetReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImProfileSetReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImSendMsgReq;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImSendMsgResp;
import cn.iocoder.yudao.module.im.config.TencentImProperties;
import cn.iocoder.yudao.module.im.service.TencentImSignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.concurrent.ThreadLocalRandom;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_REQUEST_FAILED;

/**
 * 腾讯 IM 客户端
 *
 * @author codex
 */
@Component
@Slf4j
public class TencentImClient {

    private static final String ACCOUNT_IMPORT_PATH = "/v4/im_open_login_svc/account_import";
    private static final String ACCOUNT_DELETE_PATH = "/v4/im_open_login_svc/account_delete";
    private static final String PROFILE_SET_PATH = "/v4/profile/portrait_set";
    private static final String SEND_MSG_PATH = "/v4/openim/sendmsg";
    private static final String BLACKLIST_ADD_PATH = "/v4/sns/black_list_add";
    private static final String BLACKLIST_DELETE_PATH = "/v4/sns/black_list_delete";
    private static final String BLACKLIST_GET_PATH = "/v4/sns/black_list_get";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private TencentImSignatureService signatureService;

    @Resource
    private TencentImProperties properties;

    public boolean isEnabled() {
        return signatureService.isEnabled();
    }

    public TencentImBaseResp importAccount(TencentImAccountImportReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(ACCOUNT_IMPORT_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImAccountImportReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImBaseResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImBaseResp.class);
            TencentImBaseResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[importAccount][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            if (!body.isSuccess()) {
                log.warn("[importAccount][导入失败，identifier={}, code={}, msg={}]", req.getIdentifier(),
                        body.getErrorCode(), body.getErrorInfo());
            }
            return body;
        } catch (Exception ex) {
            log.error("[importAccount][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    public TencentImProfileSetResp setProfile(TencentImProfileSetReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(PROFILE_SET_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImProfileSetReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImProfileSetResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImProfileSetResp.class);
            TencentImProfileSetResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[setProfile][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            return body;
        } catch (Exception ex) {
            log.error("[setProfile][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    public TencentImAccountDeleteResp deleteAccounts(TencentImAccountDeleteReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(ACCOUNT_DELETE_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImAccountDeleteReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImAccountDeleteResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImAccountDeleteResp.class);
            TencentImAccountDeleteResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[deleteAccounts][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            return body;
        } catch (Exception ex) {
            log.error("[deleteAccounts][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    public TencentImSendMsgResp sendSingleMessage(TencentImSendMsgReq req, String operatorIdentifier) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String identifier = StrUtil.blankToDefault(operatorIdentifier, properties.getAdminIdentifier());
        String adminSig = signatureService.generateUserSig(identifier);
        String url = buildUrl(SEND_MSG_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImSendMsgReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImSendMsgResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImSendMsgResp.class);
            TencentImSendMsgResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[sendSingleMessage][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            return body;
        } catch (Exception ex) {
            log.error("[sendSingleMessage][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    /**
     * 添加黑名单
     *
     * @param req 请求
     * @return 响应
     */
    public TencentImBlacklistAddResp addBlacklist(TencentImBlacklistAddReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(BLACKLIST_ADD_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImBlacklistAddReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImBlacklistAddResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImBlacklistAddResp.class);
            TencentImBlacklistAddResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[addBlacklist][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            if (!body.isSuccess()) {
                log.warn("[addBlacklist][添加黑名单失败，fromAccount={}, code={}, msg={}]", req.getFromAccount(),
                        body.getErrorCode(), body.getErrorInfo());
            }
            return body;
        } catch (Exception ex) {
            log.error("[addBlacklist][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    /**
     * 删除黑名单
     *
     * @param req 请求
     * @return 响应
     */
    public TencentImBlacklistDeleteResp deleteBlacklist(TencentImBlacklistDeleteReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(BLACKLIST_DELETE_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImBlacklistDeleteReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImBlacklistDeleteResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImBlacklistDeleteResp.class);
            TencentImBlacklistDeleteResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[deleteBlacklist][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            if (!body.isSuccess()) {
                log.warn("[deleteBlacklist][删除黑名单失败，fromAccount={}, code={}, msg={}]", req.getFromAccount(),
                        body.getErrorCode(), body.getErrorInfo());
            }
            return body;
        } catch (Exception ex) {
            log.error("[deleteBlacklist][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    /**
     * 获取黑名单列表
     *
     * @param req 请求
     * @return 响应
     */
    public TencentImBlacklistGetResp getBlacklist(TencentImBlacklistGetReq req) {
        if (!signatureService.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        String adminSig = signatureService.generateUserSig(properties.getAdminIdentifier());
        String url = buildUrl(BLACKLIST_GET_PATH, adminSig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TencentImBlacklistGetReq> httpEntity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<TencentImBlacklistGetResp> response = restTemplate.postForEntity(url, httpEntity,
                    TencentImBlacklistGetResp.class);
            TencentImBlacklistGetResp body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                log.error("[getBlacklist][响应异常，status={}, body={}]", response.getStatusCode(), body);
                throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
            }
            if (!body.isSuccess()) {
                log.warn("[getBlacklist][获取黑名单失败，fromAccount={}, code={}, msg={}]", req.getFromAccount(),
                        body.getErrorCode(), body.getErrorInfo());
            }
            return body;
        } catch (Exception ex) {
            log.error("[getBlacklist][请求异常，url={} body={}]", url, StrUtil.nullToEmpty(req.toString()), ex);
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    private String buildUrl(String apiPath, String userSig) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + apiPath)
                .queryParam("sdkappid", properties.getSdkAppId())
                .queryParam("identifier", properties.getAdminIdentifier())
                .queryParam("usersig", userSig)
                .queryParam("random", ThreadLocalRandom.current().nextLong(0, 1L << 32))
                .queryParam("contenttype", "json")
                .toUriString();
    }

}

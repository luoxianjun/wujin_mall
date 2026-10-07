package cn.iocoder.yudao.module.im.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.client.tencent.TencentImClient;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImAccountDeleteResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBaseResp;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImProfileSetResp;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImAccountDeleteReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImAccountImportReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImProfileSetReq;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountDeleteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountDeleteRespVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountImportReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountImportRespVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImProfileSetReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImUserSigRespVO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_REQUEST_FAILED;

/**
 * 腾讯 IM 账号服务
 *
 * @author codex
 */
@Service
@Validated
@Slf4j
public class TencentImAccountService {

    private static final int IMPORT_PAGE_SIZE = 200;
    private static final int MAX_FAILURE_DETAIL = 50;
    private static final String PROFILE_TAG_NICK = "Tag_Profile_IM_Nick";
    private static final String PROFILE_TAG_AVATAR = "Tag_Profile_IM_Image";

    @Resource
    private TencentImClient tencentImClient;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private MemberUserService memberUserService;

    @Resource
    private TencentImSignatureService tencentImSignatureService;

    public TencentImAccountImportRespVO importAccounts(TencentImAccountImportReqVO reqVO) {
        if (!tencentImClient.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        TencentImAccountImportRespVO respVO = new TencentImAccountImportRespVO();
        respVO.setFailures(new ArrayList<>());

        if (Boolean.TRUE.equals(reqVO.getImportAll())) {
            importAllMembers(respVO);
        } else {
            importSpecifiedMembers(reqVO.getUserIds(), respVO);
        }

        if (respVO.getFailures().isEmpty()) {
            respVO.setFailures(null);
        }
        return respVO;
    }

    private void importAllMembers(TencentImAccountImportRespVO respVO) {
        MemberUserPageReqVO pageReqVO = new MemberUserPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(IMPORT_PAGE_SIZE);
        while (true) {
            PageResult<MemberUserDO> pageResult = memberUserService.getUserPage(pageReqVO);
            if (CollUtil.isEmpty(pageResult.getList())) {
                break;
            }
            pageResult.getList().forEach(user ->
                    importSingleUser(user.getId(), user.getNickname(), user.getAvatar(), respVO));
            if (pageResult.getList().size() < pageReqVO.getPageSize()) {
                break;
            }
            pageReqVO.setPageNo(pageReqVO.getPageNo() + 1);
        }
    }

    private void importSpecifiedMembers(List<Long> userIds, TencentImAccountImportRespVO respVO) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<MemberUserRespDTO> users = memberUserApi.getUserList(userIds);
        Map<Long, MemberUserRespDTO> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(users)) {
            for (MemberUserRespDTO user : users) {
                userMap.put(user.getId(), user);
            }
        }
        for (Long userId : userIds) {
            MemberUserRespDTO user = userMap.get(userId);
            if (user == null) {
                recordFailure(respVO, userId, String.valueOf(userId), null, "用户不存在");
                continue;
            }
            importSingleUser(user.getId(), user.getNickname(), user.getAvatar(), respVO);
        }
    }

    private void importSingleUser(Long userId, String nickname, String avatar, TencentImAccountImportRespVO respVO) {
        respVO.setTotal(respVO.getTotal() + 1);

        TencentImAccountImportReq importReq = buildImportReq(userId, nickname, avatar);
        TencentImBaseResp resp = tencentImClient.importAccount(importReq);
        if (resp.isSuccess()) {
            respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            return;
        }
        respVO.setFailedCount(respVO.getFailedCount() + 1);
        recordFailure(respVO, userId, importReq.getIdentifier(), resp.getErrorCode(), resp.getErrorInfo());
    }

    /**
     * 单用户导入：供其他模块创建用户后调用，IM 未启用时自动跳过。
     */
    public void importUserIfEnabled(Long userId, String nickname, String avatar) {
        if (!tencentImClient.isEnabled()) {
            return;
        }
        TencentImAccountImportReq importReq = buildImportReq(userId, nickname, avatar);
        try {
            TencentImBaseResp resp = tencentImClient.importAccount(importReq);
            if (!resp.isSuccess()) {
                log.warn("[importUserIfEnabled][导入用户失败，userId={} identifier={} code={} msg={}]", userId,
                        importReq.getIdentifier(), resp.getErrorCode(), resp.getErrorInfo());
            }
        } catch (Exception ex) {
            log.warn("[importUserIfEnabled][导入用户异常，userId={} identifier={}]", userId, importReq.getIdentifier(), ex);
        }
    }

    private void recordFailure(TencentImAccountImportRespVO respVO, Long userId, String identifier,
                               Integer errorCode, String errorInfo) {
        if (respVO.getFailures().size() >= MAX_FAILURE_DETAIL) {
            return;
        }
        TencentImAccountImportRespVO.FailureItem failureItem = new TencentImAccountImportRespVO.FailureItem();
        failureItem.setUserId(userId);
        failureItem.setIdentifier(identifier);
        failureItem.setErrorCode(errorCode);
        failureItem.setErrorInfo(errorInfo);
        respVO.getFailures().add(failureItem);
        log.warn("[recordFailure][导入用户失败，userId={} identifier={} code={} msg={}]", userId, identifier, errorCode, errorInfo);
    }

    public TencentImUserSigRespVO getCurrentUserSig(Long userId) {
        TencentImUserSigRespVO respVO = new TencentImUserSigRespVO();
        respVO.setUserId(userId);
        respVO.setSdkAppId(tencentImSignatureService.getSdkAppId());
        respVO.setUserSig(tencentImSignatureService.generateUserSig(String.valueOf(userId)));
        respVO.setExpireTime(tencentImSignatureService.calculateExpireTime());
        return respVO;
    }

    private TencentImAccountImportReq buildImportReq(Long userId, String nickname, String avatar) {
        return TencentImAccountImportReq.builder()
                .identifier(String.valueOf(userId))
                .nick(nickname)
                .faceUrl(avatar)
                .build();
    }

    public TencentImAccountDeleteRespVO deleteAccounts(TencentImAccountDeleteReqVO reqVO) {
        if (!tencentImClient.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        TencentImAccountDeleteReq deleteReq = new TencentImAccountDeleteReq();
        deleteReq.setDeleteItems(buildDeleteItems(reqVO.getUserIds()));

        TencentImAccountDeleteResp resp = tencentImClient.deleteAccounts(deleteReq);
        if (!resp.isSuccess()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
        return convertDeleteResp(resp);
    }

    private List<TencentImAccountDeleteReq.DeleteItem> buildDeleteItems(List<Long> userIds) {
        List<TencentImAccountDeleteReq.DeleteItem> items = new ArrayList<>();
        if (CollUtil.isEmpty(userIds)) {
            return items;
        }
        for (Long userId : userIds) {
            items.add(new TencentImAccountDeleteReq.DeleteItem(String.valueOf(userId)));
        }
        return items;
    }

    private TencentImAccountDeleteRespVO convertDeleteResp(TencentImAccountDeleteResp resp) {
        TencentImAccountDeleteRespVO respVO = new TencentImAccountDeleteRespVO();
        List<TencentImAccountDeleteResp.ResultItem> resultItems = resp.getResultItems();
        if (CollUtil.isEmpty(resultItems)) {
            return respVO;
        }
        List<TencentImAccountDeleteRespVO.ResultItem> voItems = new ArrayList<>(resultItems.size());
        long success = 0;
        long failed = 0;
        for (TencentImAccountDeleteResp.ResultItem item : resultItems) {
            TencentImAccountDeleteRespVO.ResultItem voItem = new TencentImAccountDeleteRespVO.ResultItem();
            voItem.setIdentifier(item.getUserId());
            voItem.setResultCode(item.getResultCode());
            voItem.setResultInfo(item.getResultInfo());
            voItem.setUserId(parseLongSafely(item.getUserId()));
            voItems.add(voItem);
            if (item.isSuccess()) {
                success++;
            } else {
                failed++;
            }
        }
        respVO.setTotal(resultItems.size());
        respVO.setSuccessCount(success);
        respVO.setFailedCount(failed);
        respVO.setItems(voItems);
        return respVO;
    }

    private Long parseLongSafely(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setProfile(TencentImProfileSetReqVO reqVO) {
        if (!tencentImClient.isEnabled()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_CONFIG_INVALID);
        }
        TencentImProfileSetReq req = new TencentImProfileSetReq();
        req.setFromAccount(String.valueOf(reqVO.getUserId()));
        req.setProfileItems(buildProfileItems(reqVO));

        TencentImProfileSetResp resp = tencentImClient.setProfile(req);
        if (!resp.isSuccess()) {
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
    }

    private List<TencentImProfileSetReq.ProfileItem> buildProfileItems(TencentImProfileSetReqVO reqVO) {
        List<TencentImProfileSetReq.ProfileItem> items = new ArrayList<>();
        if (CollUtil.isEmpty(reqVO.getItems())) {
            return items;
        }
        reqVO.getItems().forEach(item ->
                items.add(new TencentImProfileSetReq.ProfileItem(item.getTag(), item.getValue())));
        return items;
    }

    /**
     * 其他模块同步用户昵称、头像到 IM 时使用，IM 未启用或参数为空时自动跳过。
     */
    public void setProfileIfEnabled(Long userId, String nickname, String avatar) {
        if (!tencentImClient.isEnabled()) {
            return;
        }
        List<TencentImProfileSetReq.ProfileItem> items = buildProfileItems(nickname, avatar);
        if (CollUtil.isEmpty(items)) {
            return;
        }
        TencentImProfileSetReq req = new TencentImProfileSetReq();
        req.setFromAccount(String.valueOf(userId));
        req.setProfileItems(items);

        try {
            TencentImProfileSetResp resp = tencentImClient.setProfile(req);
            if (!resp.isSuccess()) {
                log.warn("[setProfileIfEnabled][更新用户资料失败，userId={} code={} msg={}]", userId,
                        resp.getErrorCode(), resp.getErrorInfo());
            }
        } catch (Exception ex) {
            log.warn("[setProfileIfEnabled][更新用户资料异常，userId={}]", userId, ex);
        }
    }

    private List<TencentImProfileSetReq.ProfileItem> buildProfileItems(String nickname, String avatar) {
        List<TencentImProfileSetReq.ProfileItem> items = new ArrayList<>();
        if (StrUtil.isNotBlank(nickname)) {
            items.add(new TencentImProfileSetReq.ProfileItem(PROFILE_TAG_NICK, nickname));
        }
        if (StrUtil.isNotBlank(avatar)) {
            items.add(new TencentImProfileSetReq.ProfileItem(PROFILE_TAG_AVATAR, avatar));
        }
        return items;
    }

}

package cn.iocoder.yudao.module.forum.controller.admin.sign;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRecordRespVO;
import cn.iocoder.yudao.module.forum.convert.sign.ForumSignConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.service.sign.ForumSignService;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 签到记录")
@RestController
@RequestMapping("/forum/sign/record")
@Validated
public class AdminSignRecordController {

    @Resource
    private ForumSignService signService;

    @Resource
    private ForumUserProfileService userProfileService;

    @GetMapping("/page")
    @Operation(summary = "分页查询签到记录")
    @PreAuthorize("@ss.hasPermission('forum:sign-record:query')")
    public CommonResult<PageResult<AdminSignRecordRespVO>> getSignRecordPage(@Valid AdminSignRecordPageReqVO reqVO) {
        // 处理 UID 搜索：根据 UID 模糊匹配获取用户 ID 列表
        if (StrUtil.isNotBlank(reqVO.getUid())) {
            List<Long> userIds = userProfileService.getUserIdsByUidLike(reqVO.getUid());
            if (CollectionUtils.isEmpty(userIds)) {
                return success(PageResult.empty());
            }
            // 如果同时指定了 userId，取交集
            if (reqVO.getUserId() != null) {
                if (!userIds.contains(reqVO.getUserId())) {
                    return success(PageResult.empty());
                }
            } else if (userIds.size() == 1) {
                reqVO.setUserId(userIds.get(0));
            }
        }

        PageResult<AdminSignRecordRespVO> pageResult = ForumSignConvert.INSTANCE
                .convertAdminPage(signService.getSignRecordPage(reqVO));
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(pageResult);
        }

        // 使用 forum_user_profile 填充用户信息
        List<ForumUserProfileDO> profiles = userProfileService.getUserProfileListByUserIds(
                convertSet(pageResult.getList(), AdminSignRecordRespVO::getUserId));
        Map<Long, ForumUserProfileDO> profileMap = convertMap(profiles, ForumUserProfileDO::getUserId);
        pageResult.getList().forEach(item -> {
            ForumUserProfileDO profile = profileMap.get(item.getUserId());
            if (profile != null) {
                item.setNickname(profile.getNickname());
                item.setUid(profile.getUid());
            }
        });
        return success(pageResult);
    }

}

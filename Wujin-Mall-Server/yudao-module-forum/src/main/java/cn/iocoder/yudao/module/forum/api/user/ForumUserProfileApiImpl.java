package cn.iocoder.yudao.module.forum.api.user;

import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛用户资料 API 实现
 *
 * @author forum
 */
@Service
public class ForumUserProfileApiImpl implements ForumUserProfileApi {

    @Resource
    @Lazy
    private ForumUserProfileService forumUserProfileService;

    @Override
    public ForumUserProfileDTO getUserProfileByUserId(Long userId) {
        ForumUserProfileDO profile = forumUserProfileService.getUserProfileByUserId(userId);
        if (profile == null) {
            return null;
        }
        return convertToDTO(profile);
    }

    @Override
    public Map<Long, ForumUserProfileDTO> getUserProfileMapByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ForumUserProfileDO> profiles = forumUserProfileService.getUserProfileListByUserIds(userIds);
        Map<Long, ForumUserProfileDTO> result = new HashMap<>();
        for (ForumUserProfileDO profile : profiles) {
            result.put(profile.getUserId(), convertToDTO(profile));
        }
        return result;
    }

    @Override
    public List<Long> getUserIdsByUidLike(String uid) {
        return forumUserProfileService.getUserIdsByUidLike(uid);
    }

    private ForumUserProfileDTO convertToDTO(ForumUserProfileDO profile) {
        ForumUserProfileDTO dto = new ForumUserProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUserId());
        dto.setIsAdmin(profile.getIsAdmin());
        dto.setUid(profile.getUid());
        dto.setNickname(profile.getNickname());
        dto.setAvatar(profile.getAvatar());
        dto.setRealName(profile.getRealName());
        dto.setGender(profile.getGender());
        dto.setPoint(profile.getPoint());
        // 学校认证信息
        dto.setSchoolName(profile.getSchoolName());
        dto.setSchoolEmail(profile.getSchoolEmail());
        dto.setMajor(profile.getMajor());
        dto.setEnrollYear(profile.getEnrollYear());
        dto.setDegree(profile.getDegree());
        dto.setSchoolInfoPublic(profile.getSchoolInfoPublic());
        dto.setSchoolEmailVerified(profile.getSchoolEmailVerified());
        // 扩展信息
        dto.setBirthday(profile.getBirthday());
        dto.setConstellation(profile.getConstellation());
        dto.setMbti(profile.getMbti());
        dto.setIntroduction(profile.getIntroduction());
        // 隐私设置
        dto.setAllowPrivateChat(profile.getAllowPrivateChat());
        dto.setAllowSystemMessage(profile.getAllowSystemMessage());
        dto.setHideSchoolInfo(profile.getHideSchoolInfo());
        return dto;
    }

}

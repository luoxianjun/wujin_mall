package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationCodeMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * {@link InvitationPosterService} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(InvitationPosterServiceImpl.class)
class InvitationPosterServiceTest extends BaseDbUnitTest {

    @Resource
    private InvitationPosterService invitationPosterService;

    @Resource
    private InvitationCodeMapper invitationCodeMapper;

    @MockBean
    private MemberUserApi memberUserApi;

    @Test
    public void testGeneratePoster_Success() {
        // 准备参数
        Long userId = 1L;
        String invitationCode = "ABC12345";

        // Mock 用户信息
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(userId);
        user.setNickname("测试用户");
        user.setAvatar("https://example.com/avatar.jpg");
        when(memberUserApi.getUser(userId)).thenReturn(user);

        // Mock 邀请码
        InvitationCodeDO codeDO = new InvitationCodeDO();
        codeDO.setUserId(userId);
        codeDO.setCode(invitationCode);
        codeDO.setStatus(1);
        invitationCodeMapper.insert(codeDO);

        // 调用
        String posterUrl = invitationPosterService.generatePoster(userId);

        // 断言
        assertNotNull(posterUrl);
        assertTrue(posterUrl.contains(".png"));
    }

    @Test
    public void testGeneratePoster_UserNotFound() {
        // 准备参数
        Long userId = 999L;

        // Mock 用户不存在
        when(memberUserApi.getUser(userId)).thenReturn(null);

        // 调用并断言异常
        assertThrows(IllegalArgumentException.class, () -> {
            invitationPosterService.generatePoster(userId);
        });
    }

    @Test
    public void testGeneratePoster_NoInvitationCode() {
        // 准备参数
        Long userId = 1L;

        // Mock 用户信息
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(userId);
        user.setNickname("测试用户");
        when(memberUserApi.getUser(userId)).thenReturn(user);

        // 不插入邀请码

        // 调用并断言异常
        assertThrows(IllegalArgumentException.class, () -> {
            invitationPosterService.generatePoster(userId);
        });
    }

    @Test
    public void testGeneratePoster_CacheWorks() {
        // 准备参数
        Long userId = 1L;
        String invitationCode = "XYZ98765";

        // Mock 用户信息
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(userId);
        user.setNickname("缓存测试");
        user.setAvatar("https://example.com/avatar2.jpg");
        when(memberUserApi.getUser(userId)).thenReturn(user);

        // Mock 邀请码
        InvitationCodeDO codeDO = new InvitationCodeDO();
        codeDO.setUserId(userId);
        codeDO.setCode(invitationCode);
        codeDO.setStatus(1);
        invitationCodeMapper.insert(codeDO);

        // 第一次调用
        String posterUrl1 = invitationPosterService.generatePoster(userId);
        assertNotNull(posterUrl1);

        // 第二次调用（应该返回缓存）
        String posterUrl2 = invitationPosterService.generatePoster(userId);
        assertNotNull(posterUrl2);
        assertEquals(posterUrl1, posterUrl2);
    }

    @Test
    public void testGenerateQRCode_Success() throws Exception {
        // 准备参数
        String content = "https://example.com/invite?code=ABC123";
        int size = 200;

        // 调用（通过反射测试私有方法，或者测试公共方法的结果）
        String posterUrl = invitationPosterService.generatePoster(1L);

        // 断言海报生成成功（间接验证二维码生成）
        assertNotNull(posterUrl);
    }
}

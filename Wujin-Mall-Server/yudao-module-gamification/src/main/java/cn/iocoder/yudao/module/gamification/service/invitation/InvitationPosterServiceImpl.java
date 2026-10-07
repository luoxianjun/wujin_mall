package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationCodeMapper;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 邀请海报 Service - 模板叠加方式
 */
@Service
@Slf4j
public class InvitationPosterServiceImpl implements InvitationPosterService {

    @Resource
    private InvitationCodeMapper invitationCodeMapper;

    @Resource
    private InvitationCodeService invitationCodeService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ForumUserProfileApi forumUserProfileApi;

    @Resource
    private FileApi fileApi;

    @Resource
    private FileService fileService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private SocialClientApi socialClientApi;

    private static final int QR_CODE_SIZE = 180;
    private static final String CACHE_NAME = "invitationPoster";

    /** 可用的中文字体名称（跨平台兼容） */
    private static final String CJK_FONT_NAME = resolveCjkFontName();

    /** 背景模板缓存 */
    private volatile BufferedImage bgTemplate;

    /**
     * 跨平台解析可用的中文字体
     * Windows: Microsoft YaHei, SimHei, SimSun
     * Linux/Mac: WenQuanYi Micro Hei, Noto Sans CJK SC, Source Han Sans SC, WenQuanYi Zen Hei
     * 兜底: SansSerif（JVM 逻辑字体，始终可用）
     */
    private static String resolveCjkFontName() {
        List<String> candidates = Arrays.asList(
                "Microsoft YaHei", "SimHei", "SimSun",                            // Windows
                "WenQuanYi Micro Hei", "Noto Sans CJK SC", "Source Han Sans SC",  // Linux
                "WenQuanYi Zen Hei", "Droid Sans Fallback",                       // Linux fallback
                "PingFang SC", "Heiti SC", "STHeiti"                              // macOS
        );
        Set<String> available = new java.util.HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        ));
        for (String name : candidates) {
            if (available.contains(name)) {
                log.info("[resolveCjkFontName] 使用中文字体: {}", name);
                return name;
            }
        }
        log.warn("[resolveCjkFontName] 未找到常见中文字体, 可用字体: {}, 使用 SansSerif 兜底", available);
        return "SansSerif";
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'v2:' + #userId", unless = "#result == null")
    public String generatePoster(Long userId) {
        log.info("[generatePoster] userId: {}", userId);

        MemberUserRespDTO memberUser = memberUserApi.getUser(userId);
        if (memberUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        ForumUserProfileDTO forumProfile = forumUserProfileApi.getUserProfileByUserId(userId);

        invitationCodeService.getOrCreateInvitationCode(userId);
        InvitationCodeDO invitationCode = invitationCodeMapper.selectByUserId(userId);
        if (invitationCode == null) {
            throw new IllegalArgumentException("邀请码创建失败");
        }

        try {
            BufferedImage poster = createPosterImage(
                    resolveDisplayValue(forumProfile != null ? forumProfile.getNickname() : null, memberUser.getNickname()),
                    resolveDisplayValue(forumProfile != null ? forumProfile.getAvatar() : null, memberUser.getAvatar()),
                    invitationCode.getCode());

            String tempFileName = "poster_" + userId + "_" + IdUtil.fastSimpleUUID() + ".png";
            File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);
            ImageIO.write(poster, "PNG", tempFile);

            byte[] fileContent = FileUtil.readBytes(tempFile);
            String posterUrl = fileApi.createFile(fileContent, tempFileName, "invitation/poster", "image/png");
            FileUtil.del(tempFile);

            log.info("[generatePoster] 成功 userId: {}, URL: {}", userId, posterUrl);
            return posterUrl;
        } catch (Exception e) {
            log.error("[generatePoster] 失败 userId: {}", userId, e);
            throw new RuntimeException("海报生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "'v2:' + #userId")
    public void clearPosterCache(Long userId) {
        log.info("[clearPosterCache] userId: {}", userId);
    }

    /**
     * 加载背景模板
     */
    private BufferedImage loadBgTemplate() {
        if (bgTemplate != null) {
            return bgTemplate;
        }
        try {
            ClassPathResource resource = new ClassPathResource("invitation/poster_bg.png");
            try (InputStream is = resource.getInputStream()) {
                bgTemplate = ImageIO.read(is);
                log.info("[loadBgTemplate] 加载成功: {}x{}", bgTemplate.getWidth(), bgTemplate.getHeight());
            }
        } catch (Exception e) {
            log.warn("[loadBgTemplate] 加载失败", e);
        }
        return bgTemplate;
    }

    /**
     * 在模板上叠加动态内容生成海报
     * 参考设计稿布局：
     * - 顶部：黄色标题栏 "加入荟星Planet"（模板自带）
     * - 左上：头像 + 昵称 + "邀请你加入"
     * - 中间：小程序码（白色卡片背景）
     * - 底部：专属邀请码（半透明条）
     */
    private BufferedImage createPosterImage(String nickname, String avatarUrl, String invitationCode) throws IOException {
        BufferedImage bg = loadBgTemplate();

        // 海报尺寸 = 模板尺寸，如果没模板就用默认
        int posterW = bg != null ? bg.getWidth() : 750;
        int posterH = bg != null ? bg.getHeight() : 900;

        BufferedImage poster = new BufferedImage(posterW, posterH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = poster.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // ===== 1. 绘制背景模板 =====
        if (bg != null) {
            g2d.drawImage(bg, 0, 0, posterW, posterH, null);
        } else {
            // 无模板时用渐变填充
            GradientPaint gradient = new GradientPaint(0, 0, new Color(102, 126, 234),
                    posterW, posterH, new Color(118, 75, 162));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, posterW, posterH);
        }

        // ===== 2. 头像 + 昵称（左上角，标题栏下方） =====
        // 标题栏大约占顶部 20%，头像放在标题栏下方
        int headerBottom = (int) (posterH * 0.20);

        int avatarSize = 40;
        int avatarX = 30;
        int avatarY = headerBottom + 10;

        // 白色圆形底
        g2d.setColor(Color.WHITE);
        g2d.fillOval(avatarX - 2, avatarY - 2, avatarSize + 4, avatarSize + 4);
        drawAvatar(g2d, avatarUrl, avatarX, avatarY, avatarSize);

        // 昵称
        String displayNickname = StrUtil.blankToDefault(StrUtil.trim(nickname), "用户");
        g2d.setFont(new Font(CJK_FONT_NAME, Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(displayNickname, avatarX + avatarSize + 8, avatarY + 18);

        // 邀请文字
        g2d.setFont(new Font(CJK_FONT_NAME, Font.PLAIN, 13));
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.drawString("邀请你加入", avatarX + avatarSize + 8, avatarY + 36);

        // ===== 3. 小程序码（居中，白色卡片背景） =====
        int qrSize = Math.min(QR_CODE_SIZE, (int)(posterW * 0.38));
        BufferedImage qrCode = getWxMiniProgramCode(invitationCode, qrSize);

        int qrPadding = 12;
        int qrCardW = qrSize + qrPadding * 2;
        int qrCardH = qrSize + qrPadding * 2;
        int qrCardX = (posterW - qrCardW) / 2;
        int qrCardY = (int)(posterH * 0.35);

        // 白色半透明卡片背景
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(qrCardX, qrCardY, qrCardW, qrCardH, 12, 12);
        g2d.drawImage(qrCode, qrCardX + qrPadding, qrCardY + qrPadding, qrSize, qrSize, null);

        // ===== 4. 底部邀请码区域 =====
        int bottomBarH = 36;
        int bottomBarY = qrCardY + qrCardH + 12;
        int bottomBarW = (int)(posterW * 0.6);
        int bottomBarX = (posterW - bottomBarW) / 2;

        // 半透明白色圆角条
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(bottomBarX, bottomBarY, bottomBarW, bottomBarH, bottomBarH, bottomBarH);

        // 邀请码文字
        g2d.setFont(new Font(CJK_FONT_NAME, Font.BOLD, 14));
        g2d.setColor(new Color(60, 60, 60));
        String codeLabel = "专属邀请码";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(codeLabel, bottomBarX + 16, bottomBarY + 24);

        g2d.setFont(new Font(CJK_FONT_NAME, Font.BOLD, 16));
        g2d.setColor(new Color(40, 40, 40));
        FontMetrics fm2 = g2d.getFontMetrics();
        g2d.drawString(invitationCode, bottomBarX + bottomBarW - 16 - fm2.stringWidth(invitationCode), bottomBarY + 25);

        g2d.dispose();
        return poster;
    }

    /**
     * 通过微信官方 API 生成小程序码
     * 使用 wxacode.getUnlimited 接口，扫码可直接打开小程序对应页面
     *
     * 注意：微信 API 的 width 参数最小 280px，建议 430px
     * 生成后在海报绘制时通过 drawImage 缩放到实际需要的尺寸
     *
     * @param invitationCode 邀请码
     * @param size 期望在海报上显示的尺寸（仅用于日志，实际API用430）
     * @return 小程序码图片
     */
    private BufferedImage getWxMiniProgramCode(String invitationCode, int size) throws IOException {
        SocialWxQrcodeReqDTO reqDTO = new SocialWxQrcodeReqDTO();
        // scene 参数：最大32字符，不能包含 &，用于携带邀请码
        String scene = "code=" + invitationCode;
        reqDTO.setScene(scene);
        // 页面路径：不能携带参数（参数放在 scene 里）
        reqDTO.setPath("pages/invitation/register");
        // 微信 API width 最小280，推荐430，生成大图后绘制时再缩放
        reqDTO.setWidth(430);
        reqDTO.setCheckPath(false); // 开发/体验版不检查路径
        reqDTO.setHyaline(false);   // 不需要透明背景（海报有白色卡片底）

        log.info("[getWxMiniProgramCode] 请求微信小程序码, scene: {}, path: {}, width: 430",
                scene, reqDTO.getPath());

        try {
            byte[] qrCodeBytes = socialClientApi.getWxaQrcode(reqDTO);
            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                throw new IOException("微信小程序码返回为空");
            }

            // 检查是否返回了 JSON 错误信息（微信API失败时返回JSON而不是图片）
            if (qrCodeBytes.length < 1000) {
                String possibleError = new String(qrCodeBytes, "UTF-8");
                if (possibleError.contains("errcode") || possibleError.contains("errmsg")) {
                    log.error("[getWxMiniProgramCode] 微信API返回错误: {}", possibleError);
                    throw new IOException("微信API返回错误: " + possibleError);
                }
            }

            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));
            if (qrImage == null) {
                // 尝试打印前100字节帮助调试
                String preview = new String(qrCodeBytes, 0, Math.min(100, qrCodeBytes.length), "UTF-8");
                log.error("[getWxMiniProgramCode] 无法解析为图片, 数据预览: {}, 字节数: {}",
                        preview, qrCodeBytes.length);
                throw new IOException("无法解析微信小程序码图片，返回数据可能不是图片格式");
            }
            log.info("[getWxMiniProgramCode] 小程序码生成成功, invitationCode: {}, 图片大小: {}x{}, 目标展示尺寸: {}",
                    invitationCode, qrImage.getWidth(), qrImage.getHeight(), size);
            return qrImage;
        } catch (Exception e) {
            log.error("[getWxMiniProgramCode] 小程序码生成失败, invitationCode: {}", invitationCode, e);
            throw new IOException("生成小程序码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 绘制圆形头像
     */
    private void drawAvatar(Graphics2D g2d, String avatarUrl, int x, int y, int size) {
        BufferedImage avatarImage = loadAvatarImage(avatarUrl);

        if (avatarImage != null) {
            try {
                BufferedImage scaled = Thumbnails.of(avatarImage).size(size, size).asBufferedImage();
                BufferedImage circle = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circle.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                g2.drawImage(scaled, 0, 0, null);
                g2.dispose();
                g2d.drawImage(circle, x, y, null);
            } catch (Exception e) {
                log.warn("[drawAvatar] 头像处理失败", e);
                drawDefaultAvatar(g2d, x, y, size);
            }
        } else {
            drawDefaultAvatar(g2d, x, y, size);
        }
    }

    private BufferedImage loadAvatarImage(String avatarUrl) {
        if (StrUtil.isBlank(avatarUrl)) {
            return null;
        }

        BufferedImage storedAvatarImage = loadAvatarFromStorage(avatarUrl);
        if (storedAvatarImage != null) {
            return storedAvatarImage;
        }

        for (String candidateUrl : buildAvatarCandidateUrls(avatarUrl)) {
            try {
                byte[] avatarBytes = HttpUtil.downloadBytes(candidateUrl);
                BufferedImage avatarImage = ImageIO.read(new ByteArrayInputStream(avatarBytes));
                if (avatarImage != null) {
                    return avatarImage;
                }
            } catch (Exception e) {
                log.debug("[loadAvatarImage] 头像下载失败: {}", candidateUrl, e);
            }
        }

        log.warn("[loadAvatarImage] 头像加载失败，使用默认头像: {}", avatarUrl);
        return null;
    }

    private String resolveDisplayValue(String forumValue, String memberValue) {
        return StrUtil.blankToDefault(StrUtil.trim(forumValue), StrUtil.trim(memberValue));
    }

    private BufferedImage loadAvatarFromStorage(String avatarUrl) {
        try {
            String normalizedUrl = HttpUtils.removeUrlQuery(avatarUrl.trim());
            FileDO file = fileMapper.selectFirstOne(FileDO::getUrl, normalizedUrl);
            if (file == null) {
                return null;
            }
            byte[] content = fileService.getFileContent(file.getConfigId(), file.getPath());
            if (content == null || content.length == 0) {
                return null;
            }
            return ImageIO.read(new ByteArrayInputStream(content));
        } catch (Exception e) {
            log.debug("[loadAvatarFromStorage] 从文件存储加载头像失败: {}", avatarUrl, e);
            return null;
        }
    }

    private List<String> buildAvatarCandidateUrls(String avatarUrl) {
        Set<String> urls = new LinkedHashSet<>();
        String url = avatarUrl.trim();

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                urls.add(fileApi.presignGetUrl(url, 300));
            } catch (UnsupportedOperationException ignored) {
                // 本地/FTP 等存储不支持预签名，直接走原始 URL
            } catch (Exception e) {
                log.debug("[buildAvatarCandidateUrls] 头像预签名失败: {}", url, e);
            }
            urls.add(url);
        } else if (url.startsWith("//")) {
            urls.add("https:" + url);
            urls.add("http:" + url);
        } else if (url.startsWith("/")) {
            urls.add(url);
        } else {
            urls.add("https://" + url);
            urls.add("http://" + url);
        }

        return new ArrayList<>(urls);
    }

    /**
     * 默认灰色圆形头像
     */
    private void drawDefaultAvatar(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(new Color(200, 200, 210));
        g2d.fillOval(x, y, size, size);
        // 简单人形轮廓
        g2d.setColor(Color.WHITE);
        int headR = size / 4;
        g2d.fillOval(x + (size - headR * 2) / 2, y + size / 5, headR * 2, headR * 2);
        int bodyW = (int)(size * 0.5);
        int bodyH = size / 5;
        g2d.fillRoundRect(x + (size - bodyW) / 2, y + size / 5 + headR * 2 + 2, bodyW, bodyH, bodyW / 2, bodyW / 2);
    }
}

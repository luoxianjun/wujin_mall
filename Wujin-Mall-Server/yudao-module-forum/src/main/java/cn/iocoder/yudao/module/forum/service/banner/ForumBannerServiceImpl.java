package cn.iocoder.yudao.module.forum.service.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerPageReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerUpdateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.banner.vo.AppBannerRespVO;
import cn.iocoder.yudao.module.forum.convert.banner.ForumBannerConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.banner.ForumBannerDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivityMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.banner.ForumBannerMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.BANNER_NOT_EXISTS;

/**
 * Banner 配置 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
public class ForumBannerServiceImpl implements ForumBannerService {

    @Resource
    private ForumBannerMapper bannerMapper;

    @Resource
    private ForumActivityMapper activityMapper;

    @Resource
    private ForumPostMapper forumPostMapper;

    /**
     * Banner 时间格式
     */
    private static final DateTimeFormatter BANNER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Long createBanner(AdminBannerCreateReqVO createReqVO) {
        // 设置默认值
        if (createReqVO.getSort() == null) {
            createReqVO.setSort(0);
        }
        if (createReqVO.getStatus() == null) {
            createReqVO.setStatus(1); // 默认启用
        }
        // 插入（时间字段手动解析）
        ForumBannerDO banner = ForumBannerConvert.INSTANCE.convert(createReqVO);
        banner.setStartTime(parseDateTime(createReqVO.getStartTime()));
        banner.setEndTime(parseDateTime(createReqVO.getEndTime()));
        bannerMapper.insert(banner);
        return banner.getId();
    }

    @Override
    public void updateBanner(AdminBannerUpdateReqVO updateReqVO) {
        // 校验存在
        validateBannerExists(updateReqVO.getId());
        // 更新（时间字段手动解析）
        ForumBannerDO updateObj = ForumBannerConvert.INSTANCE.convert(updateReqVO);
        updateObj.setStartTime(parseDateTime(updateReqVO.getStartTime()));
        updateObj.setEndTime(parseDateTime(updateReqVO.getEndTime()));
        bannerMapper.updateById(updateObj);
    }

    @Override
    public void deleteBanner(Long id) {
        // 校验存在
        validateBannerExists(id);
        // 删除
        bannerMapper.deleteById(id);
    }

    @Override
    public ForumBannerDO getBanner(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public PageResult<ForumBannerDO> getBannerPage(AdminBannerPageReqVO pageReqVO) {
        return bannerMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateBannerStatus(Long id, Integer status) {
        // 校验存在
        validateBannerExists(id);
        // 更新状态
        bannerMapper.updateById(ForumBannerDO.builder().id(id).status(status).build());
    }

    @Override
    public List<ForumBannerDO> getActiveBannerList() {
        return bannerMapper.selectActiveList();
    }

    @Override
    public List<AppBannerRespVO> getActiveBannerListWithDetails() {
        // 1. 获取有效的 Banner 列表
        List<ForumBannerDO> banners = bannerMapper.selectActiveList();

        // 2. 转换为 VO 并填充详细信息
        return banners.stream()
                .map(this::buildBannerRespVO)
                .collect(Collectors.toList());
    }

    /**
     * 构建 Banner 响应 VO，包含活动和帖子的详细信息
     */
    private AppBannerRespVO buildBannerRespVO(ForumBannerDO banner) {
        AppBannerRespVO respVO = ForumBannerConvert.INSTANCE.convertApp(banner);

        // 根据 targetType 填充详细信息
        Integer targetType = banner.getTargetType();
        String targetId = banner.getTargetId();

        if (targetType == null || targetId == null) {
            return respVO;
        }

        try {
            Long id = Long.parseLong(targetId);

            if (targetType == 2) {
                // 活动类型：查询活动详细信息
                ForumActivityDO activity = activityMapper.selectById(id);
                if (activity != null) {
                    respVO.setActivityTitle(activity.getTitle());
                    respVO.setActivityRequirements(activity.getRequirements());
                    // 活动奖励：如果有积分奖励，则显示
                    if (Boolean.TRUE.equals(activity.getNeedPoint()) && activity.getPointAmount() != null && activity.getPointAmount() > 0) {
                        respVO.setActivityReward(activity.getPointAmount() + "积分");
                    }
                    // 活动时间
                    if (activity.getStartTime() != null && activity.getEndTime() != null) {
                        respVO.setActivityTime(formatActivityTime(activity.getStartTime(), activity.getEndTime()));
                    } else if (activity.getStartTime() != null) {
                        respVO.setActivityTime(formatDateTime(activity.getStartTime()));
                    } else if (activity.getEndTime() != null) {
                        respVO.setActivityTime(formatDateTime(activity.getEndTime()));
                    }
                    // 活动地点
                    respVO.setActivityLocation(activity.getLocation());
                }
            } else if (targetType == 1) {
                // 帖子类型：查询帖子详细信息
                ForumPostDO post = forumPostMapper.selectById(id);
                if (post != null) {
                    respVO.setPostTitle(post.getTitle());
                }
            }
        } catch (NumberFormatException e) {
            // targetId 不是数字，忽略
        }

        return respVO;
    }

    /**
     * 格式化活动时间
     */
    private String formatActivityTime(LocalDateTime startTime, LocalDateTime endTime) {
        String start = formatDateTime(startTime);
        String end = formatDateTime(endTime);
        if (start.equals(end)) {
            return start;
        }
        return start + " - " + end;
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void validateBannerExists(Long id) {
        if (bannerMapper.selectById(id) == null) {
            throw exception(BANNER_NOT_EXISTS);
        }
    }

    /**
     * 解析前端传入的时间字符串
     */
    private LocalDateTime parseDateTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        return LocalDateTime.parse(time, BANNER_TIME_FORMATTER);
    }

}

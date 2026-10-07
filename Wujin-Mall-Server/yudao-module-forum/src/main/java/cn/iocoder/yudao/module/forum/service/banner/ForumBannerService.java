package cn.iocoder.yudao.module.forum.service.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerPageReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerUpdateReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.banner.ForumBannerDO;

import javax.validation.Valid;
import java.util.List;

/**
 * Banner 配置 Service 接口
 *
 * @author forum
 */
public interface ForumBannerService {

    /**
     * 创建 Banner
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBanner(@Valid AdminBannerCreateReqVO createReqVO);

    /**
     * 更新 Banner
     *
     * @param updateReqVO 更新信息
     */
    void updateBanner(@Valid AdminBannerUpdateReqVO updateReqVO);

    /**
     * 删除 Banner
     *
     * @param id 编号
     */
    void deleteBanner(Long id);

    /**
     * 获取 Banner
     *
     * @param id 编号
     * @return Banner
     */
    ForumBannerDO getBanner(Long id);

    /**
     * 分页查询 Banner（管理端）
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    PageResult<ForumBannerDO> getBannerPage(AdminBannerPageReqVO pageReqVO);

    /**
     * 更新 Banner 状态
     *
     * @param id     编号
     * @param status 状态
     */
    void updateBannerStatus(Long id, Integer status);

    /**
     * 获取有效的 Banner 列表（App 端）
     *
     * @return Banner 列表
     */
    List<ForumBannerDO> getActiveBannerList();

    /**
     * 获取有效的 Banner 列表（App 端），包含活动和帖子的详细信息
     *
     * @return Banner 列表（带详细信息）
     */
    List<cn.iocoder.yudao.module.forum.controller.app.banner.vo.AppBannerRespVO> getActiveBannerListWithDetails();

}

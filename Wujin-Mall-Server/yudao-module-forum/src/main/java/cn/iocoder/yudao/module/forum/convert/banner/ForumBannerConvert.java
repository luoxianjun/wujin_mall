package cn.iocoder.yudao.module.forum.convert.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerUpdateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.banner.vo.AppBannerRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.banner.ForumBannerDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Banner 配置 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumBannerConvert {

    ForumBannerConvert INSTANCE = Mappers.getMapper(ForumBannerConvert.class);

    /**
     * 创建请求 VO -> DO
     * <p>
     * 生效/失效时间在 Service 中手动转换，避免 String 与 LocalDateTime 的类型冲突
     */
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    ForumBannerDO convert(AdminBannerCreateReqVO bean);

    /**
     * 更新请求 VO -> DO
     * <p>
     * 生效/失效时间在 Service 中手动转换，避免 String 与 LocalDateTime 的类型冲突
     */
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    ForumBannerDO convert(AdminBannerUpdateReqVO bean);

    AdminBannerRespVO convert(ForumBannerDO bean);

    List<AdminBannerRespVO> convertList(List<ForumBannerDO> list);

    PageResult<AdminBannerRespVO> convertPage(PageResult<ForumBannerDO> page);

    AppBannerRespVO convertApp(ForumBannerDO bean);

    List<AppBannerRespVO> convertAppList(List<ForumBannerDO> list);

}

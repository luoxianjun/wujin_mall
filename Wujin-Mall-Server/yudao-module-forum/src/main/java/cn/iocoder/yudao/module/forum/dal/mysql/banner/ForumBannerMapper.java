package cn.iocoder.yudao.module.forum.dal.mysql.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.AdminBannerPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.banner.ForumBannerDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Banner 配置 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumBannerMapper extends BaseMapperX<ForumBannerDO> {

    /**
     * 分页查询 Banner（管理端）
     */
    default PageResult<ForumBannerDO> selectPage(AdminBannerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumBannerDO>()
                .likeIfPresent(ForumBannerDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ForumBannerDO::getTargetType, reqVO.getTargetType())
                .eqIfPresent(ForumBannerDO::getStatus, reqVO.getStatus())
                .orderByDesc(ForumBannerDO::getSort)
                .orderByDesc(ForumBannerDO::getId));
    }

    /**
     * 查询有效的 Banner 列表（App 端）
     * 条件：status=1 AND (start_time IS NULL OR start_time <= NOW())
     *       AND (end_time IS NULL OR end_time >= NOW())
     * 排序：sort DESC, id DESC
     */
    default List<ForumBannerDO> selectActiveList() {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<ForumBannerDO>()
                .eq(ForumBannerDO::getStatus, 1)
                .and(wrapper -> wrapper
                        .isNull(ForumBannerDO::getStartTime)
                        .or()
                        .le(ForumBannerDO::getStartTime, now))
                .and(wrapper -> wrapper
                        .isNull(ForumBannerDO::getEndTime)
                        .or()
                        .ge(ForumBannerDO::getEndTime, now))
                .orderByDesc(ForumBannerDO::getSort)
                .orderByDesc(ForumBannerDO::getId));
    }

}

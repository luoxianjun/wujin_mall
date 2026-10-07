package cn.iocoder.yudao.module.forum.convert.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySignUpRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivitySignUpDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 论坛活动 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumActivityConvert {

    ForumActivityConvert INSTANCE = Mappers.getMapper(ForumActivityConvert.class);

    @Mapping(target = "detailImages", expression = "java(convertDetailImages(bean.getDetailImages()))")
    @Mapping(target = "adminMemberIds", ignore = true)
    @Mapping(target = "adminMemberNicknames", ignore = true)
    @Mapping(target = "hidden", ignore = true)
    AppActivityRespVO convert(ForumActivityDO bean);

    PageResult<AppActivityRespVO> convertPage(PageResult<ForumActivityDO> page);

    AppActivitySignUpRespVO convertSignUp(ForumActivitySignUpDO bean);

    PageResult<AppActivitySignUpRespVO> convertSignUpPage(PageResult<ForumActivitySignUpDO> page);

    default List<String> convertDetailImages(String detailImages) {
        return JsonUtils.parseArray(detailImages, String.class);
    }

}

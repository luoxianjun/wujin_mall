package cn.iocoder.yudao.module.forum.convert.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 论坛帖子 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumPostConvert {

    ForumPostConvert INSTANCE = Mappers.getMapper(ForumPostConvert.class);

    // categories 由 service 单独解析（兼容单选/多选），此处忽略类型不匹配的自动映射
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "imageUrls", expression = "java(convertImageUrls(bean.getImageUrls()))")
    AppPostRespVO convert(ForumPostDO bean);

    PageResult<AppPostRespVO> convertPage(PageResult<ForumPostDO> page);

    AppCommentRespVO convertComment(ForumCommentDO bean);

    List<AppCommentRespVO> convertCommentList(List<ForumCommentDO> list);

    default List<String> convertImageUrls(String imageUrls) {
        return JsonUtils.parseArray(imageUrls, String.class);
    }

}

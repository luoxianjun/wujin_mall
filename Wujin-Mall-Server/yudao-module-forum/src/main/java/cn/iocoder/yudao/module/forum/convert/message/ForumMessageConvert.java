package cn.iocoder.yudao.module.forum.convert.message;

import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppSystemNoticeRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumMessageDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemNoticeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 论坛消息 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumMessageConvert {

    ForumMessageConvert INSTANCE = Mappers.getMapper(ForumMessageConvert.class);

    AppMessageRespVO convert(ForumMessageDO bean);

    AppSystemNoticeRespVO convertNotice(ForumSystemNoticeDO bean);

}


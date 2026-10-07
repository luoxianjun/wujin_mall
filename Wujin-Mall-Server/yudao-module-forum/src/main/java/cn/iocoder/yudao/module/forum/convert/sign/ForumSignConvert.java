package cn.iocoder.yudao.module.forum.convert.sign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRecordRespVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 论坛签到 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumSignConvert {

    ForumSignConvert INSTANCE = Mappers.getMapper(ForumSignConvert.class);

    AppSignRespVO convert(ForumSignRecordDO bean);

    PageResult<AppSignRespVO> convertPage(PageResult<ForumSignRecordDO> page);

    AdminSignRecordRespVO convertAdmin(ForumSignRecordDO bean);

    PageResult<AdminSignRecordRespVO> convertAdminPage(PageResult<ForumSignRecordDO> page);

}

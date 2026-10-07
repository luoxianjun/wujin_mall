package cn.iocoder.yudao.module.forum.convert.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.point.ForumPointRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 积分记录 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumPointConvert {

    ForumPointConvert INSTANCE = Mappers.getMapper(ForumPointConvert.class);

    AdminPointRecordRespVO convertAdmin(ForumPointRecordDO bean);

    PageResult<AdminPointRecordRespVO> convertAdminPage(PageResult<ForumPointRecordDO> page);

}

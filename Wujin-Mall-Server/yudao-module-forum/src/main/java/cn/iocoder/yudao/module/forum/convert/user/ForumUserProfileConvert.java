package cn.iocoder.yudao.module.forum.convert.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.AppForumUserPageRespVO;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.AppUserProfileRespVO;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.AppUserProfileWithImRespVO;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.AppUserStatisticsRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 论坛用户资料 Convert
 *
 * @author forum
 */
@Mapper
public interface ForumUserProfileConvert {

    ForumUserProfileConvert INSTANCE = Mappers.getMapper(ForumUserProfileConvert.class);

    @Mapping(source = "imUserSig", target = "userSig")
    AppUserProfileRespVO convert(ForumUserProfileDO bean);

    AppUserProfileWithImRespVO convertWithIm(ForumUserProfileDO bean);

    AppUserStatisticsRespVO convertToStatistics(ForumUserProfileDO bean);

    default PageResult<AppForumUserPageRespVO> convertPage(PageResult<MemberUserRespDTO> pageResult,
            Function<MemberUserRespDTO, ForumUserProfileDO> profileFunction) {
        PageResult<AppForumUserPageRespVO> result = new PageResult<>(pageResult.getTotal());
        result.setList(convertList(pageResult.getList(), user -> {
            ForumUserProfileDO profile = profileFunction.apply(user);
            AppForumUserPageRespVO respVO = new AppForumUserPageRespVO();
            respVO.setUserId(user.getId());
            // 优先使用论坛昵称和头像，若为空则回退到会员昵称和头像
            respVO.setNickname(profile.getNickname() != null ? profile.getNickname() : user.getNickname());
            respVO.setAvatar(profile.getAvatar() != null ? profile.getAvatar() : user.getAvatar());
            respVO.setImUserSig(profile.getImUserSig());
            return respVO;
        }));
        return result;
    }

    default PageResult<AppForumUserPageRespVO> convertProfilePage(PageResult<ForumUserProfileDO> pageResult) {
        PageResult<AppForumUserPageRespVO> result = new PageResult<>(pageResult.getTotal());
        result.setList(convertList(pageResult.getList(), profile -> {
            AppForumUserPageRespVO respVO = new AppForumUserPageRespVO();
            respVO.setUserId(profile.getUserId());
            respVO.setNickname(profile.getNickname());
            respVO.setAvatar(profile.getAvatar());
            respVO.setImUserSig(profile.getImUserSig());
            return respVO;
        }));
        return result;
    }

}

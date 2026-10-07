package cn.iocoder.yudao.module.forum.dal.mysql.user;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛用户扩展信息 Mapper
 */
@Mapper
public interface ForumUserProfileMapper extends BaseMapperX<ForumUserProfileDO> {

    /**
     * 根据用户 ID 查询
     */
    default ForumUserProfileDO selectByUserId(Long userId) {
        return selectOne(ForumUserProfileDO::getUserId, userId);
    }

    /**
     * 根据 UID 查询
     */
    default ForumUserProfileDO selectByUid(String uid) {
        return selectOne(ForumUserProfileDO::getUid, uid);
    }

    /**
     * 分页查询，支持按论坛昵称模糊搜索
     */
    default PageResult<ForumUserProfileDO> selectPage(PageParam pageParam, String nickname) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ForumUserProfileDO>()
                .likeIfPresent(ForumUserProfileDO::getNickname, nickname)
                .orderByDesc(ForumUserProfileDO::getId));
    }

}


package cn.iocoder.yudao.module.forum.dal.mysql.sign;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignPeriodStatDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛签到周期统计 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumSignPeriodStatMapper extends BaseMapperX<ForumSignPeriodStatDO> {

    default ForumSignPeriodStatDO selectByUserAndPeriod(Long userId, Integer periodType, String periodKey) {
        return selectOne(new LambdaQueryWrapperX<ForumSignPeriodStatDO>()
                .eq(ForumSignPeriodStatDO::getUserId, userId)
                .eq(ForumSignPeriodStatDO::getPeriodType, periodType)
                .eq(ForumSignPeriodStatDO::getPeriodKey, periodKey));
    }
}


package cn.iocoder.yudao.module.forum.dal.mysql.sign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRecordPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

/**
 * 论坛签到记录 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumSignRecordMapper extends BaseMapperX<ForumSignRecordDO> {

    /**
     * 根据用户ID和签到日期查询
     */
    default ForumSignRecordDO selectByUserIdAndDate(Long userId, LocalDate signDate) {
        return selectOne(new LambdaQueryWrapperX<ForumSignRecordDO>()
                .eq(ForumSignRecordDO::getUserId, userId)
                .eq(ForumSignRecordDO::getSignDate, signDate));
    }

    /**
     * 查询用户最近的签到记录
     */
    default ForumSignRecordDO selectLatestByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumSignRecordDO>()
                .eq(ForumSignRecordDO::getUserId, userId)
                .orderByDesc(ForumSignRecordDO::getSignDate)
                .last("LIMIT 1"));
    }

    /**
     * 分页查询用户签到记录
     */
    default PageResult<ForumSignRecordDO> selectPage(AppSignRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumSignRecordDO>()
                .eqIfPresent(ForumSignRecordDO::getUserId, reqVO.getUserId())
                .betweenIfPresent(ForumSignRecordDO::getSignDate, reqVO.getStartDate(), reqVO.getEndDate())
                .orderByDesc(ForumSignRecordDO::getSignDate));
    }

    /**
     * 查询用户在指定日期范围内的签到记录（含边界），按日期升序
     */
    default java.util.List<ForumSignRecordDO> selectListByUserIdBetweenDates(Long userId, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<ForumSignRecordDO>()
                .eq(ForumSignRecordDO::getUserId, userId)
                .between(ForumSignRecordDO::getSignDate, startDate, endDate)
                .orderByAsc(ForumSignRecordDO::getSignDate));
    }

}

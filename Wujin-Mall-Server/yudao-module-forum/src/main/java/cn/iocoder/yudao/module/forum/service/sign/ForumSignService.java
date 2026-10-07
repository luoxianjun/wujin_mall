package cn.iocoder.yudao.module.forum.service.sign;

import java.time.LocalDate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRespVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignStatusRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRecordDO;

/**
 * 论坛签到 Service 接口
 *
 * @author forum
 */
public interface ForumSignService {

    /**
     * 用户签到
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    AppSignRespVO sign(Long userId,boolean isTest,LocalDate date);

    /**
     * 获取签到状态
     *
     * @param userId 用户ID
     * @return 签到状态
     */
    AppSignStatusRespVO getSignStatus(Long userId);

    /**
     * 分页查询签到记录
     *
     * @param reqVO 分页请求
     * @return 签到记录分页
     */
    PageResult<ForumSignRecordDO> getSignRecordPage(AppSignRecordPageReqVO reqVO);

}


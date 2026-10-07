package cn.iocoder.yudao.module.forum.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemBroadcastDO;

import javax.validation.Valid;

/**
 * 系统广播消息 Service 接口
 *
 * @author forum
 */
public interface ForumSystemBroadcastService {

    /**
     * 发送系统广播消息给所有用户
     *
     * @param senderId 发送者用户ID
     * @param reqVO    请求参数
     * @return 广播记录ID
     */
    Long broadcast(Long senderId, @Valid AdminSystemBroadcastReqVO reqVO);

    /**
     * 分页查询广播消息历史
     *
     * @param reqVO 分页参数
     * @return 广播消息列表
     */
    PageResult<AdminSystemBroadcastRespVO> getBroadcastPage(PageParam reqVO);

    /**
     * 获取广播消息详情
     *
     * @param id 消息ID
     * @return 广播消息
     */
    AdminSystemBroadcastRespVO getBroadcast(Long id);

}

package cn.iocoder.yudao.module.forum.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemBroadcastDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.message.ForumSystemBroadcastMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.im.service.TencentImMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统广播消息 Service 实现
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumSystemBroadcastServiceImpl implements ForumSystemBroadcastService {

    @Resource
    private ForumSystemBroadcastMapper broadcastMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private TencentImMessageService tencentImMessageService;

    @Override
    public Long broadcast(Long senderId, AdminSystemBroadcastReqVO reqVO) {
        // 1. 创建广播记录
        ForumSystemBroadcastDO broadcast = ForumSystemBroadcastDO.builder()
                .title(reqVO.getTitle())
                .content(reqVO.getContent())
                .senderId(senderId)
                .successCount(0)
                .failCount(0)
                .status(0) // 发送中
                .build();
        broadcastMapper.insert(broadcast);

        // 2. 异步发送消息给所有用户
        sendToAllUsersAsync(broadcast.getId(), reqVO.getContent());

        return broadcast.getId();
    }

    @Async
    public void sendToAllUsersAsync(Long broadcastId, String content) {
        int successCount = 0;
        int failCount = 0;

        try {
            // 查询所有用户
            List<ForumUserProfileDO> users = userProfileMapper.selectList();

            for (ForumUserProfileDO user : users) {
                try {
                    // 使用管理员账号发送消息
                    String toAccount = String.valueOf(user.getUserId());
                    tencentImMessageService.sendAdminTextMessage(toAccount, content);
                    successCount++;
                } catch (Exception e) {
                    log.warn("[sendToAllUsersAsync][发送消息给用户{}失败]", user.getUserId(), e);
                    failCount++;
                }
            }

            // 更新广播记录状态
            ForumSystemBroadcastDO updateObj = ForumSystemBroadcastDO.builder()
                    .id(broadcastId)
                    .successCount(successCount)
                    .failCount(failCount)
                    .status(1) // 发送完成
                    .build();
            broadcastMapper.updateById(updateObj);

        } catch (Exception e) {
            log.error("[sendToAllUsersAsync][广播消息发送异常 broadcastId={}]", broadcastId, e);
            ForumSystemBroadcastDO updateObj = ForumSystemBroadcastDO.builder()
                    .id(broadcastId)
                    .successCount(successCount)
                    .failCount(failCount)
                    .status(2) // 发送失败
                    .build();
            broadcastMapper.updateById(updateObj);
        }
    }

    @Override
    public PageResult<AdminSystemBroadcastRespVO> getBroadcastPage(PageParam reqVO) {
        PageResult<ForumSystemBroadcastDO> pageResult = broadcastMapper.selectPage(reqVO);
        return new PageResult<>(
                pageResult.getList().stream().map(this::convert).collect(Collectors.toList()),
                pageResult.getTotal());
    }

    @Override
    public AdminSystemBroadcastRespVO getBroadcast(Long id) {
        ForumSystemBroadcastDO broadcast = broadcastMapper.selectById(id);
        return broadcast != null ? convert(broadcast) : null;
    }

    private AdminSystemBroadcastRespVO convert(ForumSystemBroadcastDO broadcast) {
        AdminSystemBroadcastRespVO respVO = new AdminSystemBroadcastRespVO();
        respVO.setId(broadcast.getId());
        respVO.setTitle(broadcast.getTitle());
        respVO.setContent(broadcast.getContent());
        respVO.setSenderId(broadcast.getSenderId());
        respVO.setSuccessCount(broadcast.getSuccessCount());
        respVO.setFailCount(broadcast.getFailCount());
        respVO.setStatus(broadcast.getStatus());
        respVO.setCreateTime(broadcast.getCreateTime());
        return respVO;
    }

}

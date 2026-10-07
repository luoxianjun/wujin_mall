package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.monitor.WujinSearchBehaviorLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinSearchBehaviorLogAdminServiceImpl implements WujinSearchBehaviorLogAdminService {

    @Resource
    private WujinSearchBehaviorLogMapper behaviorLogMapper;

    @Override
    public Long createBehaviorLog(WujinSearchBehaviorLogSaveReqVO createReqVO) {
        WujinSearchBehaviorLogDO behaviorLog = BeanUtils.toBean(createReqVO, WujinSearchBehaviorLogDO.class);
        behaviorLogMapper.insert(behaviorLog);
        return behaviorLog.getId();
    }

    @Override
    public void updateBehaviorLog(WujinSearchBehaviorLogSaveReqVO updateReqVO) {
        validateBehaviorLogExists(updateReqVO.getId());
        WujinSearchBehaviorLogDO behaviorLog = BeanUtils.toBean(updateReqVO, WujinSearchBehaviorLogDO.class);
        behaviorLogMapper.updateById(behaviorLog);
    }

    @Override
    public void deleteBehaviorLog(Long id) {
        validateBehaviorLogExists(id);
        behaviorLogMapper.deleteById(id);
    }

    @Override
    public WujinSearchBehaviorLogDO getBehaviorLog(Long id) {
        return behaviorLogMapper.selectById(id);
    }

    @Override
    public List<WujinSearchBehaviorLogDO> getBehaviorLogList(WujinSearchBehaviorLogListReqVO listReqVO) {
        return behaviorLogMapper.selectList(listReqVO);
    }

    private void validateBehaviorLogExists(Long id) {
        if (id == null || behaviorLogMapper.selectById(id) == null) {
            throw new IllegalArgumentException("搜索行为日志不存在");
        }
    }
}

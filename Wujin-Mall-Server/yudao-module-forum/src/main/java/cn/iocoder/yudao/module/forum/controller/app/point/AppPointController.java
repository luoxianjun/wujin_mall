package cn.iocoder.yudao.module.forum.controller.app.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.forum.controller.app.point.vo.AppPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.point.ForumPointRecordDO;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 APP - 积分 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 积分")
@RestController
@RequestMapping("/forum/point")
@Validated
@Slf4j
public class AppPointController {

    @Resource
    private ForumPointService pointService;

    @GetMapping("/balance")
    @Operation(summary = "获取积分余额")
    @PreAuthenticated
    public CommonResult<Integer> getPointBalance() {
        Long userId = getLoginUserId();
        Integer balance = pointService.getUserPoint(userId);
        return success(balance);
    }

    @GetMapping("/record/page")
    @Operation(summary = "分页查询积分记录")
    @PreAuthenticated
    public CommonResult<PageResult<ForumPointRecordDO>> getPointRecordPage(@Valid AppPointRecordPageReqVO reqVO) {
        // 只能查询自己的积分记录
        reqVO.setUserId(getLoginUserId());
        PageResult<ForumPointRecordDO> pageResult = pointService.getPointRecordPage(reqVO);
        return success(pageResult);
    }

}


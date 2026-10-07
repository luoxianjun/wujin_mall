package cn.iocoder.yudao.module.forum.controller.app.sign;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRespVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignStatusRespVO;
import cn.iocoder.yudao.module.forum.convert.sign.ForumSignConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRecordDO;
import cn.iocoder.yudao.module.forum.service.sign.ForumSignService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 APP - 签到 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 签到")
@RestController
@RequestMapping("/forum/sign")
@Validated
@Slf4j
public class AppSignController {

    @Resource
    private ForumSignService signService;

    @PostMapping("/do")
    @Operation(
            summary = "签到",
            description = "无需入参，调用即为当前登录用户完成签到"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "success", value = "{\n  \"code\": 0,\n  \"msg\": \"成功\",\n  \"data\": {\n    \"id\": 123,\n    \"signDate\": \"2025-11-24\",\n    \"continuousDays\": 3,\n    \"point\": 5,\n    \"remark\": \"week:5;\"\n  }\n}")))
    })
    @PreAuthenticated
    public CommonResult<AppSignRespVO> sign() {
        Long userId = getLoginUserId();
        AppSignRespVO result = signService.sign(userId,false,null);
        return success(result);
    }

    @GetMapping("/status")
    @Operation(
            summary = "获取签到状态",
            description = "查询当前登录用户的签到状态与本周签到情况"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "success", value = "{\n  \"code\": 0,\n  \"msg\": \"成功\",\n  \"data\": {\n    \"todaySigned\": true,\n    \"continuousDays\": 3,\n    \"totalDays\": 25,\n    \"lastSignDate\": \"2025-11-24\",\n    \"weekContinuousDays\": 3,\n    \"weekMaxContinuousDays\": 3,\n    \"weekTotalPoints\": 9,\n    \"weekLastSignDate\": \"2025-11-24\",\n    \"weekSignedDates\": [\"2025-11-22\", \"2025-11-23\", \"2025-11-24\"]\n  }\n}")))
    })
    @PreAuthenticated
    public CommonResult<AppSignStatusRespVO> getSignStatus() {
        Long userId = getLoginUserId();
        AppSignStatusRespVO result = signService.getSignStatus(userId);
        return success(result);
    }

    @GetMapping("/record/page")
    @Operation(
            summary = "分页查询签到记录",
            description = "查询当前登录用户的签到记录。查询参数示例：pageNo=1&pageSize=10&startDate=2025-11-01&endDate=2025-11-24"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "success", value = "{\n  \"code\": 0,\n  \"msg\": \"成功\",\n  \"data\": {\n    \"total\": 2,\n    \"list\": [\n      {\"id\": 123, \"signDate\": \"2025-11-24\", \"continuousDays\": 3, \"point\": 5, \"remark\": \"week:5;\"},\n      {\"id\": 122, \"signDate\": \"2025-11-23\", \"continuousDays\": 2, \"point\": 3, \"remark\": \"week:3;\"}\n    ]\n  }\n}")))
    })
    @PreAuthenticated
    public CommonResult<PageResult<AppSignRespVO>> getSignRecordPage(@Valid AppSignRecordPageReqVO reqVO) {
        // 只能查询自己的签到记录
        reqVO.setUserId(getLoginUserId());
        PageResult<ForumSignRecordDO> pageResult = signService.getSignRecordPage(reqVO);
        return success(ForumSignConvert.INSTANCE.convertPage(pageResult));
    }

}

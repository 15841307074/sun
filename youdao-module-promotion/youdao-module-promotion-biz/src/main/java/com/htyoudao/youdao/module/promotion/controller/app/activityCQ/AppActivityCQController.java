package com.htyoudao.youdao.module.promotion.controller.app.activityCQ;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqDetailVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqPrizeListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.AddressSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.DrawRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyCodeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.TaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.WinningRecordVO;
import com.htyoudao.youdao.module.promotion.service.activityCqApp.ActivityCqAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/promotion/activity-cq")
@Tag(name = "抽签活动小程序", description = "抽签活动小程序接口")
public class AppActivityCQController {

    @Resource
    private ActivityCqAppService activityCqAppService;

    @GetMapping("/getActivityCqDetail")
    @Operation(summary = "获取抽签活动详情")
    public CommonResult<ActivityCqDetailVO> getActivityCqDetail(@RequestParam("activityId") Long activityId,
                                                                @RequestParam(value = "storeId", required = false) Long storeId,
                                                                @RequestParam(value = "memberId", required = false) Long memberId) {
        return CommonResult.success(activityCqAppService.getActivityCqDetail(activityId, storeId, getMemberIdOrLogin(memberId)));
    }

    @PostMapping("/task/getTaskList")
    @Operation(summary = "获取抽签任务列表")
    public CommonResult<List<TaskVO>> getTaskList(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getTaskList(reqVO));
    }

    @PostMapping("/join")
    @Operation(summary = "参与抽签活动")
    public CommonResult<Boolean> join(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.join(reqVO));
    }

    @PostMapping("/task/sign")
    @Operation(summary = "完成签到任务")
    public CommonResult<List<String>> signTask(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.signTask(reqVO));
    }

    @PostMapping("/task/share/simple")
    @Operation(summary = "完成普通分享任务")
    public CommonResult<List<String>> simpleShareTask(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.simpleShareTask(reqVO));
    }

    @PostMapping("/task/browse/home")
    @Operation(summary = "完成浏览首页任务")
    public CommonResult<List<String>> browseHomeTask(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.browseHomeTask(reqVO));
    }

    @Deprecated
    @PostMapping("/task/shareCheck")
    @Operation(summary = "检测分享助力任务")
    public CommonResult<Boolean> shareCheck(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.shareCheck(reqVO));
    }

    @PostMapping("/task/share")
    @Operation(summary = "完成分享助力任务")
    public CommonResult<Boolean> shareTask(@Valid @RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.shareTask(reqVO));
    }

    @PostMapping("/prize/list")
    @Operation(summary = "获取奖品列表")
    public CommonResult<ActivityCqPrizeListVO> getPrizeList(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getPrizeList(reqVO));
    }

    @PostMapping("/prize/address/save")
    @Operation(summary = "填写收货地址")
    public CommonResult<Boolean> saveAddress(@Valid @RequestBody AddressSaveReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.saveAddress(reqVO));
    }

    @PostMapping("/record/draw")
    @Operation(summary = "获取抽签记录")
    public CommonResult<PageResult<DrawRecordVO>> getDrawRecord(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getDrawRecord(reqVO));
    }

    @PostMapping("/code/my")
    @Operation(summary = "获取我的抽签码汇总")
    public CommonResult<MyCodeVO> getMyCodeInfo(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getMyCodeInfo(reqVO));
    }

    @PostMapping("/record/myResult")
    @Operation(summary = "获取我的抽签结果")
    public CommonResult<PageResult<MyResultVO>> getMyResultRecord(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getMyResultRecord(reqVO));
    }

    @PostMapping("/record/winning")
    @Operation(summary = "获取中奖公示列表")
    public CommonResult<PageResult<WinningRecordVO>> getWinningRecord(@RequestBody ActivityCqReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return CommonResult.success(activityCqAppService.getWinningRecord(reqVO));
    }

    private Long getLoginMemberId() {
        return SecurityFrameworkUtils.getLoginUserId();
    }

    private Long getMemberIdOrLogin(Long memberId) {
        return memberId != null ? memberId : getLoginMemberId();
    }

    private void fillLoginMemberId(ActivityCqReqVO reqVO) {
        if (reqVO != null) {
            reqVO.setMemberId(getMemberIdOrLogin(reqVO.getMemberId()));
        }
    }

    private void fillLoginMemberId(AddressSaveReqVO reqVO) {
        if (reqVO != null) {
            reqVO.setMemberId(getMemberIdOrLogin(reqVO.getMemberId()));
        }
    }
}

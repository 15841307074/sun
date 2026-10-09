package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo.*;
import com.htyoudao.youdao.module.promotion.service.activityAnswerApp.ActivityAnswerAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 有奖问答活动小程序接口。
 */
@RestController
@RequestMapping("/promotion/activity-answer")
@Tag(name = "有奖问答活动小程序", description = "有奖问答活动小程序接口")
public class AppActivityAnswerController {

    @Resource
    private ActivityAnswerAppService activityAnswerAppService;

    /**
     * 查询有奖问答活动详情。
     */
    @GetMapping("/detail")
    @Operation(summary = "查询有奖问答活动详情")
    public CommonResult<ActivityAnswerAppDetailRespVO> getDetail(@RequestParam("activityId") Long activityId,
                                                                 @RequestParam("storeId") Long storeId) {
        return success(activityAnswerAppService.getDetail(activityId, storeId, getLoginMemberId()));
    }

    /**
     * 查询当前可用答题次数。
     */
    @PostMapping("/chance/count")
    @Operation(summary = "查询当前可用答题次数")
    public CommonResult<ActivityAnswerChanceCountRespVO> getChanceCount(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.getChanceCount(reqVO));
    }

    /**
     * 开始或继续答题。
     */
    @PostMapping("/join")
    @Operation(summary = "开始或继续答题")
    public CommonResult<ActivityAnswerJoinRespVO> join(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.join(reqVO));
    }

    /**
     * 提交完整答题结果。
     */
    @PostMapping("/submit")
    @Operation(summary = "提交完整答题结果")
    public CommonResult<ActivityAnswerSubmitRespVO> submit(@Valid @RequestBody ActivityAnswerSubmitReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.submit(reqVO));
    }

    /**
     * 取消本次答题。
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消本次答题")
    public CommonResult<Boolean> cancel(@Valid @RequestBody ActivityAnswerCancelReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.cancel(reqVO));
    }

    /**
     * 查询任务列表。
     */
    @PostMapping("/task/getTaskList")
    @Operation(summary = "查询有奖问答任务列表")
    public CommonResult<List<ActivityAnswerTaskRespVO>> getTaskList(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.getTaskList(reqVO));
    }

    /**
     * 完成签到任务。
     */
    @PostMapping("/task/sign")
    @Operation(summary = "完成签到任务")
    public CommonResult<ActivityAnswerTaskCompleteRespVO> signTask(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.signTask(reqVO));
    }

    /**
     * 检测分享任务。
     */
    @PostMapping("/task/shareCheck")
    @Operation(summary = "检测分享任务")
    public CommonResult<Boolean> shareCheck(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.shareCheck(reqVO));
    }

    /**
     * 完成分享任务。
     */
    @PostMapping("/task/share")
    @Operation(summary = "完成分享任务")
    public CommonResult<ActivityAnswerTaskCompleteRespVO> shareTask(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.shareTask(reqVO));
    }

    /**
     * 完成浏览首页任务。
     */
    @PostMapping("/task/browse")
    @Operation(summary = "完成浏览首页任务")
    public CommonResult<ActivityAnswerTaskCompleteRespVO> browseTask(@Valid @RequestBody ActivityAnswerBaseReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.browseHomeTask(reqVO));
    }


    /**
     * 查询我的答题记录。
     */
    @PostMapping("/record/my")
    @Operation(summary = "查询我的答题记录")
    public CommonResult<PageResult<ActivityAnswerMyRecordRespVO>> getMyRecord(@Valid @RequestBody ActivityAnswerMyRecordPageReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.getMyRecord(reqVO));
    }

    /**
     * 查询我的奖励记录。
     */
    @PostMapping("/reward/my")
    @Operation(summary = "查询我的奖励记录")
    public CommonResult<PageResult<ActivityAnswerMyRewardRespVO>> getMyReward(@Valid @RequestBody ActivityAnswerMyRewardPageReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.getMyReward(reqVO));
    }

    /**
     * 保存实物奖品收货地址。
     */
    @PostMapping("/reward/address/save")
    @Operation(summary = "保存实物奖品收货地址")
    public CommonResult<Boolean> saveRewardAddress(@Valid @RequestBody ActivityAnswerRewardAddressSaveReqVO reqVO) {
        fillLoginMemberId(reqVO);
        return success(activityAnswerAppService.saveRewardAddress(reqVO));
    }

    /**
     * 获取当前登录会员 ID。
     */
    private Long getLoginMemberId() {
        return SecurityFrameworkUtils.getLoginUserId();
    }

    /**
     * 用登录态会员 ID 覆盖前端入参。
     */
    private void fillLoginMemberId(ActivityAnswerBaseReqVO reqVO) {
        if (reqVO != null) {
            reqVO.setMemberId(getLoginMemberId());
        }
    }

    /**
     * 用登录态会员 ID 覆盖答题记录分页入参。
     */
    private void fillLoginMemberId(ActivityAnswerMyRecordPageReqVO reqVO) {
        if (reqVO != null) {
            reqVO.setMemberId(getLoginMemberId());
        }
    }

    /**
     * 用登录态会员 ID 覆盖奖励记录分页入参。
     */
    private void fillLoginMemberId(ActivityAnswerMyRewardPageReqVO reqVO) {
        if (reqVO != null) {
            reqVO.setMemberId(getLoginMemberId());
        }
    }
}

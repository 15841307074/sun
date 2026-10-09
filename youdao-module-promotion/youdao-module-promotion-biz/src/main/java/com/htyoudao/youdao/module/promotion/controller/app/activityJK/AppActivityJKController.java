package com.htyoudao.youdao.module.promotion.controller.app.activityJK;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkDetailVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkPrizeListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.AddressSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ExchangeRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.MyCardListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeExchangeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeExchangeResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.TaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.UniversalExchangeReqVO;
import com.htyoudao.youdao.module.promotion.service.activityJkApp.ActivityJkAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 集卡活动小程序 Controller
 */
@RestController
@RequestMapping("/promotion/activity-jk")
@Tag(name = "集卡活动小程序", description = "集卡活动小程序接口")
public class AppActivityJKController {

    @Resource
    private ActivityJkAppService activityJKAppService;

    @GetMapping("/getActivityJkList")
    @Operation(summary = "获取集卡活动列表")
    @PermitAll
    public CommonResult<List<ActivityJkListVO>> getActivityJkList(@Param("storeId") Long storeId) {
        return CommonResult.success(activityJKAppService.getActivityJkList(storeId));
    }

    @GetMapping("/getActivityJkDetail")
    @Operation(summary = "获取集卡活动详情")
    @PermitAll
    public CommonResult<ActivityJkDetailVO> getActivityJkDetail(
            @RequestParam("activityId") Long activityId,
            @RequestParam(value = "memberId", required = false) Long memberId) {
        return CommonResult.success(activityJKAppService.getActivityJkDetail(activityId, memberId));
    }


    @PostMapping("/draw/verify")
    @Operation(summary = "校验抽卡资格")
    public CommonResult<Boolean> verifyDraw(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.verifyDraw(reqVO));
    }

    @PostMapping("/draw/count")
    @Operation(summary = "查询当前活动可用集卡次数")
    public CommonResult<Integer> getDrawChanceCount(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getDrawChanceCount(reqVO));
    }

    @PostMapping("/draw")
    @Operation(summary = "用户抽卡")
    public CommonResult<DrawResultVO> drawCard(@Valid @RequestBody DrawReqVO reqVO) {
        return CommonResult.success(activityJKAppService.drawCard(reqVO));
    }

    @PostMapping("/task/getTaskList")
    @Operation(summary = "获取集卡任务列表")
    public CommonResult<List<TaskVO>> getTaskList(@RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getTaskList(reqVO));
    }

    @PostMapping("/task/sign")
    @Operation(summary = "完成签到任务", description = "有上限时按当天校验，无上限时按累计校验")
    public CommonResult<Boolean> signTask(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.signTask(reqVO));
    }
    @PostMapping("/task/browse")
    @Operation(summary = "完成浏览首页任务", description = "有上限时按当天校验，无上限时按累计校验")
    public CommonResult<Boolean> browseTask(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.browseTask(reqVO));
    }
    @PostMapping("/task/shareCheck")
    @Operation(summary = "检测分享助力任务", description = "memberId 为当前助力用户，inviterMemberId 为邀请人用户ID")
    public CommonResult<Boolean> shareCheck(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.shareCheck(reqVO));
    }
    @PostMapping("/task/share")
    @Operation(summary = "完成分享助力任务", description = "memberId 为当前助力用户，inviterMemberId 为邀请人用户ID")
    public CommonResult<Boolean> shareTask(@Valid @RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.shareTask(reqVO));
    }

    @PostMapping("/card/my")
    @Operation(summary = "获取我的卡片列表")
    @PermitAll
    public CommonResult<MyCardListVO> getMyCardList(@RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getMyCardList(reqVO));
    }

    @PostMapping("/card/universal/exchange")
    @Operation(summary = "万能卡兑换")
    public CommonResult<Boolean> exchangeUniversalCard(@Valid @RequestBody UniversalExchangeReqVO reqVO) {
        return CommonResult.success(activityJKAppService.exchangeUniversalCard(reqVO));
    }

    @PostMapping("/prize/list")
    @Operation(summary = "获取奖品列表")
    @PermitAll
    public CommonResult<ActivityJkPrizeListVO> getPrizeList(@RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getPrizeList(reqVO));
    }

    @PostMapping("/prize/exchange")
    @Operation(summary = "兑换奖品")
    public CommonResult<PrizeExchangeResultVO> exchangePrize(@Valid @RequestBody PrizeExchangeReqVO reqVO) {
        return CommonResult.success(activityJKAppService.exchangePrize(reqVO));
    }

    @PostMapping("/prize/address/save")
    @Operation(summary = "填写收货地址")
    public CommonResult<Boolean> saveAddress(@Valid @RequestBody AddressSaveReqVO reqVO) {
        return CommonResult.success(activityJKAppService.saveAddress(reqVO));
    }

    @PostMapping("/record/draw")
    @Operation(summary = "获取抽卡记录")
    public CommonResult<PageResult<DrawRecordVO>> getDrawRecord(@RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getDrawRecord(reqVO));
    }

    @PostMapping("/record/exchange")
    @Operation(summary = "获取兑换记录")
    public CommonResult<PageResult<ExchangeRecordVO>> getExchangeRecord(@RequestBody ActivityJkReqVO reqVO) {
        return CommonResult.success(activityJKAppService.getExchangeRecord(reqVO));
    }

    @GetMapping("/getCardCount")
    @Operation(summary = "获取卡片数量")
    public CommonResult<Long> getCardCount(@Param("id") Long id,@Param("memberId") Long memberId) {
        return CommonResult.success(activityJKAppService.getCardCount(id,memberId));
    }

    @GetMapping("/claimRedPacket")
    @Operation(summary = "领取红包")
    public CommonResult<Long> claimRedPacket(@Param("id") Long id,@Param("memberId") Long memberId) {
//        return CommonResult.success(activityJKAppService.claimRedPacket(id,memberId));
        return null;
    }
    @GetMapping("/getShareVO")
    @Operation(summary = "小程序集卡分享详情")
    @PermitAll
    public CommonResult<ActivityCollectAppShareVO> getShareVO(@RequestParam("activityId") Long activityId){
        ActivityCollectAppShareVO activityCollectAppShareVO =   activityJKAppService.getShareVO(activityId);
        return success(activityCollectAppShareVO);
    }
}









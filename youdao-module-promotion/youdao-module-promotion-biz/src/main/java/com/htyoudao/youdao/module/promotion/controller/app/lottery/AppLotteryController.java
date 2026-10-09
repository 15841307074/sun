package com.htyoudao.youdao.module.promotion.controller.app.lottery;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryBrowseCountVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsNumVo;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryV2Service;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryWinnerFeed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promotion/lottery-mobile")
@Tag(name = "抽奖活动小程序", description = "优抽奖活动小程序Controller")
public class AppLotteryController {

    @Resource
    private LotteryMobileService lotteryMobileservice;
    @Resource
    private LotteryV2Service lotteryV2Service;
    @Resource
    private LotteryWinnerFeed winnerFeed;

    @GetMapping("/recent-winners")
    @Operation(summary = "当前活动最新20条非兜底中奖记录，手机号已脱敏", description = "lotteryId 支持抽奖配置ID或活动主表ID；活动内所有门店汇总；返回不足20条时取实际条数")
    public CommonResult<List<LotteryRecentWinnerRespVO>> recentWinners(@RequestParam("lotteryId") Long lotteryId) {
        return CommonResult.success(winnerFeed.latest(lotteryId));
    }

    @PostMapping("/result")
    @Operation(summary = "根据 requestId 查询已受理的抽奖及发奖结果")
    public CommonResult<LotteryUserLogVO> result(@Valid @RequestBody LotteryVO request) {
        return CommonResult.success(lotteryV2Service.query(request));
    }


    @GetMapping("/getLotteryTypeList")
    @Operation(summary = "获取活动分类")
    public CommonResult<List<LotteryTypeVO>> getLotteryTypeList(@Param("storeId") Long storeId) {
        return CommonResult.success(lotteryMobileservice.getLotteryTypeList(storeId));
    }

    @PostMapping("/getLotteryDetailApp")
    @Operation(summary = "获取活动详情App")
    public CommonResult<LotterySettingsResVO> getLotteryDetail(@RequestBody LotteryVO lotteryVO) {
        return CommonResult.success(lotteryMobileservice.getLotteryDetail(lotteryVO));
    }

    @PostMapping("/getLotteryLogByMemberId")
    @Operation(summary = "获取活动中奖记录")
    public CommonResult<List<LotteryLogDO>> getLotteryLogByMemberId(@RequestBody LotteryLogVO lotteryLogVO) {
        return CommonResult.success(lotteryMobileservice.getLotteryLogByMemberId(lotteryLogVO));
    }

    @PostMapping("/updateReceivingAddress")
    @Operation(summary = "设置收货地址")
    public CommonResult updateReceivingAddress(@RequestBody LotteryLogVO lotteryLogVO) {
        return CommonResult.success(lotteryMobileservice.updateReceivingAddress(lotteryLogVO));
    }

    @PostMapping("/verifyLottery")
    @Operation(summary = "抽奖校验")
    public CommonResult verifyLottery(@RequestBody LotteryVO lotteryVO) {
        return lotteryMobileservice.verifyLottery(lotteryVO);
    }

    @PostMapping("/lottery")
    @Operation(summary = "抽奖")
    public CommonResult<LotteryUserLogVO> lottery(@RequestBody LotteryVO lotteryVO) {
        return lotteryMobileservice.lottery(lotteryVO);
    }

    @PostMapping("/lotteryNum")
    @Operation(summary = "抽奖次数")
    public CommonResult<LotterySettingsNumVo> lotteryNum(@RequestBody LotteryVO lotteryVO) {
        return CommonResult.success(lotteryMobileservice.lotteryNum(lotteryVO));
    }

    @PostMapping("/task/getTaskList")
    @Operation(summary = "获取抽奖任务列表")
    public CommonResult<List<LotteryTaskVO>> getTaskList(@Valid @RequestBody LotteryTaskReqVO reqVO) {
        return CommonResult.success(lotteryMobileservice.getTaskList(reqVO));
    }

    @PostMapping("/task/shareCheck")
    @Operation(summary = "检测分享任务", description = "memberId 为分享用户ID")
    public CommonResult<Boolean> shareCheck(@Valid @RequestBody LotteryTaskReqVO reqVO) {
        return CommonResult.success(lotteryMobileservice.shareCheck(reqVO));
    }

    @PostMapping("/task/share")
    @Operation(summary = "完成分享任务", description = "memberId 为分享用户ID")
    public CommonResult<Boolean> shareTask(@Valid @RequestBody LotteryTaskReqVO reqVO) {
        return CommonResult.success(lotteryMobileservice.shareTask(reqVO));
    }

    @PostMapping("/task/browse")
    @Operation(summary = "完成浏览首页任务")
    public CommonResult<Boolean> browseTask(@Valid @RequestBody LotteryTaskReqVO reqVO) {
        return CommonResult.success(lotteryMobileservice.browseTask(reqVO));
    }

    @GetMapping("/delRedis")
    @Operation(summary = "清除缓存")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:update')")
    public CommonResult<Integer> delRedis(@Param("type") int type) {
        lotteryMobileservice.delRedis(type);
        return CommonResult.success(0);
    }

    @GetMapping("/getRedis")
    @Operation(summary = "查询次数缓存")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:update')")
    public CommonResult<Integer> getRedis(@Param("type") int type, @Param("memberId") Long memberId, @Param("lotteryId") Long lotteryId) {
        return CommonResult.success(lotteryMobileservice.getRedis(type, memberId, lotteryId));
    }

    @GetMapping("/getLotteryList")
    @Operation(summary = "查询用户抽奖列表")
    public CommonResult<Map<Long, List<Long>>> getLotteryList(@Param("storeId") Long storeId, @Param("memberId") Long memberId) {
        return CommonResult.success(lotteryMobileservice.getLotteryList(storeId, memberId));
    }

    @PostMapping("/browse/complete")
    @Operation(summary = "完成浏览任务并增加一次额外抽奖次数")
    public CommonResult<LotteryBrowseCountVO> completeBrowseChance(@RequestBody LotteryVO lotteryVO) {
        return CommonResult.success(new LotteryBrowseCountVO());
    }

    @PostMapping("/browse/count")
    @Operation(summary = "获取浏览任务完成次数")
    public CommonResult<LotteryBrowseCountVO> getBrowseCount(@RequestBody LotteryVO lotteryVO) {
        return CommonResult.success(new LotteryBrowseCountVO());
    }
}

package com.htyoudao.youdao.module.promotion.controller.admin.lottery;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotterySettingsResVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotterySettingService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryReissueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.io.IOException;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 活动中奖记录")
@RestController
@RequestMapping("/promotion/lottery-log")
@Validated
public class LotteryController {

    @Resource
    private LotteryLogService lotteryLogService;

    @Resource
    private LotterySettingService lotterySettingService;
    @Resource
    private LotteryReissueService lotteryReissueService;

    @PostMapping("/reissue")
    @Operation(summary="人工补发原抽奖奖品",description="仅后台有补发权限的人员调用；异步安排原任务，不重复扣次数或消耗积分；已成功/正在执行不会重复发放")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:reissue')")
    public CommonResult<LotteryReissueRespVO> reissue(@Valid @RequestBody LotteryReissueReqVO reqVO) {
        return success(lotteryReissueService.reissue(reqVO));
    }

    @GetMapping("/getLotteryLogList")
    @Operation(summary = "获取活动中奖记录")
    public PageResult<LotteryLogRespVO> getLotteryLogList(@RequestParam(value = "pageNum", required = false) int pageNum,
                                                            @RequestParam(value = "pageSize", required = false) int pageSize,
                                                            LotteryLogReqVO lotteryLogReqVO) {
        PageResult<LotteryLogDO> lotteryLogList = lotteryLogService.getLotteryLogList(pageNum, pageSize, lotteryLogReqVO);
        return BeanUtils.toBean(lotteryLogList, LotteryLogRespVO.class);
    }

    @GetMapping("/getLotterySettingsListPage")
    @Operation(summary = "活动设置分页列表")
    public PageResult<LotterySettingsRespVO> getLotterySettingsListPage(@RequestParam(value = "pageNum", required = false) int pageNum,
                                                                        @RequestParam(value = "pageSize", required = false) int pageSize,
                                                                        LotterySettingsReqVO lotterySettingsReqVO) {
        return lotterySettingService.getLotterySettingsListPage(pageNum, pageSize, lotterySettingsReqVO);
    }

    @PostMapping("/addLottery")
    @Operation(summary = "活动设置添加")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:add')")
    public CommonResult<Integer> addLotteryPrize(@RequestBody LotterySettingsAddReqVO lotterySettingsAddVO) {
        LotterySettingsReqVO lotterySettingsVO = BeanUtils.toBean(lotterySettingsAddVO, LotterySettingsReqVO.class);
        return lotterySettingService.addLotteryPrize(lotterySettingsVO);
    }

    @PostMapping("/updateLottery")
    @Operation(summary = "活动设置修改")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:update')")
    public CommonResult<Integer> updateLottery(@RequestBody LotterySettingsUpdateReqVO lotterySettingsUpdateReqVO) {
        LotterySettingsReqVO lotterySettingsVO = BeanUtils.toBean(lotterySettingsUpdateReqVO, LotterySettingsReqVO.class);
        return lotterySettingService.updateLottery(lotterySettingsVO);
    }

    @PostMapping("/saveLottery")
    @Operation(summary = "活动设置添加")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:add')")
    public CommonResult<Integer> saveLottery(@Valid @RequestBody LotterySettingsAddReqVO lotterySettingsAddVO) {
        return lotterySettingService.saveLottery(lotterySettingsAddVO);
    }

    @PostMapping("/update")
    @Operation(summary = "活动设置修改（新）")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:update')")
    public CommonResult<Integer> update(@Valid @RequestBody LotterySettingsUpdateReqVO lotterySettingsUpdateReqVO) {
        return lotterySettingService.updateLotteryData(lotterySettingsUpdateReqVO);
    }


    @PostMapping("/updateLotteryState")
    @Operation(summary = "活动启用/禁用")
    @PreAuthorize("@ss.hasPermission('promotion:lotteryState:update')")
    public CommonResult<Integer> updateLotteryState(@RequestBody LotterySettingsReqVO lotterySettingsVO) {
        return lotterySettingService.updateLotteryState(lotterySettingsVO);
    }

    @PostMapping("/updateState")
    @Operation(summary = "活动启用/禁用（新）")
    @PreAuthorize("@ss.hasPermission('promotion:lotteryState:update')")
    public CommonResult<Integer> updateState(@Valid @RequestBody LotterySettingsReqVO lotterySettingsVO) {
        return lotterySettingService.updateState(lotterySettingsVO);
    }

    @GetMapping("/removeLottery")
    @PreAuthorize("@ss.hasPermission('promotion:lottery:delete')")
    @Operation(summary = "活动删除（新）")
    public CommonResult<Integer> removeLottery(@RequestParam(value = "id", required = false) long id) {
        return lotterySettingService.removeLottery(id);
    }


    @GetMapping("/getByDetail")
    @Operation(summary = "获取活动详情（新）")
    public CommonResult<LotterySettingsDetailDataRespVO> getByDetail(@RequestParam(value = "id", required = false) long id) {
        return CommonResult.success(lotterySettingService.getByDetail(id));
    }

    @GetMapping("/deleteLottery")
    @Operation(summary = "活动删除")
    public CommonResult<Integer> deleteLottery(@RequestParam(value = "id", required = false) long id) {
        return lotterySettingService.deleteLottery(id);
    }

    @GetMapping("/getLotteryDetail")
    @Operation(summary = "活动详情")
    public CommonResult<LotterySettingsDetailRespVO> getLotteryDetail(@RequestParam(value = "id", required = false) long id) {
        return CommonResult.success(lotterySettingService.getLotteryDetails(id));
    }

    @PostMapping("/updateExpress")
    @Operation(summary = "设置快递")
    @PreAuthorize("@ss.hasPermission('promotion:express:addOrUpdate')")
    public CommonResult<Integer> updateExpress(@RequestBody LotteryLogReqVO lotteryLogReqVO) {
        return CommonResult.success(lotteryLogService.updateExpress(lotteryLogReqVO));
    }
    @GetMapping("/returnReal")
    @Operation(summary = "奖品退回")
    @PermitAll
    public void returnReal() {
        lotteryLogService.returnReal();
    }

    @PostMapping("/getLotteryLogList")
    @Operation(summary = "根据活动获取抽奖记录（新）")
    public CommonResult<PageResult<LotteryLogPageRespVO>> getByLotteryLogList(@RequestBody LotteryLogReqVO lotteryLogReqVO) {
        PageResult<LotteryLogDO> lotteryLogList = lotteryLogService.getByLotteryLogList(lotteryLogReqVO);
        return CommonResult.success(BeanUtils.toBean(lotteryLogList, LotteryLogPageRespVO.class));
    }



    @PostMapping("/lotteryDrawStatistics")
    @Operation(summary = "参与人数、抽奖次数、消耗积分数据统计（新）")
    public CommonResult<LotteryLogStatisticsRespVO> lotteryDrawStatistics(@RequestBody LotteryLogReqVO lotteryLogReqVO){
        LotteryLogStatisticsRespVO statisticsRespVO = lotteryLogService.lotteryDrawStatistics(lotteryLogReqVO);
        return CommonResult.success(statisticsRespVO);
    }



    @GetMapping("/export")
    @Operation(summary = "导出抽奖记录（新）")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportLotteryList(@Valid LotteryLogExportVO lotteryLogExportVO, HttpServletRequest request, HttpServletResponse response) throws ServerException {
        lotteryLogService.exportLotteryList(lotteryLogExportVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @GetMapping("/selectSpread")
    @Operation(summary = "查询抽奖活动的推广")
    public CommonResult<LotterySpreadRespVO>  selectSpread(@RequestParam("id") Long id) {
        LotterySpreadRespVO lotterySpreadRespVO = lotterySettingService.selectSpread(id);
        return CommonResult.success(lotterySpreadRespVO);
    }


    @PostMapping("/updateSpread")
    @Operation(summary = "修改秒杀活动的推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody LotterySpreadSaveReqVO lotterySpreadSaveReqVO) {

        lotterySettingService.updateSpread(lotterySpreadSaveReqVO);

        return success(true);
    }



    /*@PostMapping("/copyLottery")
    @Operation(summary = "活动设置添加")
//    @PreAuthorize("@ss.hasPermission('promotion:lottery:copy')")
    public CommonResult<Integer> copyLotteryPrize(@RequestBody LotterySettingsAddReqVO lotterySettingsAddVO) {
        LotterySettingsReqVO lotterySettingsVO = BeanUtils.toBean(lotterySettingsAddVO, LotterySettingsReqVO.class);
        return lotterySettingService.copyLotteryPrize(lotterySettingsVO);
    }*/

    @GetMapping("/getLotteryLogAnalysis/{id}")
    @Operation(summary = "获取抽奖活动数据概览")
    public CommonResult<Map<String, Object>> getLotteryLogAnalysis(@PathVariable("id") String id) {
        Map<String, Object> result = lotteryLogService.getLotteryLogAnalysis(id);
        return CommonResult.success(result);
    }

    @PostMapping ("/getLotteryLogDailyAnalysis")
    @Operation(summary = "获取抽奖活动数据分析")
    public CommonResult<Map<String, Object>> getLotteryLogDailyAnalysis(@RequestBody @Valid LotteryLogEventReqVO lotteryLogEventReqVO) {
        Map<String, Object> result = lotteryLogService.getLotteryLogDailyAnalysis(lotteryLogEventReqVO);
        return CommonResult.success(result);
    }
    @PostMapping ("/lotteryPoolReset")
    @Operation(summary = "重置")
    public void  lotteryPoolReset() {
        lotterySettingService.lotteryPoolReset();
    }



}

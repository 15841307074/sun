package com.htyoudao.youdao.module.promotion.controller.admin.activityJk;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityJk.ActivityJkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MJ_IS_NOT_ID;

@Tag(name = "后台pc - 集卡活动")
@RestController
@RequestMapping("/promotion/activityJk")
public class ActivityJkController {


    @Resource
    private ActivityJkService activityJkService;


    @PostMapping("/create")
    @Operation(summary = "创建集卡活动")
    public CommonResult<Integer> createActivityJk(@Valid @RequestBody ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        return activityJkService.createActivityJk(saveOrUpdateReqVO);
    }

    @PostMapping("/update")
    @Operation(summary = "修改集卡活动")
    public CommonResult<Integer> updateActivityJk(@Valid @RequestBody ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        if (saveOrUpdateReqVO.getId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        return activityJkService.updateActivityJk(saveOrUpdateReqVO);
    }


    @GetMapping("/delete")
    @Operation(summary = "删除集卡活动")
    public CommonResult<Integer> deleteActivityJk(@RequestParam(name = "id") Long id) {
        return activityJkService.deleteActivityJk(id);
    }


    @GetMapping("/selectDetail")
    @Operation(summary = "查询集卡活动详情")
    public CommonResult<ActivityJkDetailRespVO> selectDetail(@RequestParam("id") Long id) {
        ActivityJkDetailRespVO activityInfoRespVO = activityJkService.selectInfo(id);
        return success(activityInfoRespVO);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制集卡活动")
    public CommonResult<Integer> copyActivityJk(@Valid @RequestBody ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        return activityJkService.copyActivityJk(saveOrUpdateReqVO);
    }

    @GetMapping("/getActivityJkAnalysis/{id}")
    @Operation(summary = "获取集卡活动数据概览")
    public CommonResult<Map<String, Object>> getActivityJkAnalysis(@PathVariable("id") String id) {
        Map<String, Object> result = activityJkService.getActivityJkAnalysis(id);
        return CommonResult.success(result);
    }


    @PostMapping ("/getActivityJkDailyAnalysis")
    @Operation(summary = "获取集卡活动数据分析")
    public CommonResult<Map<String, Object>> getActivityJkDailyAnalysis(@Valid @RequestBody ActivityJkLogEventReqVO activityJkLogEventReqVO) {
        Map<String, Object> result = activityJkService.getActivityJkDailyAnalysis(activityJkLogEventReqVO);
        return CommonResult.success(result);
    }


    @PostMapping("/getActivityJkLogList")
    @Operation(summary = "根据活动获取集卡记录")
    public CommonResult<PageResult<ActivityCardLogRespVO>> getActivityJkLogList(@Valid @RequestBody ActivityJkLogPageReqVO activityJkLogPageReqVO) {
        return CommonResult.success(activityJkService.getActivityJkLogList(activityJkLogPageReqVO));
    }


    @PostMapping("/getActivityJkLogCount")
    @Operation(summary = "集卡参与人数、参与次数统计")
    public CommonResult<ActivityJkStatisticsRespVO> getActivityJkLogCount(@Valid @RequestBody ActivityJkLogPageReqVO activityJkLogPageReqVO){
        ActivityJkStatisticsRespVO statisticsRespVO = activityJkService.getActivityJkLogCount(activityJkLogPageReqVO);
        return CommonResult.success(statisticsRespVO);
    }

    @PostMapping("/getExchangeLogList")
    @Operation(summary = "根据活动获取兑换记录")
    public CommonResult<PageResult<ActivityExchangePageRespVO>> getExchangeLogList(@Valid @RequestBody ActivityJkExchangePageReqVO activityJkExchangePageReqVO) {
        return CommonResult.success(activityJkService.getExchangeLogList(activityJkExchangePageReqVO));
    }

    @PostMapping("/getExchangeCount")
    @Operation(summary = "奖品兑换参与人数、参与次数统计")
    public CommonResult<ActivityJkStatisticsRespVO> getExchangeCount(@Valid @RequestBody ActivityJkExchangePageReqVO activityJkExchangePageReqVO){
        ActivityJkStatisticsRespVO statisticsRespVO = activityJkService.getExchangeCount(activityJkExchangePageReqVO);
        return CommonResult.success(statisticsRespVO);
    }

    @PostMapping("/updateExpress")
    @Operation(summary = "兑换 发货地址填写")
    public CommonResult<Integer> updateExpress(@Valid @RequestBody ActivityExchangeReqVO activityExchangeReqVO) {
        return CommonResult.success(activityJkService.updateExpress(activityExchangeReqVO));
    }

    @GetMapping("/exportActivityJkLog")
    @Operation(summary = "导出集卡记录）")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportActivityJkLog(@Valid ActivityJkLogExportReqVO activityJkLogExportReqVO, HttpServletRequest request, HttpServletResponse response) throws ServerException {
        activityJkService.exportActivityJkLog(activityJkLogExportReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }


    @GetMapping("/exportExchangeLog")
    @Operation(summary = "导出兑换记录）")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportExchangeLog(@Valid ActivityJkExchangeExchangeReqVO activityJkLogExportReqVO, HttpServletRequest request, HttpServletResponse response) throws ServerException {
        activityJkService.exportExchangeLog(activityJkLogExportReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @GetMapping("/selectActivitySpread")
    @Operation(summary = "查询集卡活动的推广")
    public CommonResult<ActivityJkSpreadRespVO> selectActivitySpread(@RequestParam("id") Long id) {
        ActivityJkSpreadRespVO activityJkSpreadRespVO = activityJkService.selectSpread(id);
        return CommonResult.success(activityJkSpreadRespVO);
    }


    @PostMapping("/updateSpread")
    @Operation(summary = "修改集卡活动的推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivityJkSpreadSaveReqVO activityJkSpreadSaveReqVO) {

        activityJkService.updateSpread(activityJkSpreadSaveReqVO);

        return success(true);
    }


    @GetMapping("/getActivityJkList")
    @Operation(summary = "广告获取集卡活动列表")
    public CommonResult<List<ActivityJkReqVO>> getActivityJkList() {
        return CommonResult.success(activityJkService.getActivityJkList());
    }

    @PostMapping("/updateState")
    @Operation(summary = "活动启用/禁用（新）")
    public CommonResult<Integer> updateState(@Valid @RequestBody ActivityJkStateReqVO activityJkStateReqVO) {
        return CommonResult.success(activityJkService.updateState(activityJkStateReqVO));
    }


}

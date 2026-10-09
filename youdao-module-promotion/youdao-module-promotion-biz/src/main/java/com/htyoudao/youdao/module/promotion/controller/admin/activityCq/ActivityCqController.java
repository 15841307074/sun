package com.htyoudao.youdao.module.promotion.controller.admin.activityCq;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogReqVO;
import com.htyoudao.youdao.module.promotion.service.activityCq.ActivityCqService;
import com.htyoudao.youdao.module.promotion.service.activityCqDraw.ActivityCqDrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MJ_IS_NOT_ID;

@Tag(name = "后台 PC - 抽签活动")
@RestController
@RequestMapping("/promotion/activityCq")
public class ActivityCqController {

    /**
     * 抽签活动服务
     */
    @Resource
    private ActivityCqService activityCqService;
    @Resource
    private ActivityCqDrawService activityCqDrawService;

    /**
     * 创建抽签活动
     */
    @PostMapping("/create")
    @Operation(summary = "创建抽签活动")
    public CommonResult<Integer> createActivityCq(@Valid @RequestBody ActivityCqSaveOrUpdateReqVO reqVO) {
        return activityCqService.createActivityCq(reqVO);
    }

    /**
     * 修改抽签活动
     */
    @PostMapping("/update")
    @Operation(summary = "修改抽签活动")
    public CommonResult<Integer> updateActivityCq(@Valid @RequestBody ActivityCqSaveOrUpdateReqVO reqVO) {
        if (reqVO.getId() == null && reqVO.getActivityId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        return activityCqService.updateActivityCq(reqVO);
    }

    /**
     * 删除抽签活动
     */
    @GetMapping("/delete")
    @Operation(summary = "删除抽签活动")
    public CommonResult<Integer> deleteActivityCq(@RequestParam("id") Long id) {
        return activityCqService.deleteActivityCq(id);
    }

    /**
     * 查询抽签活动详情
     */
    @GetMapping("/selectDetail")
    @Operation(summary = "查询抽签活动详情")
    public CommonResult<ActivityCqDetailRespVO> selectDetail(@RequestParam("id") Long id) {
        return success(activityCqService.selectDetail(id));
    }

    /**
     * 复制抽签活动
     */
    @PostMapping("/copy")
    @Operation(summary = "复制抽签活动")
    public CommonResult<Integer> copyActivityCq(@Valid @RequestBody ActivityCqSaveOrUpdateReqVO reqVO) {
        return activityCqService.copyActivityCq(reqVO);
    }

    /**
     * 获取抽签活动概览数据
     */
    @GetMapping("/getActivityCqAnalysis/{id}")
    @Operation(summary = "获取抽签活动数据概览")
    public CommonResult<Map<String, Object>> getActivityCqAnalysis(@PathVariable("id") String id) {
        return success(activityCqService.getActivityCqAnalysis(id));
    }

    /**
     * 获取抽签活动按天统计数据
     */
    @PostMapping("/getActivityCqDailyAnalysis")
    @Operation(summary = "获取抽签活动数据分析")
    public CommonResult<Map<String, Object>> getActivityCqDailyAnalysis(@Valid @RequestBody ActivityCqLogEventReqVO reqVO) {
        return success(activityCqService.getActivityCqDailyAnalysis(reqVO));
    }

    /**
     * 分页查询抽签记录
     */
    @PostMapping("/getActivityCqLogList")
    @Operation(summary = "根据活动获取抽签记录")
    public CommonResult<PageResult<ActivityCqLogRespVO>> getActivityCqLogList(@Valid @RequestBody ActivityCqLogPageReqVO reqVO) {
        return success(activityCqService.getActivityCqLogList(reqVO));
    }

    /**
     * 统计抽签参与人数和参与次数
     */
    @PostMapping("/getActivityCqLogCount")
    @Operation(summary = "抽签参与人数、参与次数统计")
    public CommonResult<ActivityCqStatisticsRespVO> getActivityCqLogCount(@Valid @RequestBody ActivityCqLogPageReqVO reqVO) {
        return success(activityCqService.getActivityCqLogCount(reqVO));
    }

    /**
     * 分页查询中签记录
     */
    @PostMapping("/getWinningLogList")
    @Operation(summary = "根据活动获取中签记录")
    public CommonResult<PageResult<ActivityCqLogRespVO>> getWinningLogList(@Valid @RequestBody ActivityCqLogPageReqVO reqVO) {
        return success(activityCqService.getWinningLogList(reqVO));
    }

    /**
     * 统计中签人数和中签次数
     */
    @PostMapping("/getWinningCount")
    @Operation(summary = "中签人数、中签次数统计")
    public CommonResult<ActivityCqStatisticsRespVO> getWinningCount(@Valid @RequestBody ActivityCqLogPageReqVO reqVO) {
        return success(activityCqService.getWinningCount(reqVO));
    }

    /**
     * 导出抽签记录
     */
    @GetMapping("/exportActivityCqLog")
    @Operation(summary = "导出抽签记录")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportActivityCqLog(@Valid ActivityCqLogExportReqVO reqVO,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) throws ServerException {
        activityCqService.exportActivityCqLog(reqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 导出中签记录
     */
    @GetMapping("/exportWinningLog")
    @Operation(summary = "导出中签记录")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportWinningLog(@Valid ActivityCqLogExportReqVO reqVO,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws ServerException {
        activityCqService.exportWinningLog(reqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 更新中签记录发货信息
     */
    @PostMapping("/updateExpress")
    @Operation(summary = "中签记录填写发货信息")
    public CommonResult<Integer> updateExpress(@Valid @RequestBody ActivityCqUpdateExpressReqVO reqVO) {
        return success(activityCqService.updateExpress(reqVO));
    }

    /**
     * 查询抽签活动分享配置
     */
    @GetMapping("/selectActivitySpread")
    @Operation(summary = "查询抽签活动推广")
    public CommonResult<ActivityCqSpreadRespVO> selectActivitySpread(@RequestParam("id") Long id) {
        return success(activityCqService.selectSpread(id));
    }

    /**
     * 修改抽签活动分享配置
     */
    @PostMapping("/updateSpread")
    @Operation(summary = "修改抽签活动推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivityCqSpreadSaveReqVO reqVO) {
        activityCqService.updateSpread(reqVO);
        return success(true);
    }

    /**
     * 获取抽签活动列表
     */
    @GetMapping("/getActivityCqList")
    @Operation(summary = "获取抽签活动列表")
    public CommonResult<List<ActivityCqReqVO>> getActivityCqList() {
        return success(activityCqService.getActivityCqList());
    }

    /**
     * 更新抽签活动启用状态
     */
    @PostMapping("/updateState")
    @Operation(summary = "抽签活动启用/禁用")
    public CommonResult<Integer> updateState(@Valid @RequestBody ActivityCqStateReqVO reqVO) {
        return success(activityCqService.updateState(reqVO));
    }

    @PostMapping("/drawExecute")
    @Operation(summary = "执行开奖")
    public CommonResult<Boolean> drawExecute(@RequestParam("id") Long id) {
        // 后台手动触发开奖，实际执行逻辑统一收口到 activityCqDrawService，和定时任务入口共用同一套流程。
        activityCqDrawService.executeDraw(id);
        return success(true);
    }

}

package com.htyoudao.youdao.module.promotion.controller.admin.activityVote;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.*;
import com.htyoudao.youdao.module.promotion.service.activityVote.ActivityVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.VOTE_IS_NOT_ID;

@Tag(name = "后台 PC - 投票活动")
@RestController
@RequestMapping("/promotion/activityVote")
public class ActivityVoteController {

    @Resource
    private ActivityVoteService activityVoteService;

    @PostMapping("/create")
    @Operation(summary = "创建投票活动")
    public CommonResult<Integer> createActivityVote(@Valid @RequestBody ActivityVoteSaveOrUpdateReqVO reqVO) {
        return activityVoteService.createActivityVote(reqVO);
    }

    @PostMapping("/update")
    @Operation(summary = "修改投票活动")
    public CommonResult<Integer> updateActivityVote(@Valid @RequestBody ActivityVoteSaveOrUpdateReqVO reqVO) {
        if (reqVO.getId() == null && reqVO.getActivityId() == null) {
            throw exception(VOTE_IS_NOT_ID);
        }
        return activityVoteService.updateActivityVote(reqVO);
    }

    @GetMapping("/delete")
    @Operation(summary = "删除投票活动")
    public CommonResult<Integer> deleteActivityVote(@RequestParam("id") Long id) {
        return activityVoteService.deleteActivityVote(id);
    }

    @GetMapping("/selectDetail")
    @Operation(summary = "查询投票活动详情")
    public CommonResult<ActivityVoteDetailRespVO> selectDetail(@RequestParam("id") Long id) {
        return success(activityVoteService.selectDetail(id));
    }

    @PostMapping("/copy")
    @Operation(summary = "复制投票活动")
    public CommonResult<Integer> copyActivityVote(@Valid @RequestBody ActivityVoteSaveOrUpdateReqVO reqVO) {
        return activityVoteService.copyActivityVote(reqVO);
    }

    @GetMapping("/getActivityVoteAnalysis")
    @Operation(summary = "获取投票活动数据概览")
    public CommonResult<ActivityVoteAnalysisRespVO> getActivityVoteAnalysis(@RequestParam("id") String id) {
        return success(activityVoteService.getActivityVoteAnalysis(id));
    }

    @PostMapping("/getActivityVoteDailyAnalysis")
    @Operation(summary = "获取投票活动数据分析")
    public CommonResult<Map<String, Object>> getActivityVoteDailyAnalysis(@Valid @RequestBody ActivityVoteLogEventReqVO reqVO) {
        return success(activityVoteService.getActivityVoteDailyAnalysis(reqVO));
    }

    @PostMapping("/getActivityVoteOptionAnalysis")
    @Operation(summary = "获取投票选项分析")
    public CommonResult<List<ActivityVoteOptionAnalysisVO>> getActivityVoteOptionAnalysis(@Valid @RequestBody ActivityVoteLogEventReqVO reqVO) {
        return success(activityVoteService.getActivityVoteOptionAnalysis(reqVO));
    }

    @PostMapping("/getVoteLogList")
    @Operation(summary = "获取投票记录列表")
    public CommonResult<PageResult<ActivityVoteLogRespVO>> getVoteLogList(@Valid @RequestBody ActivityVoteLogPageReqVO reqVO) {
        return success(activityVoteService.getVoteLogList(reqVO));
    }

    @PostMapping("/getVoteLogCount")
    @Operation(summary = "投票参与人数、投票次数统计")
    public CommonResult<ActivityVoteStatisticsRespVO> getVoteLogCount(@Valid @RequestBody ActivityVoteLogPageReqVO reqVO) {
        return success(activityVoteService.getVoteLogCount(reqVO));
    }

    @PostMapping("/getRewardLogList")
    @Operation(summary = "获取奖励发放记录")
    public CommonResult<PageResult<ActivityVoteRewardLogRespVO>> getRewardLogList(@Valid @RequestBody ActivityVoteRewardLogPageReqVO reqVO) {
        return success(activityVoteService.getRewardLogList(reqVO));
    }

    @PostMapping("/getRewardLogCount")
    @Operation(summary = "发放人数、发放次数统计")
    public CommonResult<ActivityVoteRewardStatisticsRespVO> getRewardLogCount(@Valid @RequestBody ActivityVoteRewardLogPageReqVO reqVO) {
        return success(activityVoteService.getRewardLogCount(reqVO));
    }

    @PostMapping("/exportVoteLog")
    @Operation(summary = "导出投票记录")
    public CommonResult<String> exportVoteLog(@Valid @RequestBody ActivityVoteLogPageReqVO reqVO) {
        activityVoteService.exportVoteLog(reqVO);
        return success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/exportRewardLog")
    @Operation(summary = "导出发放记录")
    public CommonResult<String> exportRewardLog(@Valid @RequestBody ActivityVoteRewardLogPageReqVO reqVO) {
        activityVoteService.exportRewardLog(reqVO);
        return success("数据下载中,请稍后到下载管理中查看..");
    }

    @PostMapping("/updateExpress")
    @Operation(summary = "奖励记录填写发货信息")
    public CommonResult<Integer> updateExpress(@Valid @RequestBody ActivityVoteUpdateExpressReqVO reqVO) {
        return success(activityVoteService.updateExpress(reqVO));
    }

    @GetMapping("/selectActivitySpread")
    @Operation(summary = "查询投票活动推广")
    public CommonResult<ActivityVoteSpreadRespVO> selectActivitySpread(@RequestParam("id") Long id) {
        return success(activityVoteService.selectSpread(id));
    }

    @PostMapping("/updateSpread")
    @Operation(summary = "修改投票活动推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivityVoteSpreadSaveReqVO reqVO) {
        activityVoteService.updateSpread(reqVO);
        return success(true);
    }

    @GetMapping("/getActivityVoteList")
    @Operation(summary = "获取投票活动列表")
    public CommonResult<List<ActivityVoteDetailRespVO>> getActivityVoteList() {
        return success(activityVoteService.getActivityVoteList());
    }

    @PostMapping("/updateState")
    @Operation(summary = "投票活动启用/禁用")
    public CommonResult<Integer> updateState(@Valid @RequestBody ActivityVoteStateReqVO reqVO) {
        return success(activityVoteService.updateState(reqVO));
    }
}

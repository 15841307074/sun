package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerAnalysisReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerDetailRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerQuestionAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerRecordPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerRecordRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerRewardLogPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerRewardLogRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerRewardStatisticsRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerSaveOrUpdateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerSpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerSpreadSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerStateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerStatisticsRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerUpdateExpressReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerDO;
import com.htyoudao.youdao.module.promotion.service.activityAnswer.ActivityAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台 PC - 有奖问答活动")
@RestController
@RequestMapping("/promotion/activityAnswer")
public class ActivityAnswerController {

    @Resource
    private ActivityAnswerService activityAnswerService;

    /**
     * 创建有奖问答活动。
     */
    @PostMapping("/create")
    @Operation(summary = "创建有奖问答活动")
    public CommonResult<Integer> createActivityAnswer(@Valid @RequestBody ActivityAnswerSaveOrUpdateReqVO reqVO) {
        return activityAnswerService.createActivityAnswer(reqVO);
    }

    /**
     * 修改有奖问答活动。
     */
    @PostMapping("/update")
    @Operation(summary = "修改有奖问答活动")
    public CommonResult<Integer> updateActivityAnswer(@Valid @RequestBody ActivityAnswerSaveOrUpdateReqVO reqVO) {
        return activityAnswerService.updateActivityAnswer(reqVO);
    }

    /**
     * 删除有奖问答活动。
     */
    @GetMapping("/delete")
    @Operation(summary = "删除有奖问答活动")
    public CommonResult<Integer> deleteActivityAnswer(@RequestParam("id") Long id) {
        return activityAnswerService.deleteActivityAnswer(id);
    }

    /**
     * 复制有奖问答活动。
     */
    @PostMapping("/copy")
    @Operation(summary = "复制有奖问答活动")
    public CommonResult<Integer> copyActivityAnswer(@Valid @RequestBody ActivityAnswerSaveOrUpdateReqVO reqVO) {
        return activityAnswerService.copyActivityAnswer(reqVO);
    }

    /**
     * 查询有奖问答活动详情。
     */
    @GetMapping("/selectDetail")
    @Operation(summary = "查询有奖问答活动详情")
    public CommonResult<ActivityAnswerDetailRespVO> selectDetail(@RequestParam("id") Long id) {
        return success(activityAnswerService.selectDetail(id));
    }

    /**
     * 分页查询有奖问答活动。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询有奖问答活动")
    public CommonResult<PageResult<ActivityAnswerDetailRespVO>> getActivityAnswerPage(@Valid @RequestBody ActivityAnswerPageReqVO reqVO) {
        PageResult<ActivityAnswerDO> page = activityAnswerService.getActivityAnswerPage(reqVO);
        return success(BeanUtils.toBean(page, ActivityAnswerDetailRespVO.class));
    }

    /**
     * 更新有奖问答活动启用状态。
     */
    @PostMapping("/updateState")
    @Operation(summary = "有奖问答活动启用/禁用")
    public CommonResult<Integer> updateState(@Valid @RequestBody ActivityAnswerStateReqVO reqVO) {
        return success(activityAnswerService.updateState(reqVO));
    }

    /**
     * 获取有奖问答活动概览数据。
     */
    @GetMapping("/getActivityAnswerAnalysis/{id}")
    @Operation(summary = "获取有奖问答活动数据概览")
    public CommonResult<Map<String, Object>> getActivityAnswerAnalysis(@PathVariable("id") Long id) {
        return success(activityAnswerService.getActivityAnswerAnalysis(id));
    }

    /**
     * 获取有奖问答每日趋势数据。
     */
    @PostMapping("/getActivityAnswerDailyAnalysis")
    @Operation(summary = "获取有奖问答每日趋势数据")
    public CommonResult<Map<String, Object>> getActivityAnswerDailyAnalysis(@Valid @RequestBody ActivityAnswerAnalysisReqVO reqVO) {
        return success(activityAnswerService.getActivityAnswerDailyAnalysis(reqVO));
    }

    /**
     * 获取有奖问题目分析。
     */
    @PostMapping("/questionAnalysis")
    @Operation(summary = "获取有奖问题目分析")
    public CommonResult<List<ActivityAnswerQuestionAnalysisRespVO>> getQuestionAnalysis(@Valid @RequestBody ActivityAnswerAnalysisReqVO reqVO) {
        return success(activityAnswerService.getQuestionAnalysis(reqVO));
    }

    /**
     * 分页查询答题记录。
     */
    @PostMapping("/record/page")
    @Operation(summary = "分页查询答题记录")
    public CommonResult<PageResult<ActivityAnswerRecordRespVO>> getRecordPage(@Valid @RequestBody ActivityAnswerRecordPageReqVO reqVO) {
        return success(activityAnswerService.getRecordPage(reqVO));
    }

    /**
     * 统计答题参与数据。
     */
    @PostMapping("/record/count")
    @Operation(summary = "统计答题参与数据")
    public CommonResult<ActivityAnswerStatisticsRespVO> getRecordCount(@Valid @RequestBody ActivityAnswerRecordPageReqVO reqVO) {
        return success(activityAnswerService.getRecordCount(reqVO));
    }

    /**
     * 导出答题记录。
     */
    @PostMapping("/record/export")
    @Operation(summary = "导出有奖问答答题记录")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportRecord(@Valid @RequestBody ActivityAnswerRecordPageReqVO reqVO) {
        activityAnswerService.exportRecord(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 分页查询奖励发放记录。
     */
    @PostMapping("/reward/page")
    @Operation(summary = "分页查询奖励发放记录")
    public CommonResult<PageResult<ActivityAnswerRewardLogRespVO>> getRewardLogPage(@Valid @RequestBody ActivityAnswerRewardLogPageReqVO reqVO) {
        return success(activityAnswerService.getRewardLogPage(reqVO));
    }

    /**
     * 统计奖励发放人数和发放次数。
     */
    @PostMapping("/reward/count")
    @Operation(summary = "统计有奖问答奖励发放数据")
    public CommonResult<ActivityAnswerRewardStatisticsRespVO> getRewardLogCount(@Valid @RequestBody ActivityAnswerRewardLogPageReqVO reqVO) {
        return success(activityAnswerService.getRewardLogCount(reqVO));
    }

    /**
     * 导出奖励发放记录。
     */
    @PostMapping("/reward/export")
    @Operation(summary = "导出有奖问答奖励发放记录")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportRewardLog(@Valid @RequestBody ActivityAnswerRewardLogPageReqVO reqVO) {
        activityAnswerService.exportRewardLog(reqVO);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 修改实物奖品快递信息。
     */
    @PostMapping("/reward/updateExpress")
    @Operation(summary = "修改实物奖品快递信息")
    public CommonResult<Integer> updateExpress(@Valid @RequestBody ActivityAnswerUpdateExpressReqVO reqVO) {
        return success(activityAnswerService.updateExpress(reqVO));
    }

    /**
     * 查询有奖问答活动推广配置。
     */
    @GetMapping("/selectActivitySpread")
    @Operation(summary = "查询有奖问答活动推广")
    public CommonResult<ActivityAnswerSpreadRespVO> selectActivitySpread(@RequestParam("id") Long id) {
        return success(activityAnswerService.selectSpread(id));
    }

    /**
     * 修改有奖问答活动推广配置。
     */
    @PostMapping("/updateSpread")
    @Operation(summary = "修改有奖问答活动推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivityAnswerSpreadSaveReqVO reqVO) {
        activityAnswerService.updateSpread(reqVO);
        return success(true);
    }

}

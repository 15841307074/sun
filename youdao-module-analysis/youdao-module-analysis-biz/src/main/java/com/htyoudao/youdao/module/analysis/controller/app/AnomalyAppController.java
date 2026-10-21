package com.htyoudao.youdao.module.analysis.controller.app;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.AnomalyReportRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.ComplaintRatioRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.FoodHygieneComplaintRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.PlatformMessageRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyUserReportDO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.PlatformMessageDO;
import com.htyoudao.youdao.module.analysis.enums.AnomalyTypeEnum;
import com.htyoudao.youdao.module.analysis.framework.config.anomaly.AnomalyProperties;
import com.htyoudao.youdao.module.analysis.service.anomaly.PlatformMessageService;
import com.htyoudao.youdao.module.analysis.service.anomaly.ComplaintRankingService;
import com.htyoudao.youdao.module.analysis.service.anomaly.StoreOrgStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * App 接口 — 经营异常提醒
 *
 * <p>3 个接口：
 * <ol>
 *   <li>GET  /analysis/app/platform-message/page — 平台消息分页</li>
 *   <li>GET  /analysis/app/anomaly-report/page    — 经营异常报告分页</li>
 *   <li>PUT  /analysis/app/platform-message/read  — 标记已读</li>
 * </ol>
 *
 * <p>都只查自己的数据，需要登录。
 */
@Tag(name = "App - 经营异常提醒")
@RestController
@RequestMapping("/analysis/app")
public class AnomalyAppController {

    @Resource
    private PlatformMessageService messageService;
    @Resource
    private AnomalyProperties properties;
    @Resource
    private StoreOrgStatisticsService storeOrgStatisticsService;
    @Resource
    private ComplaintRankingService complaintRankingService;

    // ==================== 接口 1：平台消息分页 ====================

    @GetMapping("/platform-message/page")
    @Operation(summary = "平台消息查询")
    public CommonResult<PageResult<PlatformMessageRespVO>> getPlatformMessagePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "消息类型筛选（可选）") @RequestParam(required = false) Integer messageType) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        PageResult<PlatformMessageDO> result = messageService.getPlatformMessagePage(userId, pageParam, messageType);
        // 转 VO
        List<PlatformMessageRespVO> voList = result.getList().stream().map(this::toMessageVO).toList();
        return success(new PageResult<>(voList, result.getTotal()));
    }

    // ==================== 接口 2：经营异常报告分页 ====================

    @GetMapping("/anomaly-report/page")
    @Operation(summary = "经营异常消息查询")
    public CommonResult<PageResult<AnomalyReportRespVO>> getAnomalyReportPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "异常类型筛选（可选）") @RequestParam(required = false) Integer anomalyType) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        PageResult<AnomalyUserReportDO> result = messageService.getAnomalyReportPage(userId, pageParam, anomalyType);
        // 转 VO
        List<AnomalyReportRespVO> voList = result.getList().stream().map(this::toReportVO).toList();
        return success(new PageResult<>(voList, result.getTotal()));
    }

    // ==================== 接口 3：标记已读 ====================

    @PutMapping("/platform-message/read")
    @Operation(summary = "标记消息已读")
    public CommonResult<Boolean> markRead(
            @Parameter(description = "消息ID列表", required = true) @RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.markRead(userId, ids);
        return success(true);
    }

    // ==================== 接口 4：实时门店组织统计 ====================

    @GetMapping("/anomaly-report/org-store-statistics")
    @Operation(summary = "查询当前登录组织的实时门店统计")
    public CommonResult<StoreOrgStatisticsRespVO> getOrgStoreStatistics() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(storeOrgStatisticsService.getStatistics(userId));
    }

    // ==================== 接口 5：投诉异常排行 ====================

    @GetMapping("/anomaly-report/complaint-ratio-ranking")
    @Operation(summary = "查询投诉订单占比排行")
    public CommonResult<List<ComplaintRatioRankingRespVO>> getComplaintRatioRanking(
            @Parameter(description = "开始日期，格式 yyyy-MM-dd", required = true)
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式 yyyy-MM-dd", required = true)
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "排行类型：TOP 或 BOTTOM", required = true)
            @RequestParam("rankType") String rankType) {
        return success(complaintRankingService.getComplaintRatioRanking(
                SecurityFrameworkUtils.getLoginUserId(), startDate, endDate, rankType));
    }

    @GetMapping("/anomaly-report/food-hygiene-complaint-ranking")
    @Operation(summary = "查询食品卫生投诉排行")
    public CommonResult<List<FoodHygieneComplaintRankingRespVO>> getFoodHygieneComplaintRanking(
            @Parameter(description = "开始日期，格式 yyyy-MM-dd", required = true)
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式 yyyy-MM-dd", required = true)
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "排行类型：TOP 或 BOTTOM", required = true)
            @RequestParam("rankType") String rankType) {
        return success(complaintRankingService.getFoodHygieneComplaintRanking(
                SecurityFrameworkUtils.getLoginUserId(), startDate, endDate, rankType));
    }

    // ==================== VO 转换 ====================

    private PlatformMessageRespVO toMessageVO(PlatformMessageDO message) {
        PlatformMessageRespVO vo = new PlatformMessageRespVO();
        vo.setId(message.getId().toString());
        vo.setMessageType(message.getMessageType());
        vo.setPeriodStart(message.getPeriodStart().toString());
        vo.setContent(message.getContent());
        vo.setActionText(properties.getMessage().getActionText());
        vo.setReadStatus(message.getReadStatus());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private AnomalyReportRespVO toReportVO(AnomalyUserReportDO report) {
        AnomalyReportRespVO vo = new AnomalyReportRespVO();
        vo.setId(report.getId().toString());
        vo.setAnomalyType(report.getAnomalyType());
        vo.setPeriodStart(report.getPeriodStart().toString());
        vo.setStoreCount(report.getStoreCount());
        // 周度（type=1）有 anomalyMetricCount，月度为 null
        // 月度（type=2/3）有 anomalyStoreCount，周度为 null
        if (report.getAnomalyType() != null
                && report.getAnomalyType().equals(AnomalyTypeEnum.WEEK_ANOMALY.getType())) {
            vo.setAnomalyMetricCount(report.getAnomalyMetricCount());
            vo.setAnomalyStoreCount(null);
        } else {
            vo.setAnomalyMetricCount(null);
            vo.setAnomalyStoreCount(report.getAnomalyStoreCount());
        }
        return vo;
    }
}

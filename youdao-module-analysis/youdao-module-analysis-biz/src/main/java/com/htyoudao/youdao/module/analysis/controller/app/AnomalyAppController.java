package com.htyoudao.youdao.module.analysis.controller.app;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.AnomalyReportRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.PlatformMessageRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyUserReportDO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.PlatformMessageDO;
import com.htyoudao.youdao.module.analysis.enums.AnomalyTypeEnum;
import com.htyoudao.youdao.module.analysis.framework.config.anomaly.AnomalyProperties;
import com.htyoudao.youdao.module.analysis.service.anomaly.PlatformMessageService;
import com.htyoudao.youdao.module.analysis.service.anomaly.StoreOrgStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    // ==================== 接口 4：月度门店组织统计 ====================

    @GetMapping("/anomaly-report/org-store-statistics")
    @Operation(summary = "查询月度门店组织统计")
    public CommonResult<StoreOrgStatisticsRespVO> getOrgStoreStatistics(
            @Parameter(description = "组织ID", required = true)
            @RequestParam("orgId") Long orgId,
            @Parameter(description = "月份，格式 yyyy-MM", required = true, example = "2026-08")
            @RequestParam("month") String month) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(storeOrgStatisticsService.getStatistics(userId, orgId, month));
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

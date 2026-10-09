package com.htyoudao.youdao.module.promotion.controller.admin.activitySign;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo.*;
import com.htyoudao.youdao.module.promotion.service.activitySign.ActivitySignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台pc - 签到活动")
@RestController
@RequestMapping("/promotion/activity-sign")
public class ActivitySignController {

  @Resource private ActivitySignService activitySignService;

  /** 创建签到活动：保存活动主表、签到扩展配置、奖励配置、适用门店，并写入门店命中缓存和推广渠道。参数 reqVO 为 PC 新建活动 JSON 结构。 */
  @PostMapping("/create")
  @Operation(summary = "创建签到活动")
  public CommonResult<Boolean> create(@Valid @RequestBody ActivitySignSaveReqVO reqVO) {
    return success(activitySignService.create(reqVO));
  }

  /** 修改签到活动：覆盖活动基础信息、签到配置、奖励配置和门店关系；注意活动期间修改规则后只对后续计算即时生效，不追溯旧奖励。参数 reqVO 必须带 id。 */
  @PostMapping("/update")
  @Operation(summary = "修改签到活动")
  public CommonResult<Boolean> update(@Valid @RequestBody ActivitySignSaveReqVO reqVO) {
    return success(activitySignService.update(reqVO));
  }

  /** 查询签到活动详情：组装活动基础信息、签到配置、门店列表、奖励列表给 PC 回显。参数 id 为活动ID。 */
  @GetMapping("/get")
  @Operation(summary = "获取签到活动详情")
  public CommonResult<ActivitySignRespVO> get(@RequestParam("id") Long id) {
    return success(activitySignService.get(id));
  }

  /** 启停签到活动：同步活动状态，并维护门店命中 Redis 缓存。参数 enabled=1 表示启用。 */
  @PostMapping("/update-status")
  @Operation(summary = "启用/停用签到活动")
  public CommonResult<Boolean> updateStatus(
      @Valid @RequestBody ActivitySignStatusUpdateReqVO reqVO) {
    return success(activitySignService.updateStatus(reqVO));
  }

  /** 删除签到活动：删除活动并清理门店命中缓存。参数 id 为活动ID。 */
  @GetMapping("/delete")
  @Operation(summary = "删除签到活动")
  public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
    return success(activitySignService.delete(id));
  }

  /** 查询签到活动推广配置：返回分享标题、描述、图片。参数 id 为活动ID。 */
  @GetMapping("/selectActivitySpread")
  @Operation(summary = "查询签到活动推广")
  public CommonResult<ActivitySignSpreadRespVO> selectActivitySpread(@RequestParam("id") Long id) {
    return success(activitySignService.selectActivitySpread(id));
  }

  /** 修改签到活动推广配置：只更新分享标题、描述、图片。参数 reqVO 为 PC 推广配置 JSON。 */
  @PostMapping("/updateSpread")
  @Operation(summary = "修改签到活动推广")
  public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivitySignSpreadSaveReqVO reqVO) {
    return success(activitySignService.updateSpread(reqVO));
  }

  /** PC 数据分析汇总：优先查 ES，失败或强制 MySQL 时降级查分表。参数 forceMysql=true 仅后端调试使用。 */
  @PostMapping("/analysis/summary")
  @Operation(summary = "签到活动数据分析汇总")
  public CommonResult<ActivitySignAnalysisSummaryRespVO> analysisSummary(
      @Valid @RequestBody ActivitySignAnalysisSummaryReqVO reqVO) {
    return success(activitySignService.analysisSummary(reqVO));
  }


  /** PC 数据分析折线图：按日期返回 PV/UV，优先查 ES，失败或 forceMysql=true 时降级 MySQL。 */
  @PostMapping("/analysis/daily")
  @Operation(summary = "签到活动数据分析折线图")
  public CommonResult<ActivitySignDailyAnalysisRespVO> dailyAnalysis(
      @Valid @RequestBody ActivitySignDailyAnalysisReqVO reqVO) {
    return success(activitySignService.dailyAnalysis(reqVO));
  }

  /** PC 签到记录分页：优先查 ES，失败降级 MySQL 分表；continuousCycleDays 用于计算连签周期次数。 */
  @PostMapping("/record/page")
  @Operation(summary = "签到记录分页")
  public CommonResult<ActivitySignRecordPageRespVO> recordPage(
      @Valid @RequestBody ActivitySignRecordPageReqVO reqVO) {
    return success(activitySignService.recordPage(reqVO));
  }

  /** 签到记录真实导出：按相同筛选条件异步生成 Excel，超过 30W 时 service 会直接抛业务异常。 */
  @PostMapping("/record/export")
  @Operation(summary = "签到记录导出")
  public CommonResult<String> recordExport(@Valid @RequestBody ActivitySignRecordExportReqVO reqVO)
      throws com.htyoudao.youdao.framework.common.exception.ServerException {
    activitySignService.recordExport(reqVO);
    return success("数据下载中,请稍后到下载管理中查看..");
  }

  /** PC 奖励发放记录分页：优先查 ES，失败降级 MySQL 分表；筛选项与蓝湖底表一致。 */
  @PostMapping("/reward-record/page")
  @Operation(summary = "发放记录分页")
  public CommonResult<ActivitySignRewardRecordPageRespVO> rewardRecordPage(
      @Valid @RequestBody ActivitySignRewardRecordPageReqVO reqVO) {
    return success(activitySignService.rewardRecordPage(reqVO));
  }

  /** PC 填写/修改实物奖品快递单号：同一个接口同时支持首次填写和后续修改。 */
  @PostMapping("/updateExpress")
  @Operation(summary = "发放记录填写/修改快递单号")
  public CommonResult<Integer> updateExpress(
      @Valid @RequestBody ActivitySignUpdateExpressReqVO reqVO) {
    return success(activitySignService.updateExpress(reqVO));
  }

  /** 奖励发放记录真实导出：按相同筛选条件异步生成 Excel，超过 30W 时 service 会直接抛业务异常。 */
  @PostMapping("/reward-record/export")
  @Operation(summary = "发放记录导出")
  public CommonResult<String> rewardRecordExport(
      @Valid @RequestBody ActivitySignRewardRecordExportReqVO reqVO)
      throws com.htyoudao.youdao.framework.common.exception.ServerException {
    activitySignService.rewardRecordExport(reqVO);
    return success("数据下载中,请稍后到下载管理中查看..");
  }

  /** 红包回调状态更新入口：给微信回调同事按 outBillNo 更新未领取/已领取/已过期状态。 */
  @PostMapping("/red-packet/claim-status/update")
  @Operation(summary = "更新签到红包领取状态")
  public CommonResult<Boolean> updateRedPacketClaimStatus(
      @Valid @RequestBody ActivitySignRedPacketClaimStatusUpdateReqVO reqVO) {
    return success(
        activitySignService.updateSignRedPacketClaimStatusByOutBillNo(
            reqVO.getOutBillNo(), reqVO.getClaimStatus(), reqVO.getFailReason()));
  }
}

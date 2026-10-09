package com.htyoudao.youdao.module.promotion.service.activitySign;

import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo.*;

public interface ActivitySignService {
  /** 创建签到活动；reqVO 为 PC 新建活动 JSON，包含基础信息、门店、图片、重置规则、奖励配置。 */
  Boolean create(ActivitySignSaveReqVO reqVO);

  /** 修改签到活动；reqVO 必须带 id，保存成功后即时生效，不追溯历史奖励。 */
  Boolean update(ActivitySignSaveReqVO reqVO);

  /** 查询 PC 活动详情；id 为活动ID，返回活动配置、门店列表和奖励列表。 */
  ActivitySignRespVO get(Long id);

  /** 启停活动；enabled=1 启用，停用后需要同步清理门店命中缓存。 */
  Boolean updateStatus(ActivitySignStatusUpdateReqVO reqVO);

  /** 删除活动；id 为活动ID，同时清理相关命中缓存。 */
  Boolean delete(Long id);

  /** 查询推广配置；id 为活动ID，返回分享标题、说明和图片。 */
  ActivitySignSpreadRespVO selectActivitySpread(Long id);

  /** 修改推广配置；reqVO 为推广配置 JSON。 */
  Boolean updateSpread(ActivitySignSpreadSaveReqVO reqVO);

  /** PC 数据分析汇总；默认 ES 优先，forceMysql=true 时直查 MySQL。 */
  ActivitySignAnalysisSummaryRespVO analysisSummary(ActivitySignAnalysisSummaryReqVO reqVO);


  /** PC 数据分析折线图；按日期返回 PV/UV，ES 失败降级 MySQL。 */
  ActivitySignDailyAnalysisRespVO dailyAnalysis(ActivitySignDailyAnalysisReqVO reqVO);

  /** PC 签到记录分页；activityId 和 continuousCycleDays 必填，ES 失败降级 MySQL。 */
  ActivitySignRecordPageRespVO recordPage(ActivitySignRecordPageReqVO reqVO);

  /** 签到记录真实导出；超过 30W 抛业务异常，exportSource=999 手动绕过。 */
  void recordExport(ActivitySignRecordExportReqVO reqVO) throws ServerException;

  /** PC 奖励发放记录分页；筛选项按蓝湖底表，ES 失败降级 MySQL。 */
  ActivitySignRewardRecordPageRespVO rewardRecordPage(ActivitySignRewardRecordPageReqVO reqVO);

  /** 奖励发放记录真实导出；超过 30W 抛业务异常，exportSource=999 手动绕过。 */
  void rewardRecordExport(ActivitySignRewardRecordExportReqVO reqVO) throws ServerException;

  /** PC 更新实物奖品物流信息；reqVO 需要奖励记录 id 和收货/物流字段。 */
  Integer updateExpress(ActivitySignUpdateExpressReqVO reqVO);

  /** 小程序首页命中/进度；storeId 为当前门店，forceMysql 为后端隐藏调试参数。 */
  AppActivitySignHitRespVO hit(Long storeId, Boolean forceMysql);

  /** 小程序活动详情；reqVO 包含 activityId、storeId。 */
  AppActivitySignDetailRespVO detail(AppActivitySignReqVO reqVO);

  /** 小程序立即签到；按手机号维度防重，奖励发给当前 memberId。 */
  AppActivitySignResultRespVO sign(AppActivitySignReqVO reqVO);

  /** 小程序签到日期列表；入参 activityId，返回该手机号已签到的所有日期。 */
  AppActivitySignCalendarRespVO signRecord(AppActivitySignRecordReqVO reqVO);

  /** 小程序我的奖励；activityId 为活动ID，按当前手机号查询。 */
  AppActivitySignMyRewardRespVO myRewards(Long activityId);

  /** 小程序填写实物地址；reqVO 需要 rewardRecordId 和收货信息。 */
  Boolean savePhysicalAddress(AppActivitySignAddressReqVO reqVO);

  /** 定时刷新实物奖品地址超时状态；发放超过48小时仍未填写地址的记录更新为 prizeState=9。 */
  Integer refreshPhysicalAddressTimeout();

  /** 红包回调状态更新；按 outBillNo 更新 claimStatus，供微信回调同事调用。 */
  Boolean updateSignRedPacketClaimStatusByOutBillNo(
      String outBillNo,  String failReason);

  /** 红包回调状态更新；按 outBillNo 更新 claimStatus，供微信回调同事调用。 */
  Boolean updateSignRedPacketClaimStatusByOutBillNo(
          String outBillNo, Integer claimStatus, String failReason);

}

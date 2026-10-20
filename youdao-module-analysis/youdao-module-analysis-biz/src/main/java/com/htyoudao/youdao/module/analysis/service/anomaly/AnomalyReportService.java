package com.htyoudao.youdao.module.analysis.service.anomaly;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyUserReportDO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreAnomalyWeeklyDO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.UserStoreSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.AnomalyUserReportMapper;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.GoodsSalesAnomalyMonthlyMapper;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.GoodsZeroOrderAnomalyMonthlyMapper;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.UserStoreSnapshotMapper;
import com.htyoudao.youdao.module.analysis.enums.AnomalyTypeEnum;
import com.htyoudao.youdao.module.analysis.enums.PeriodTypeEnum;
import com.htyoudao.youdao.module.analysis.framework.config.anomaly.AnomalyProperties;
import com.htyoudao.youdao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 经营异常报告聚合服务
 *
 * <p>定时任务调，从上游异常表取异常门店，按人聚合出报告。
 *
 * <p>算法（周度/月度通用）：
 * <ol>
 *   <li>查上游表获取异常门店集合</li>
 *   <li>角色捞人（PermissionApi）</li>
 *   <li>从快照表查人店关系</li>
 *   <li>过滤出有异常门店的人</li>
 *   <li>计算 store_count / anomaly_metric_count / anomaly_store_count</li>
 *   <li>写入报告表（有则更新，无则插入）</li>
 * </ol>
 */
@Service
@Slf4j
public class AnomalyReportService {

    /** 用户异常报告表 Mapper，负责报告的查/写/更新 */
    @Resource
    private AnomalyUserReportMapper reportMapper;
    /** ClickHouse 周度异常查询服务，周度报告的数据源 */
    @Resource
    private ComplaintAnomalyClickHouseService weeklyClickHouseService;
    /** 货品销量月度异常表 Mapper，月度销量异常报告的数据源 */
    @Resource
    private GoodsSalesAnomalyMonthlyMapper salesMapper;
    /** 货品0订单月度异常表 Mapper，月度0订单异常报告的数据源 */
    @Resource
    private GoodsZeroOrderAnomalyMonthlyMapper zeroOrderMapper;
    /** 人店快照表 Mapper，用于查人店关系 */
    @Resource
    private UserStoreSnapshotMapper snapshotMapper;
    /** 经营异常相关配置（角色ID、白名单、消息文案等） */
    @Resource
    private AnomalyProperties properties;
    /** 权限 API（Dubbo 远程调用），用于按角色捞取用户列表 */
    @DubboReference
    private PermissionApi permissionApi;

    /**
     * 生成周度异常报告（anomalyType=1）
     *
     * @param periodStart 周一日期
     */
    public void generateWeekReport(LocalDate periodStart) {
        // 从业务上下文取当前 businessId，作为数据隔离条件
        Long businessId = BusinessContextHolder.getBusinessId();
        if (businessId == null) {
            log.warn("[generateWeekReport] BusinessContextHolder 无 businessId，跳过");
            return;
        }

        // 1. periodStart → stat_year + stat_week（用 ISO 周历换算）
        int statYear = periodStart.get(WeekFields.ISO.weekBasedYear());        // 周基年
        int statWeek = periodStart.get(WeekFields.ISO.weekOfWeekBasedYear());  // 当年第几周

        // 2. 查上游周表获取异常门店指标记录（按年+周+businessId过滤）
        List<StoreAnomalyWeeklyDO> weeklyList = weeklyClickHouseService
                .listActiveByWeek(businessId, statYear, statWeek);
        if (CollUtil.isEmpty(weeklyList)) {
            log.info("[generateWeekReport] {} 第{}周 无异常门店数据", statYear, statWeek);
            return;
        }

        // 按 store_id 分组（一个门店一行），重复时保留前者
        Map<Long, StoreAnomalyWeeklyDO> anomalyStoreMap = weeklyList.stream()
                .collect(Collectors.toMap(StoreAnomalyWeeklyDO::getStoreId, w -> w, (a, b) -> a));
        // 异常门店ID集合，用于后续与人店快照求交集
        Set<Long> anomalyStoreIds = anomalyStoreMap.keySet();

        // 3. 角色捞人
        Set<Long> userIds = getUserIds();
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        // 4. 从周快照查人店关系（周期=周 + 周期开始日 + 用户集合）
        List<UserStoreSnapshotDO> snapshots = snapshotMapper.selectListByPeriodAndUsers(
                PeriodTypeEnum.WEEK.getType(), periodStart, userIds);
        // 按 userId 分组，方便逐人聚合
        Map<Long, List<UserStoreSnapshotDO>> userSnapshotMap = snapshots.stream()
                .collect(Collectors.groupingBy(UserStoreSnapshotDO::getUserId));

        // 5. 逐人聚合（周度：anomaly_store_count 固定填 0，只用 metric_count）
        int count = 0; // 统计有异常的人数
        for (Long userId : userIds) {
            // 取该用户负责的门店快照
            List<UserStoreSnapshotDO> userStores = userSnapshotMap.get(userId);
            if (CollUtil.isEmpty(userStores)) {
                continue; // 该用户本周期无门店，跳过
            }
            // 该用户的异常门店：门店ID命中异常Map即取出，过滤掉无异常的
            List<StoreAnomalyWeeklyDO> userAnomalies = userStores.stream()
                    .map(s -> anomalyStoreMap.get(s.getStoreId()))
                    .filter(Objects::nonNull)
                    .toList();
            if (userAnomalies.isEmpty()) {
                continue; // 该用户名下门店均无异常，跳过
            }
            // store_count = 快照门店数（该用户负责的门店总数）
            int storeCount = userStores.size();
            // anomaly_metric_count = 异常门店涉及的指标 union 数
            int anomalyMetricCount = countMetricUnion(userAnomalies);

            // 写报告表（周度 anomalyStoreCount 传 0）
            saveOrUpdateReport(AnomalyTypeEnum.WEEK_ANOMALY, periodStart, userId,
                    storeCount, anomalyMetricCount, 0);
            count++;
        }
        log.info("[generateWeekReport] {} 第{}周 完成，{} 人有异常", statYear, statWeek, count);
    }

    /**
     * 生成月度异常报告（anomalyType=2 或 3）
     *
     * @param anomalyType 月度类型（MONTH_PRODUCT_SALES 或 MONTH_ZERO_ORDER）
     * @param periodStart 月1号
     */
    public void generateMonthReport(AnomalyTypeEnum anomalyType, LocalDate periodStart) {
        // 从业务上下文取当前 businessId，作为数据隔离条件
        Long businessId = BusinessContextHolder.getBusinessId();
        if (businessId == null) {
            log.warn("[generateMonthReport] BusinessContextHolder 无 businessId，跳过");
            return;
        }

        // 1. 查上游月表获取异常门店ID集合（按异常类型选不同数据源）
        Set<Long> anomalyStoreIds;
        if (anomalyType == AnomalyTypeEnum.MONTH_PRODUCT_SALES) {
            // 货品销量异常：查销量异常月表的去重门店ID
            List<Long> ids = salesMapper.selectDistinctStoreIds(periodStart, businessId);
            anomalyStoreIds = ids == null ? Collections.emptySet() : new HashSet<>(ids);
        } else if (anomalyType == AnomalyTypeEnum.MONTH_ZERO_ORDER) {
            // 货品0订单异常：查0订单异常月表的去重门店ID
            List<Long> ids = zeroOrderMapper.selectDistinctStoreIds(periodStart, businessId);
            anomalyStoreIds = ids == null ? Collections.emptySet() : new HashSet<>(ids);
        } else {
            // 非月度类型不处理
            log.warn("[generateMonthReport] 不支持的月度类型: {}", anomalyType);
            return;
        }

        if (anomalyStoreIds.isEmpty()) {
            log.info("[generateMonthReport] {} {} 无异常门店数据", anomalyType, periodStart);
            return;
        }

        // 2. 角色捞人
        Set<Long> userIds = getUserIds();
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        // 3. 从月快照查人店关系（周期=月 + 周期开始日 + 用户集合）
        List<UserStoreSnapshotDO> snapshots = snapshotMapper.selectListByPeriodAndUsers(
                PeriodTypeEnum.MONTH.getType(), periodStart, userIds);
        // 按 userId 分组，方便逐人聚合
        Map<Long, List<UserStoreSnapshotDO>> userSnapshotMap = snapshots.stream()
                .collect(Collectors.groupingBy(UserStoreSnapshotDO::getUserId));

        // 4. 逐人聚合（月度：anomaly_metric_count 固定填 0，只用 store_count）
        int count = 0; // 统计有异常的人数
        for (Long userId : userIds) {
            // 取该用户负责的门店快照
            List<UserStoreSnapshotDO> userStores = userSnapshotMap.get(userId);
            if (CollUtil.isEmpty(userStores)) {
                continue; // 该用户本周期无门店，跳过
            }
            // 该用户的异常门店数：门店ID命中异常集合即计数
            long anomalyStoreCount = userStores.stream()
                    .map(UserStoreSnapshotDO::getStoreId)
                    .filter(anomalyStoreIds::contains)
                    .count();
            if (anomalyStoreCount == 0) {
                continue; // 该用户名下门店均无异常，跳过
            }
            // store_count = 快照门店数（该用户负责的门店总数）
            int storeCount = userStores.size();

            // 写报告表（月度 anomalyMetricCount 传 0）
            saveOrUpdateReport(anomalyType, periodStart, userId,
                    storeCount, 0, (int) anomalyStoreCount);
            count++;
        }
        log.info("[generateMonthReport] {} {} 完成，{} 人有异常", anomalyType, periodStart, count);
    }

    // ==================== 内部方法 ====================

    /**
     * 查角色下的用户ID集合
     */
    private Set<Long> getUserIds() {
        // 取推送角色ID，作为捞人依据
        Long roleId = properties.getPush().getRoleId();
        if (roleId == null) {
            // 角色未配置，无法捞人
            log.warn("[getUserIds] role-id 未配置");
            return Collections.emptySet();
        }
        // 调权限API按角色查用户ID集合（getCheckedData 校验并取数据）
        Set<Long> userIds = permissionApi.getUserRoleIdListByRoleIds(Set.of(roleId)).getCheckedData();
        if (CollUtil.isEmpty(userIds)) {
            // 角色下无用户
            log.info("[getUserIds] 角色 {} 下没有用户", roleId);
            return Collections.emptySet();
        }
        return userIds;
    }

    /**
     * 计算异常指标 union 数（周表用）
     * <p>遍历该用户所有异常门店的指标列，非空的取 union，返回 union 大小
     */
    private int countMetricUnion(List<StoreAnomalyWeeklyDO> anomalies) {
        // 用 Set 收集所有出现过的异常指标名（自动去重）
        Set<String> metricUnion = new HashSet<>();
        // 遍历该用户所有异常门店，非空的指标列加入 union
        for (StoreAnomalyWeeklyDO w : anomalies) {
            if (w.getWeeklyTurnover() != null) metricUnion.add("weekly_turnover");
            if (w.getPrevWeekTurnover() != null) metricUnion.add("prev_week_turnover");
            if (w.getWeeklyAvgOrderAmountChangeRate() != null) metricUnion.add("weekly_avg_order_amount_change_rate");
            if (w.getWeeklyAvgOrderAmount() != null) metricUnion.add("weekly_avg_order_amount");
            if (w.getPrevWeekAvgOrderAmount() != null) metricUnion.add("prev_week_avg_order_amount");
            if (w.getAvgOrderAmountChange() != null) metricUnion.add("avg_order_amount_change");
            if (w.getWeeklyRepeatCustomerCount() != null) metricUnion.add("weekly_repeat_customer_count");
            if (w.getWeeklyOrderCustomerCount() != null) metricUnion.add("weekly_order_customer_count");
            if (w.getWeeklyMealSalesQuantity() != null) metricUnion.add("weekly_meal_sales_quantity");
            if (w.getPrevWeekMealSalesQuantity() != null) metricUnion.add("prev_week_meal_sales_quantity");
            boolean hasComplaint = w.getComplaintOrderCount() != null && w.getComplaintOrderCount() > 0;
            if (w.getComplaintOrderRatio() != null && w.getComplaintOrderRatio().signum() > 0)
                metricUnion.add("complaint_order_ratio");
            if (hasComplaint) metricUnion.add("complaint_order_count");
            if (hasComplaint && w.getValidOrderCount() != null) metricUnion.add("valid_order_count");
            if (w.getFoodHygieneComplaintOrderCount() != null && w.getFoodHygieneComplaintOrderCount() > 0)
                metricUnion.add("food_hygiene_complaint_order_count");
        }
        // union 大小即异常指标种类数
        return metricUnion.size();
    }

    /**
     * 写报告表（有则更新统计数字，无则插入）
     */
    private void saveOrUpdateReport(AnomalyTypeEnum anomalyType, LocalDate periodStart,
                                     Long userId, int storeCount,
                                     int anomalyMetricCount, int anomalyStoreCount) {
        // 按唯一键（异常类型 + 周期开始日 + 用户ID）查是否已存在
        AnomalyUserReportDO existing = reportMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AnomalyUserReportDO>()
                        .eq(AnomalyUserReportDO::getAnomalyType, anomalyType.getType()) // 异常类型
                        .eq(AnomalyUserReportDO::getPeriodStart, periodStart)           // 周期开始日
                        .eq(AnomalyUserReportDO::getUserId, userId));                   // 用户ID
        if (existing != null) {
            // 已存在：只更新三个统计数字（重跑覆盖最新值）
            existing.setStoreCount(storeCount);
            existing.setAnomalyMetricCount(anomalyMetricCount);
            existing.setAnomalyStoreCount(anomalyStoreCount);
            reportMapper.updateById(existing);
        } else {
            // 不存在：新建报告记录
            AnomalyUserReportDO report = new AnomalyUserReportDO();
            report.setUserId(userId);
            report.setAnomalyType(anomalyType.getType());
            report.setPeriodStart(periodStart);
            report.setStoreCount(storeCount);
            report.setAnomalyMetricCount(anomalyMetricCount);
            report.setAnomalyStoreCount(anomalyStoreCount);
            reportMapper.insert(report);
        }
    }
}

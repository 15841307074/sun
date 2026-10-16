package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.ComplaintRatioRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.FoodHygieneComplaintRankingRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreAnomalyWeeklyDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.CH_QUERY_ERROR;

/**
 * 周度投诉异常计算入口。
 *
 * <p>投诉和订单明细从 ClickHouse 读取，周异常结果统一保存到 MySQL。</p>
 */
@Service
@Slf4j
public class ComplaintAnomalyClickHouseService {

    @Resource
    private ComplaintMetricClickHouseQueryService clickHouseQueryService;
    @Resource
    private ComplaintAnomalyWeeklyMysqlService weeklyMysqlService;

    /** 计算指定自然周的投诉指标，并合并写入 MySQL 周异常表。 */
    public int calculateAndSaveWeek(Long businessId, LocalDate periodStart) {
        WeekFields iso = WeekFields.ISO;
        int statYear = periodStart.get(iso.weekBasedYear());
        int statWeek = periodStart.get(iso.weekOfWeekBasedYear());
        LocalDate periodEnd = periodStart.plusWeeks(1);
        try {
            List<ComplaintMetricClickHouseQueryService.ComplaintMetric> metrics =
                    clickHouseQueryService.queryWeek(businessId, periodStart, periodEnd);
            return weeklyMysqlService.mergeWeek(businessId, statYear, statWeek, metrics);
        } catch (DataAccessException e) {
            log.error("[calculateAndSaveWeek] 周投诉指标计算失败, businessId={}, periodStart={}",
                    businessId, periodStart, e);
            throw e;
        }
    }

    /** 给周度报告任务读取 MySQL 当前有效记录。 */
    public List<StoreAnomalyWeeklyDO> listActiveByWeek(Long businessId, int statYear, int statWeek) {
        try {
            return weeklyMysqlService.listActiveByWeek(businessId, statYear, statWeek);
        } catch (DataAccessException e) {
            log.error("[listActiveByWeek] MySQL 周异常查询失败, businessId={}, year={}, week={}",
                    businessId, statYear, statWeek, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    public List<ComplaintRatioRankingRespVO> queryComplaintRatioRanking(
            Long businessId, Map<Long, String> storeNames, Collection<WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        try {
            return weeklyMysqlService.queryComplaintRatioRanking(businessId, storeNames, weeks, rankType);
        } catch (DataAccessException e) {
            log.error("[queryComplaintRatioRanking] MySQL 查询失败, businessId={}", businessId, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    public List<FoodHygieneComplaintRankingRespVO> queryFoodHygieneComplaintRanking(
            Long businessId, Map<Long, String> storeNames, Collection<WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        try {
            return weeklyMysqlService.queryFoodHygieneComplaintRanking(businessId, storeNames, weeks, rankType);
        } catch (DataAccessException e) {
            log.error("[queryFoodHygieneComplaintRanking] MySQL 查询失败, businessId={}", businessId, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    public record WeekKey(int statYear, int statWeek) {
    }
}

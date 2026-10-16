package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.ComplaintRatioRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.FoodHygieneComplaintRankingRespVO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.UserStoreRelationDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.COMPLAINT_RANKING_DATE_INVALID;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.COMPLAINT_RANKING_DATE_REQUIRED;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.COMPLAINT_RANKING_TYPE_INVALID;

/** 投诉异常排行的权限、日期口径和查询编排。 */
@Service
public class ComplaintRankingService {

    @DubboReference
    private StoreApi storeApi;
    @Resource
    private ComplaintAnomalyClickHouseService clickHouseService;

    public List<ComplaintRatioRankingRespVO> getComplaintRatioRanking(
            Long userId, LocalDate startDate, LocalDate endDate, String rankType) {
        QueryScope scope = resolveScope(userId, startDate, endDate, rankType);
        if (scope.storeNames().isEmpty()) {
            return List.of();
        }
        return clickHouseService.queryComplaintRatioRanking(scope.businessId(), scope.storeNames(),
                scope.weeks(), scope.rankType());
    }

    public List<FoodHygieneComplaintRankingRespVO> getFoodHygieneComplaintRanking(
            Long userId, LocalDate startDate, LocalDate endDate, String rankType) {
        QueryScope scope = resolveScope(userId, startDate, endDate, rankType);
        if (scope.storeNames().isEmpty()) {
            return List.of();
        }
        return clickHouseService.queryFoodHygieneComplaintRanking(scope.businessId(), scope.storeNames(),
                scope.weeks(), scope.rankType());
    }

    private QueryScope resolveScope(Long userId, LocalDate startDate, LocalDate endDate, String rankType) {
        if (startDate == null || endDate == null) {
            throw exception(COMPLAINT_RANKING_DATE_REQUIRED);
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0 || days > 90) {
            throw exception(COMPLAINT_RANKING_DATE_INVALID);
        }
        RankType resolvedRankType;
        try {
            resolvedRankType = RankType.valueOf(rankType == null ? "" : rankType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw exception(COMPLAINT_RANKING_TYPE_INVALID);
        }

        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        List<UserStoreRelationDTO> relations = storeApi
                .listUserStoreRelations(List.of(userId), null);
        Map<Long, String> storeNames = new LinkedHashMap<>();
        if (relations != null) {
            relations.stream()
                    .filter(relation -> relation.getStoreId() != null)
                    .forEach(relation -> storeNames.putIfAbsent(relation.getStoreId(), relation.getStoreName()));
        }
        return new QueryScope(businessId, storeNames, resolveWeeks(startDate, endDate), resolvedRankType);
    }

    /** 将任意日期范围扩展为所有命中的完整 ISO 自然周。 */
    static List<ComplaintAnomalyClickHouseService.WeekKey> resolveWeeks(LocalDate startDate, LocalDate endDate) {
        LocalDate monday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastMonday = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<ComplaintAnomalyClickHouseService.WeekKey> weeks = new ArrayList<>();
        WeekFields iso = WeekFields.ISO;
        for (LocalDate date = monday; !date.isAfter(lastMonday); date = date.plusWeeks(1)) {
            weeks.add(new ComplaintAnomalyClickHouseService.WeekKey(
                    date.get(iso.weekBasedYear()), date.get(iso.weekOfWeekBasedYear())));
        }
        return weeks;
    }

    public enum RankType {
        TOP, BOTTOM
    }

    private record QueryScope(Long businessId, Map<Long, String> storeNames,
                              Collection<ComplaintAnomalyClickHouseService.WeekKey> weeks,
                              RankType rankType) {
    }
}

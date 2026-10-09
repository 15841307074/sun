package com.htyoudao.youdao.module.analysis.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.PurchaseStoreStatsExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.PurchaseWarehouseStatsExcelVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.NoPurchaseDetailReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStoreStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.*;
import com.htyoudao.youdao.module.analysis.dal.es.EsField;
import com.htyoudao.youdao.module.analysis.enums.DateRangeMode;
import com.htyoudao.youdao.module.analysis.service.EsPurchaseQueryService;
import com.htyoudao.youdao.module.analysis.service.PurchaseStatsService;
import com.htyoudao.youdao.module.analysis.util.DateRangeUtil;
import com.htyoudao.youdao.module.analysis.util.ParallelUtil;
import com.htyoudao.youdao.module.analysis.util.PercentUtil;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreStatusLogDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PurchaseStatsServiceImpl implements PurchaseStatsService {

    private static final List<Integer> INVALID_ORDER_STATUS = List.of(0, 7, 70);
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long EXPORT_PAGE_SIZE = 2000L;

    @Resource
    private EsPurchaseQueryService es;

    @Resource
    private ExcelActionService<Object> excelActionService;

    @DubboReference
    private StoreApi storeApi;

    static class NoPurchaseSegment {
        LocalDate start;
        LocalDate end;
        long days;
    }

    @Override
    public WarehouseStatsResp warehouseStats(PurchaseStatsReq req) {
        DateRangeUtil.Range cur = DateRangeUtil.normalizeExcludeToday(req.getStartDate(), req.getEndDate());
        DateRangeUtil.Range mom = DateRangeUtil.momRange(cur);
        DateRangeUtil.Range yoy = DateRangeUtil.yoyRange(cur);

        WarehouseStatsResp resp = new WarehouseStatsResp();
        resp.setStartDate(cur.getStart().format(DF));
        resp.setEndDate(cur.getEnd().format(DF));
        resp.setDays(cur.getDaysInclusive());

        if (cur.getDaysInclusive() <= 0) {
            resp.setRows(Collections.emptyList());
            return resp;
        }

        // 1) 先把门店信息查出来（用于：warehouseId分组、仓库名、门店数/新增门店数、detail接口仓库名等）
        List<StoreInfoDTO> stores = this.queryStores(req.getStoreIds());
        Map<Long, List<StoreInfoDTO>> whToStores = stores.stream()
                .filter(s -> s.getWarehouseId() != null)
                .collect(Collectors.groupingBy(StoreInfoDTO::getWarehouseId));

        Map<Long, String> whNameMap = stores.stream()
                .filter(s -> s.getWarehouseId() != null)
                .collect(Collectors.toMap(StoreInfoDTO::getWarehouseId,
                        s -> Optional.ofNullable(s.getWarehouseName()).orElse(""),
                        (a, b) -> a));

        // 2) 按月：先计算十天未进货门店（用于剔除）
        Set<Long> excludedStoreIdsForMonth = Collections.emptySet();
        if (req.getMode() == DateRangeMode.MONTH) {
            excludedStoreIdsForMonth =  this.calcNoPurchase10dStoreIds(req.getStoreIds(), cur);
        }
        Set<Long> activeStoreIds = new HashSet<>(req.getStoreIds());
        if (req.getMode() == DateRangeMode.MONTH && !excludedStoreIdsForMonth.isEmpty()) {
            activeStoreIds.removeAll(excludedStoreIdsForMonth);
        }

        // 3) ES：current+mom 一次查（filters切割），yoy 单独查（并行）
        long curStart = DateRangeUtil.toEpochMillisStart(cur.getStart());
        long curEndEx = DateRangeUtil.toEpochMillisEndExclusive(cur.getEnd());
        long momStart = DateRangeUtil.toEpochMillisStart(mom.getStart());
        long momEndEx = DateRangeUtil.toEpochMillisEndExclusive(mom.getEnd());
        long yoyStart = DateRangeUtil.toEpochMillisStart(yoy.getStart());
        long yoyEndEx = DateRangeUtil.toEpochMillisEndExclusive(yoy.getEnd());

        Collection<Long> activeFilter = (req.getMode() == DateRangeMode.MONTH) ? activeStoreIds : null;

        CompletableFuture<EsPurchaseQueryService.AmountAggResult> curMomFuture =
                ParallelUtil.supplyAsync(() -> es.queryAmountByTermsWithMom(
                        EsField.WAREHOUSE_ID, EsField.WAREHOUSE_NAME,
                        req.getStoreIds(),
                        curStart, curEndEx,
                        momStart, momEndEx,
                        INVALID_ORDER_STATUS,
                        activeFilter
                ));

        CompletableFuture<Map<Long, Double>> yoyFuture =
                ParallelUtil.supplyAsync(() -> es.queryAmountByTermsYoy(
                        EsField.WAREHOUSE_ID, EsField.WAREHOUSE_NAME,
                        req.getStoreIds(),
                        yoyStart, yoyEndEx,
                        INVALID_ORDER_STATUS,
                        activeFilter
                ));

        EsPurchaseQueryService.AmountAggResult curMom = curMomFuture.join();
        Map<Long, Double> yoyAmt = yoyFuture.join();

        // 4) DB：新增门店数（按仓库分组）
        Map<Long, Long> newStoreCountByWh =  this.countNewStoresByWarehouse(whToStores, cur);

        // 5) DB：闭店门店数（不去重，同门店多次闭店都算）
        Map<Long, Long> closedCountByWh =  this.countClosedStoresByWarehouse(req.getStoreIds(), cur, stores);

        // 6) 组装仓库行
        List<WarehouseStatsRow> rows = new ArrayList<>();
        Set<Long> allWarehouseIds = new HashSet<>();
        allWarehouseIds.addAll(whToStores.keySet());
        allWarehouseIds.addAll(curMom.getCurrentAmount().keySet());
        allWarehouseIds.addAll(yoyAmt.keySet());

        for (Long whId : allWarehouseIds) {
            WarehouseStatsRow row = new WarehouseStatsRow();
            row.setWarehouseId(whId);
            row.setWarehouseName(Optional.ofNullable(curMom.getIdToName().get(whId))
                    .filter(s -> !s.isBlank())
                    .orElse(whNameMap.getOrDefault(whId, "")));

            // 门店数：按月要剔除十天未进货门店
            List<StoreInfoDTO> whStores = whToStores.getOrDefault(whId, Collections.emptyList());
            long storeCount = whStores.stream()
                    .map(StoreInfoDTO::getStoreId)
                    .filter(id -> req.getMode() != DateRangeMode.MONTH || activeStoreIds.contains(id))
                    .count();
            row.setStoreCount(whStores.size());

            row.setNewStoreCount(newStoreCountByWh.getOrDefault(whId, 0L));
            row.setClosedStoreCount(closedCountByWh.getOrDefault(whId, 0L));

            double curAmount = curMom.getCurrentAmount().getOrDefault(whId, 0D);
            double momAmount = curMom.getMomAmount().getOrDefault(whId, 0D);
            double yoyAmount = yoyAmt.getOrDefault(whId, 0D);

            row.setOrderTotalAmount(BigDecimal.valueOf(curAmount).setScale(2, RoundingMode.HALF_UP).doubleValue());

            // 门店日均货值：总值/门店数/天数
            double dailyAvg = 0D;
            if (storeCount > 0 && cur.getDaysInclusive() > 0) {
                dailyAvg = curAmount / storeCount / cur.getDaysInclusive();
            }
            row.setStoreDailyAvgAmount(BigDecimal.valueOf(dailyAvg).setScale(2, RoundingMode.HALF_UP).doubleValue());

            row.setMom(PercentUtil.percentChange(curAmount, momAmount));
            row.setYoy(PercentUtil.percentChange(curAmount, yoyAmount));

            // 按月统计时：十天未进货门店数（按仓库归属统计）
            if (req.getMode() == DateRangeMode.MONTH) {
                long noPurchaseCount = whStores.stream()
                        .map(StoreInfoDTO::getStoreId)
                        .filter(excludedStoreIdsForMonth::contains)
                        .count();
                row.setNoPurchase10dStoreCount(noPurchaseCount);
            } else {
                row.setNoPurchase10dStoreCount(0);
            }

            rows.add(row);
        }

        // 排序：订货总值降序
        rows.sort(Comparator.comparingDouble(WarehouseStatsRow::getOrderTotalAmount).reversed());
        resp.setRows(rows);
        return resp;
    }

    @Override
    public StoreStatsResp storeStats(PurchaseStoreStatsReq req) {
        DateRangeUtil.Range cur = DateRangeUtil.normalizeExcludeToday(req.getStartDate(), req.getEndDate());
        DateRangeUtil.Range mom = DateRangeUtil.momRange(cur);
        DateRangeUtil.Range yoy = DateRangeUtil.yoyRange(cur);

        StoreStatsResp resp = new StoreStatsResp();
        resp.setStartDate(cur.getStart().format(DF));
        resp.setEndDate(cur.getEnd().format(DF));
        resp.setDays(cur.getDaysInclusive());

        if (cur.getDaysInclusive() <= 0) {
            resp.setRows(Collections.emptyList());
            resp.setTotal(0);
            return resp;
        }

        // 门店信息（用于名字/仓库）
        List<StoreInfoDTO> stores = queryStores(req.getStoreIds());
        Map<Long, StoreInfoDTO> storeInfoMap = stores.stream()
                .collect(Collectors.toMap(StoreInfoDTO::getStoreId, s -> s, (a, b) -> a));

        // 按月剔除十天未进货门店
        Set<Long> excludedStoreIdsForMonth = Collections.emptySet();
        if (req.getMode() == DateRangeMode.MONTH) {
            excludedStoreIdsForMonth = calcNoPurchase10dStoreIds(req.getStoreIds(), cur);
        }
        Set<Long> activeStoreIds = new HashSet<>(req.getStoreIds());
        if (req.getMode() == DateRangeMode.MONTH && !excludedStoreIdsForMonth.isEmpty()) {
            activeStoreIds.removeAll(excludedStoreIdsForMonth);
        }

        long curStart = DateRangeUtil.toEpochMillisStart(cur.getStart());
        long curEndEx = DateRangeUtil.toEpochMillisEndExclusive(cur.getEnd());
        long momStart = DateRangeUtil.toEpochMillisStart(mom.getStart());
        long momEndEx = DateRangeUtil.toEpochMillisEndExclusive(mom.getEnd());
        long yoyStart = DateRangeUtil.toEpochMillisStart(yoy.getStart());
        long yoyEndEx = DateRangeUtil.toEpochMillisEndExclusive(yoy.getEnd());

        Collection<Long> activeFilter = (req.getMode() == DateRangeMode.MONTH) ? activeStoreIds : null;

        CompletableFuture<EsPurchaseQueryService.AmountAggResult> curMomFuture =
                ParallelUtil.supplyAsync(() -> es.queryAmountByTermsWithMom(
                        EsField.STORE_ID, EsField.STORE_NAME,
                        req.getStoreIds(),
                        curStart, curEndEx,
                        momStart, momEndEx,
                        INVALID_ORDER_STATUS,
                        activeFilter
                ));

        CompletableFuture<Map<Long, Double>> yoyFuture =
                ParallelUtil.supplyAsync(() -> es.queryAmountByTermsYoy(
                        EsField.STORE_ID, EsField.STORE_NAME,
                        req.getStoreIds(),
                        yoyStart, yoyEndEx,
                        INVALID_ORDER_STATUS,
                        activeFilter
                ));

        EsPurchaseQueryService.AmountAggResult curMom = curMomFuture.join();
        Map<Long, Double> yoyAmt = yoyFuture.join();

        List<Long> allStoreIds = req.getStoreIds();
        // 如果按月剔除，activeStoreIds 才是最终展示口径
        if (req.getMode() == DateRangeMode.MONTH) {
            allStoreIds = allStoreIds.stream().filter(activeStoreIds::contains).collect(Collectors.toList());
        }
        allStoreIds.sort((a, b) -> Double.compare(
                curMom.getCurrentAmount().getOrDefault(b, 0D),
                curMom.getCurrentAmount().getOrDefault(a, 0D)
        ));

        int total = allStoreIds.size();
        resp.setTotal(total);

        // 分页：这里用简单内存分页（你如果门店很多，建议改成 ES composite aggregation 分页）
        int from = Math.min((req.getPageNo() - 1) * req.getPageSize(), total);
        int to = Math.min(from + req.getPageSize(), total);
        List<Long> pageIds = allStoreIds.subList(from, to);

        List<StoreStatsRow> rows = new ArrayList<>();
        for (Long storeId : pageIds) {
            StoreStatsRow row = new StoreStatsRow();
            row.setStoreId(storeId);

            StoreInfoDTO si = storeInfoMap.get(storeId);
            row.setStoreName(si != null ? si.getStoreName() : curMom.getIdToName().get(storeId));
            row.setWarehouseId(si != null ? si.getWarehouseId() : null);
            row.setWarehouseName(si != null ? si.getWarehouseName() : null);

            double curAmount = curMom.getCurrentAmount().getOrDefault(storeId, 0D);
            double momAmount = curMom.getMomAmount().getOrDefault(storeId, 0D);
            double yoyAmount = yoyAmt.getOrDefault(storeId, 0D);

            row.setOrderTotalAmount(BigDecimal.valueOf(curAmount).setScale(2, RoundingMode.HALF_UP).doubleValue());
            row.setDailyAvgAmount(
                    cur.getDaysInclusive() > 0 ? BigDecimal.valueOf(curAmount / cur.getDaysInclusive()).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0D
            );
            row.setMom(PercentUtil.percentChange(curAmount, momAmount));
            row.setYoy(PercentUtil.percentChange(curAmount, yoyAmount));
            rows.add(row);
        }

        resp.setRows(rows);
        return resp;
    }

    @Override
    public void exportWarehouseStats(PurchaseStatsReq req) {
        Page<PurchaseWarehouseStatsExcelVO> page = new Page<>(1, EXPORT_PAGE_SIZE);
        excelActionService.exportAsyncExcel(
                PurchaseWarehouseStatsExcelVO.class,
                page,
                param -> param.getCurrent() > 1 ? Collections.emptyList() : buildWarehouseExportRows(req),
                "采购仓库统计"
        );
    }

    @Override
    public void exportStoreStats(PurchaseStoreStatsReq req) {
        Page<PurchaseStoreStatsExcelVO> page = new Page<>(1, EXPORT_PAGE_SIZE);
        excelActionService.exportAsyncExcel(
                PurchaseStoreStatsExcelVO.class,
                page,
                param -> param.getCurrent() > 1 ? Collections.emptyList() : buildStoreExportRows(req),
                "采购门店统计"
        );
    }

    @Override
    public NoPurchaseDetailResp noPurchase10dDetail(NoPurchaseDetailReq req) {
        DateRangeUtil.Range cur = DateRangeUtil.normalizeExcludeToday(req.getStartDate(), req.getEndDate());

        NoPurchaseDetailResp resp = new NoPurchaseDetailResp();
        resp.setWarehouseId(req.getWarehouseId());
        resp.setStartDate(cur.getStart().format(DF));
        resp.setEndDate(cur.getEnd().format(DF));

        if (cur.getDaysInclusive() <= 0) {
            resp.setRows(Collections.emptyList());
            return resp;
        }

        // 只查该仓库范围内的门店（并且受入参storeIds集合限制）
        List<StoreInfoDTO> whStores = storeApi.listStoresByWarehouse(req.getWarehouseId(), req.getStoreIds());

        if (whStores.isEmpty()) {
            resp.setRows(Collections.emptyList());
            return resp;
        }

        long startMs = DateRangeUtil.toEpochMillisStart(cur.getStart());
        long endEx = DateRangeUtil.toEpochMillisEndExclusive(cur.getEnd());

        Map<Long, List<LocalDate>> orderDaysMap =
                es.queryOrderDaysByStore(
                        whStores.stream().map(StoreInfoDTO::getStoreId).toList(),
                        startMs, endEx,
                        INVALID_ORDER_STATUS,
                        req.getWarehouseId()
                );

        String whName = whStores.get(0).getWarehouseName();

        List<NoPurchaseDetailRow> rows = new ArrayList<>();
        for (StoreInfoDTO s : whStores) {
            List<LocalDate> days = orderDaysMap.get(s.getStoreId());

            NoPurchaseSegment seg =
                    findRecentNoPurchaseSegment(days, cur.getStart(), cur.getEnd(), 10);

            if (seg == null) continue;

            NoPurchaseDetailRow r = new NoPurchaseDetailRow();
            r.setWarehouseId(req.getWarehouseId());
            r.setWarehouseName(whName);
            r.setStoreId(s.getStoreId());
            r.setStoreName(s.getStoreName());
            r.setNoPurchaseStartDate(seg.start.format(DF));
            r.setNoPurchaseEndDate(seg.end.format(DF));
            r.setNoPurchaseDays(seg.days);
            rows.add(r);
        }

        // 按未进货天数降序
        rows.sort(Comparator.comparingLong(NoPurchaseDetailRow::getNoPurchaseDays).reversed());
        resp.setRows(rows);
        return resp;
    }

    /**
     * 计算区间内「最近一段」连续未进货区间（>= minDays）
     */
    private NoPurchaseSegment findRecentNoPurchaseSegment(
            List<LocalDate> orderDays,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            long minDays
    ) {
        List<LocalDate> days = (orderDays == null)
                ? List.of()
                : orderDays.stream().distinct().sorted().toList();

        final NoPurchaseSegment[] best = {null};

        // 尝试更新（以 end 更靠后者为“最近一段”）
        java.util.function.BiConsumer<LocalDate, LocalDate> tryUpdate = (s, e) -> {
            if (s.isAfter(e)) return;
            long d = ChronoUnit.DAYS.between(s, e) + 1;
            if (d < minDays) return;

            if (best[0] == null || e.isAfter(best[0].end)) {
                NoPurchaseSegment seg = new NoPurchaseSegment();
                seg.start = s;
                seg.end = e;
                seg.days = d;
                best[0] = seg;
            }
        };

        if (days.isEmpty()) {
            tryUpdate.accept(rangeStart, rangeEnd);
            return best[0];
        }

        // start → first-1
        tryUpdate.accept(rangeStart, days.get(0).minusDays(1));

        // order 之间
        for (int i = 0; i < days.size() - 1; i++) {
            tryUpdate.accept(days.get(i).plusDays(1), days.get(i + 1).minusDays(1));
        }

        // last+1 → end
        tryUpdate.accept(days.get(days.size() - 1).plusDays(1), rangeEnd);

        return best[0];
    }

    // -------------------------内部方法-------------------------

    private List<StoreInfoDTO> queryStores(List<Long> storeIds) {
        return storeApi.listStoresByIds(storeIds);
    }

    private List<PurchaseWarehouseStatsExcelVO> buildWarehouseExportRows(PurchaseStatsReq req) {
        return warehouseStats(req).getRows().stream().map(row -> {
            PurchaseWarehouseStatsExcelVO vo = new PurchaseWarehouseStatsExcelVO();
//            vo.setWarehouseId(row.getWarehouseId());
            vo.setWarehouseName(row.getWarehouseName());
            vo.setStoreCount(row.getStoreCount());
            vo.setNewStoreCount(row.getNewStoreCount());
            vo.setClosedStoreCount(row.getClosedStoreCount());
            vo.setOrderTotalAmount(row.getOrderTotalAmount());
            vo.setStoreDailyAvgAmount(row.getStoreDailyAvgAmount());
            vo.setMom(row.getMom());
            vo.setYoy(row.getYoy());
//            vo.setNoPurchase10dStoreCount(row.getNoPurchase10dStoreCount());
            return vo;
        }).toList();
    }

    private List<PurchaseStoreStatsExcelVO> buildStoreExportRows(PurchaseStoreStatsReq req) {
        PurchaseStoreStatsReq pageReq = copyStoreStatsReq(req);
        pageReq.setPageNo(1);
        pageReq.setPageSize(Math.max(pageReq.getStoreIds() == null ? 0 : pageReq.getStoreIds().size(), 1));
        return storeStats(pageReq).getRows().stream().map(row -> {
            PurchaseStoreStatsExcelVO vo = new PurchaseStoreStatsExcelVO();
//            vo.setStoreId(row.getStoreId());
            vo.setStoreName(row.getStoreName());
//            vo.setWarehouseId(row.getWarehouseId());
            vo.setWarehouseName(row.getWarehouseName());
            vo.setOrderTotalAmount(row.getOrderTotalAmount());
            vo.setDailyAvgAmount(row.getDailyAvgAmount());
            vo.setMom(row.getMom());
            vo.setYoy(row.getYoy());
            return vo;
        }).toList();
    }

    private PurchaseStoreStatsReq copyStoreStatsReq(PurchaseStoreStatsReq req) {
        PurchaseStoreStatsReq target = new PurchaseStoreStatsReq();
        target.setMode(req.getMode());
        target.setStartDate(req.getStartDate());
        target.setEndDate(req.getEndDate());
        target.setStoreIds(req.getStoreIds() == null ? null : new ArrayList<>(req.getStoreIds()));
        target.setPageNo(req.getPageNo());
        target.setPageSize(req.getPageSize());
        return target;
    }

    private Map<Long, Long> countNewStoresByWarehouse(Map<Long, List<StoreInfoDTO>> whToStores, DateRangeUtil.Range cur) {
        LocalDateTime start = cur.getStart().atStartOfDay();
        LocalDateTime endEx = cur.getEnd().plusDays(1).atStartOfDay();

        Map<Long, Long> res = new HashMap<>();
        for (Map.Entry<Long, List<StoreInfoDTO>> e : whToStores.entrySet()) {
            long cnt = e.getValue().stream()
                    .filter(s -> s.getCreateTime() != null)
                    .filter(s -> !s.getCreateTime().isBefore(start) && s.getCreateTime().isBefore(endEx))
                    .count();
            res.put(e.getKey(), cnt);
        }
        return res;
    }

    private Map<Long, Long> countClosedStoresByWarehouse(List<Long> storeIds,
                                                         DateRangeUtil.Range cur,
                                                         List<StoreInfoDTO> stores) {
        // storeId -> warehouseId
        Map<Long, Long> storeToWh = stores.stream()
                .filter(s -> s.getWarehouseId() != null)
                .collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getWarehouseId, (a, b) -> a));

        LocalDateTime start = cur.getStart().atStartOfDay();
        LocalDateTime endEx = cur.getEnd().plusDays(1).atStartOfDay();

        // 跨服务获取闭店日志：store_status=1 && deleted=0 && create_time in range
        List<StoreStatusLogDTO> logs = storeApi.listClosedLogs(storeIds, start, endEx);

        Map<Long, Long> res = new HashMap<>();
        for (StoreStatusLogDTO log : logs) {
            Long whId = storeToWh.get(log.getStoreId());
            if (whId == null) continue;
            res.put(whId, res.getOrDefault(whId, 0L) + 1L);
        }
        return res;
    }


    /**
     * 按月：计算“连续>=10天未进货”的门店（用于剔除与统计）
     */
    private Set<Long> calcNoPurchase10dStoreIds(List<Long> storeIds, DateRangeUtil.Range cur) {
        long startMs = DateRangeUtil.toEpochMillisStart(cur.getStart());
        long endEx = DateRangeUtil.toEpochMillisEndExclusive(cur.getEnd());

        Map<Long, List<LocalDate>> orderDaysMap =
                es.queryOrderDaysByStore(storeIds, startMs, endEx, INVALID_ORDER_STATUS, null);

        Set<Long> excluded = new HashSet<>();
        for (Long storeId : storeIds) {
            List<LocalDate> days = orderDaysMap.get(storeId);
            NoPurchaseSegment seg =
                    findRecentNoPurchaseSegment(days, cur.getStart(), cur.getEnd(), 10);
            if (seg != null) {
                excluded.add(storeId);
            }
        }
        return excluded;
    }

}

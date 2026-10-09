package com.htyoudao.youdao.module.analysis.service.impl;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

import cn.hutool.core.util.NumberUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductPageDownloadExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.IChProductAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterOrderSimpleDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityHiddenFilterDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSpuCountDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 商品分页聚合查询 —— ClickHouse 数据源实现
 * <p>
 * 查询分析表 analytics.bz_order_product_ch_analytics (ReplacingMergeTree)，
 * ORDER BY (store_id, order_sn, order_product_id, source_type)。
 * <p>
 * 去重策略: 所有查询使用 FINAL 确保 ReplacingMergeTree 去重准确 (16GB 内存下 FINAL 性能可接受)。
 * <p>
 * 内存安全: 总数用 countDistinct 单独查询，不使用 count() OVER()
 * 窗口函数 (避免聚合全量物化 OOM)。
 * <p>
 * 列类型: is_son/is_purchase=UInt8, is_single=Int32, goods_num=Int32,
 * activity_discount_amount/promotion_discount_amount=Nullable(Decimal),
 * order_state/store_id/member_id/commodity_id=String, create_time=DateTime
 */
@Slf4j
@DS("clickhouse")
@Service
public class ChProductAggServiceImpl implements IChProductAggregationService {

    @Resource
    private JdbcTemplate clickHouseJdbc;

    @Resource
    private IEventService eventService;

    @DubboReference
    private CommodityApi commodityApi;

    @DubboReference
    private StoreApi storeApi;

    private static final DateTimeFormatter CH_DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String VALID_IN =
        "'20','30','40','50','60','70','80','200'";

    private static final String CH_TABLE = "analytics.bz_order_product_ch_analytics";

    @Override
    public PageResult<ProductResult> queryProductPageFromCh(ProductPageRequest request) {
        // 加购 (isSingle=3): 商品清单由加购单据配置表驱动（镜像 ES 老链路 getPurchaseProductPage），
        // 不依赖 CH 订单数据 —— 当天无订单行的加购商品也必须展示、指标置 0。
        // 故不走 queryTotal/queryPage 聚合链路（GROUP BY commodity_id 产不出无订单行的商品，
        // 且 is_purchase=1 无数据时 queryTotal 短路会直接返回空页）
        if (Objects.equals(request.getIsSingle(), 3)) {
            return queryPurchasePageFromCh(request);
        }
        // 隐藏商品过滤: 整个请求只调一次 RPC，queryTotal 与 queryPage 共用同一口径
        // (仅单品/套餐启用，加购 isSingle=3 跳过，与 ES 实现口径一致)
        CommodityHiddenFilterDTO hiddenFilter = fetchHiddenCommodityFilter(request);
        // isHidden=0 且可见白名单为空: 所有商品均隐藏，直接返回空页
        // (appendHiddenCommodityFilter 内亦有 AND 1 = 0 兜底，此处短路避免执行无效 SQL)
        if (hiddenFilter != null && Objects.equals(hiddenFilter.getIsHidden(), 0)
                && filterNonNullCommodityIds(hiddenFilter).isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }
        // 先查总数（轻量 countDistinct，不走 count() OVER() 全量物化）
        long total = queryTotal(request, hiddenFilter);
        if (total == 0) {
            return new PageResult<>(List.of(), 0L);
        }
        // 再查分页数据（不含 count() OVER()，避免聚合全量物化 OOM）
        PageResult<ProductResult> page = queryPage(request, total, hiddenFilter);
        if (!CollectionUtils.isEmpty(page.getList())
                && request.getMetrics().contains("repurchaseRate")) {
            fillRepurchaseCounts(page.getList(), request);
        }
        // clickUserCount / addCartUserCount: 复用 ES EventService 查询事件 UV
        if (!CollectionUtils.isEmpty(page.getList())) {
            fillEventCounts(page.getList(), request);
        }
        return page;
    }

    // ==================== purchase page (isSingle=3, 加购) ====================

    /**
     * 加购分页（镜像 ES 老链路 getPurchaseProductPage）: 商品清单来自加购单据配置表
     * （commodity_after_order，RPC 全量 + 内存过滤），指标从 CH 反查、查不到置 0，
     * 内存排序后 subList 分页，total = 过滤去重后清单 size。
     * <p>
     * 修复背景: 原实现在 is_purchase=1 无订单数据时被 queryTotal 短路返回空页，
     * 无订单行的加购商品无法展示 —— 清单改由单据表驱动后与老接口行为一致。
     */
    private PageResult<ProductResult> queryPurchasePageFromCh(ProductPageRequest request) {
        // 加购单据全量清单（RPC + 内存过滤，不加时间/门店/状态条件）
        List<AfterOrderSimpleDTO> afterOrders = listFilteredAfterOrders(request);
        if (CollectionUtils.isEmpty(afterOrders)) {
            return new PageResult<>(List.of(), 0L);
        }
        // 按 commodityId 去重: RPC 返回已按 afterId 降序，首次出现即最大 afterId
        // （与老链路 Collectors.toMap (left, right) -> left 语义一致）
        Map<Long, AfterOrderSimpleDTO> uniqueAfterOrders = new LinkedHashMap<>();
        afterOrders.forEach(item -> uniqueAfterOrders.putIfAbsent(item.getCommodityId(), item));
        // 指标反查用全部商品 ID 而非当页（排序需要全清单指标）；
        // 当前 CH 表 is_purchase 恒 0，反查预期为空 → 指标零兜底
        Map<Long, ProductResult> salesMap = queryPurchaseMetricsFromCh(request,
            new ArrayList<>(uniqueAfterOrders.keySet()));
        // 组装（含零销量商品）+ 排序
        Comparator<PurchaseProductRow> comparator = buildPurchaseComparator(request);
        List<PurchaseProductRow> rows = uniqueAfterOrders.values().stream()
            .map(item -> new PurchaseProductRow(item.getAfterId(),
                buildPurchaseProductResult(item, salesMap.get(item.getCommodityId()))))
            .sorted(comparator)
            .toList();
        // 内存分页（越界防护），total = 清单 size
        int from = Math.max((request.getPageNo() - 1) * request.getPageSize(), 0);
        int to = Math.min(from + request.getPageSize(), rows.size());
        List<ProductResult> currentPage = from >= rows.size() ? List.of()
            : rows.subList(from, to).stream().map(PurchaseProductRow::result).toList();

        PageResult<ProductResult> pageResult = new PageResult<>();
        pageResult.setTotal((long) rows.size());
        pageResult.setList(currentPage);
        return pageResult;
    }

    /**
     * 加购单据清单（镜像老链路 listFilteredAfterOrders）: 对 commodity_after_order
     * 配置表无条件全量查询（afterId 降序），内存仅按三条件过滤 —— commodityId 非空 +
     * 商品名称 contains + commodityIds IN，不加时间/门店/状态过滤。
     */
    private List<AfterOrderSimpleDTO> listFilteredAfterOrders(ProductPageRequest request) {
        List<AfterOrderSimpleDTO> checkedData = commodityApi.getAfterOrderSimpleList().getCheckedData();
        if (CollectionUtils.isEmpty(checkedData)) {
            return List.of();
        }
        return checkedData.stream()
            .filter(item -> item.getCommodityId() != null)
            .filter(item -> StringUtils.isBlank(request.getGoodsName())
                || StringUtils.contains(item.getCommodityName(), request.getGoodsName()))
            .filter(item -> CollectionUtils.isEmpty(request.getCommodityIds())
                || request.getCommodityIds().contains(item.getCommodityId()))
            .toList();
    }

    /**
     * 加购指标反查（条件口径镜像老链路 queryPurchaseSalesMap）: 沿用 queryPage 的
     * 两段 UNION ALL 骨架（今天段 FINAL + 历史段），差异仅在 GROUP BY commodity_id 单列、
     * 指标固定为销量/销售额两列、无排序分页。
     * <p>
     * WHERE = queryPage 同款（business_id/deleted/order_state 恒定集 + storeIds +
     * commodityIds + is_purchase=1 + 时间范围 + 营销筛选），不含 goodsName ——
     * 单据名与订单名可能不一致，重复过滤会丢数据。
     */
    private Map<Long, ProductResult> queryPurchaseMetricsFromCh(ProductPageRequest request,
                                                                 List<Long> commodityIds) {
        if (CollectionUtils.isEmpty(commodityIds)) {
            return Map.of();
        }
        // 拷贝请求: commodityIds 换成去重后全部商品 ID、goodsName 清空（镜像老链路 queryPurchaseSalesMap）
        ProductPageRequest metricsRequest = copyForPurchaseMetrics(request, commodityIds);

        // ---- 最内层列: WHERE 引用的动态列随条件追加（与 queryPage 拼法一致） ----
        StringBuilder innerCols = new StringBuilder("commodity_id, goods_num, money_amount");
        if (StringUtils.isNotBlank(metricsRequest.getStatType())) {
            innerCols.append(", source_type, activity_discount_amount, promotion_discount_amount");
        }
        if (!CollectionUtils.isEmpty(metricsRequest.getCouponIds())) {
            innerCols.append(", coupon_id");
        }
        if (!CollectionUtils.isEmpty(metricsRequest.getActivityIds())) {
            innerCols.append(", activity_id");
        }

        // 构建 WHERE 条件（不含时间范围；includeGoodsName=false —— 反查无名称条件）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        appendConditions(whereSql, whereParams, metricsRequest, false, false);

        // 今天0点作为分界线
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = metricsRequest.getCurrentTimeStart() != null
            ? metricsRequest.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = metricsRequest.getCurrentTimeEnd() != null
            ? metricsRequest.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(1024);
        sql.append("SELECT commodity_id,")
           .append(" sum(goods_num) AS allSalesVolume,")
           .append(" sum(COALESCE(money_amount, 0) * goods_num) AS salesAmount")
           .append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT ").append(innerCols).append(" FROM ")
           .append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT ").append(innerCols).append(" FROM ")
           .append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" ) t GROUP BY commodity_id");
        sql.append(" SETTINGS optimize_aggregation_in_order = 1");
        log.info("CH purchase metrics SQL: {}", sql);

        return executeChQuery("purchase metrics", () -> clickHouseJdbc.query(sql.toString(), rs -> {
            Map<Long, ProductResult> salesMap = new HashMap<>();
            while (rs.next()) {
                Long commodityId = null;
                try { commodityId = Long.parseLong(rs.getString("commodity_id")); }
                catch (Exception ignored) {}
                if (commodityId == null) {
                    continue;
                }
                ProductResult item = new ProductResult();
                item.setCommodityId(commodityId);
                item.setAllSalesVolume(rs.getInt("allSalesVolume"));
                item.setSalesAmount(rs.getDouble("salesAmount"));
                salesMap.put(commodityId, item);
            }
            return salesMap;
        }, params.toArray()));
    }

    /**
     * 浅拷贝请求用于加购指标反查: 字段清单对齐 copyForMatrix，
     * 差异仅 commodityIds 换成去重后全部商品、goodsName 清空（反查无名称条件）。
     */
    private ProductPageRequest copyForPurchaseMetrics(ProductPageRequest request, List<Long> commodityIds) {
        ProductPageRequest metricsRequest = new ProductPageRequest();
        metricsRequest.setCurrentTimeStart(request.getCurrentTimeStart());
        metricsRequest.setCurrentTimeEnd(request.getCurrentTimeEnd());
        metricsRequest.setSortBy(request.getSortBy());
        metricsRequest.setSortOrder(request.getSortOrder());
        metricsRequest.setStoreIds(request.getStoreIds());
        metricsRequest.setGoodsName(null);
        metricsRequest.setIsSingle(request.getIsSingle());
        metricsRequest.setRealTime(request.getRealTime());
        metricsRequest.setCommodityIds(commodityIds);
        metricsRequest.setMetrics(new HashSet<>(request.getMetrics()));
        metricsRequest.setRangeHours(request.getRangeHours());
        metricsRequest.setStatType(request.getStatType());
        metricsRequest.setChannelIds(request.getChannelIds());
        metricsRequest.setCouponIds(request.getCouponIds());
        metricsRequest.setActivityIds(request.getActivityIds());
        metricsRequest.setPageNo(1);
        metricsRequest.setPageSize(commodityIds.size());
        return metricsRequest;
    }

    /**
     * 加购结果组装（镜像老链路 buildPurchaseProductResult）: 单据基础信息 +
     * isPurchase 硬编码 1 + 零指标兜底；反查命中才覆盖销量/销售额（反查仅产出这两列，
     * 判空覆盖避免把零兜底刷成 null），categoryName 不设置（单据无分类信息）。
     */
    private ProductResult buildPurchaseProductResult(AfterOrderSimpleDTO item, ProductResult sales) {
        ProductResult result = new ProductResult();
        result.setCommodityId(item.getCommodityId());
        result.setGoodsName(item.getCommodityName());
        result.setGoodsImage(item.getThumbnailUrl());
        result.setIsPurchase(1);
        result.setSalesAmount(0D);
        result.setSalesVolume(0);
        result.setSingleSalesVolume(0);
        result.setAllSalesVolume(0);
        result.setOrderCount(0);
        result.setCustomerCount(0L);
        result.setRepurchaseUserCount(0L);
        result.setNewCustomerCount(0L);

        if (sales != null) {
            if (sales.getSalesAmount() != null) {
                result.setSalesAmount(sales.getSalesAmount());
            }
            if (sales.getAllSalesVolume() != null) {
                result.setAllSalesVolume(sales.getAllSalesVolume());
            }
        }
        return result;
    }

    /**
     * 加购排序（镜像老链路 buildPurchaseComparator）: sortBy 仅认 salesAmount/allSalesVolume，
     * 默认按 afterId；sortOrder != "asc" 时主序反转；末位 afterId 兜底。
     */
    private Comparator<PurchaseProductRow> buildPurchaseComparator(ProductPageRequest request) {
        // 拆箱前置条件: 行对象由 buildPurchaseProductResult 构造，salesAmount/allSalesVolume
        // 恒有零兑底非空，可安全拆箱
        Comparator<PurchaseProductRow> comparator = switch (StringUtils.defaultString(request.getSortBy())) {
            case "salesAmount" -> Comparator.comparingDouble(row -> row.result().getSalesAmount());
            case "allSalesVolume" -> Comparator.comparingLong(row -> row.result().getAllSalesVolume());
            default -> Comparator.comparing(PurchaseProductRow::afterId, Comparator.nullsLast(Long::compareTo));
        };
        if (!Objects.equals(request.getSortOrder(), "asc")) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(PurchaseProductRow::afterId, Comparator.nullsLast(Long::compareTo));
    }

    /**
     * 加购排序/组装的行载体（afterId 供默认排序，镜像老链路 PurchaseProductRow）
     */
    private record PurchaseProductRow(Long afterId, ProductResult result) {
    }

    // ==================== total count ====================

    /**
     * 分组计数: 今天数据加 FINAL 去重，历史数据不加 FINAL（已 merge 无重复），UNION ALL 合并后 countDistinct。
     * 天然跳过 commodity_id 为 NULL 的脏数据。
     */
    private long queryTotal(ProductPageRequest request, CommodityHiddenFilterDTO hiddenFilter) {
        // 构建 WHERE 条件（不含时间范围）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        appendConditions(whereSql, whereParams, request, true, false);
        appendHiddenCommodityFilter(whereSql, whereParams, hiddenFilter);

        // 今天0点作为分界线
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = request.getCurrentTimeStart() != null ? request.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = request.getCurrentTimeEnd() != null ? request.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(512);
        sql.append("SELECT countDistinct(commodity_id) FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT commodity_id FROM ").append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT commodity_id FROM ").append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(") SETTINGS optimize_aggregation_in_order = 1");
        log.info("CH product total SQL: {}", sql);

        Long total = executeChQuery("product total", () -> clickHouseJdbc.queryForObject(sql.toString(), Long.class, params.toArray()));
        return total != null ? total : 0L;
    }

    // ==================== page query ====================

    /**
     * 聚合分页查询，不含 count() OVER() 以避免全量物化导致 OOM。
     * <pre>
     * SELECT ... FROM (SELECT ... FROM table ... WHERE ...) t GROUP BY commodity_id
     * ORDER BY ... LIMIT ? OFFSET ?
     * </pre>
     * 总数由 queryTotal() 单独查询。
     */
    private PageResult<ProductResult> queryPage(ProductPageRequest request, long total,
                                                CommodityHiddenFilterDTO hiddenFilter) {
        Set<String> metrics = request.getMetrics();
        boolean needRepurchase = metrics.contains("repurchaseRate");

        // ---- 内层聚合列 ----
        StringBuilder aggCols = new StringBuilder(256);
        aggCols.append(" commodity_id,")
               .append(" any(goods_name) AS goods_name,")
               .append(" any(goods_image) AS goods_image,")
               .append(" any(category_name) AS category_name,")
               .append(" any(is_single) AS is_single,")
               .append(" any(is_purchase) AS is_purchase");

        if (metrics.contains("allSalesVolume")) {
            aggCols.append(", sum(goods_num) AS allSalesVolume");
        }
        if (metrics.contains("salesVolume")) {
            aggCols.append(", sumIf(goods_num, is_son = 0) AS salesVolume");
        }
        if (metrics.contains("singleSalesVolume")) {
            aggCols.append(", sumIf(goods_num, is_son = 1) AS singleSalesVolume");
        }
        if (metrics.contains("salesAmount")) {
            aggCols.append(", sum(COALESCE(money_amount, 0) * goods_num) AS salesAmount");
        }
        if (metrics.contains("orderCount")) {
            aggCols.append(", count(DISTINCT order_sn) AS orderCount");
        }
        if (metrics.contains("customerCount") || needRepurchase) {
            aggCols.append(", countDistinct(member_id) AS customerCount");
        }
        if (metrics.contains("storeCount")) {
            aggCols.append(", countDistinct(store_id) AS storeCount");
        }
        if (needRepurchase) {
            aggCols.append(", toUInt64(0) AS repurchaseUserCount");
        }

        // ---- 最内层: FINAL 流式去重 ----
        StringBuilder innerCols = new StringBuilder("commodity_id, goods_name, goods_image, category_name,"
            + " is_single, is_purchase, goods_num, money_amount, order_sn, member_id, is_son, store_id");
        if (StringUtils.isNotBlank(request.getStatType())) {
            innerCols.append(", source_type, activity_discount_amount, promotion_discount_amount");
        }
        if (!CollectionUtils.isEmpty(request.getCouponIds())) {
            innerCols.append(", coupon_id");
        }
        if (!CollectionUtils.isEmpty(request.getActivityIds())) {
            innerCols.append(", activity_id");
        }

        int offset = (request.getPageNo() - 1) * request.getPageSize();
        // 构建 WHERE 条件（不含时间范围）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        appendConditions(whereSql, whereParams, request, true, false);
        appendHiddenCommodityFilter(whereSql, whereParams, hiddenFilter);

        // 今天0点作为分界线
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = request.getCurrentTimeStart() != null ? request.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = request.getCurrentTimeEnd() != null ? request.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(1280);
        sql.append("SELECT ").append(aggCols);
        sql.append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT ").append(innerCols).append(" FROM ").append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT ").append(innerCols).append(" FROM ").append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" ) t GROUP BY commodity_id");
        appendSorting(sql, request, metrics);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(request.getPageSize());
        params.add(offset);

        sql.append(" SETTINGS optimize_aggregation_in_order = 1");
        log.info("CH product page SQL: {}", sql);

        return executeChQuery("product page", () -> clickHouseJdbc.query(sql.toString(), rs -> {
            List<ProductResult> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapProductResult(rs, metrics, needRepurchase));
            }
            return new PageResult<>(list, total);
        }, params.toArray()));
    }

    /**
     * 通用结果行映射: CH ResultSet → ProductResult
     */
    private ProductResult mapProductResult(java.sql.ResultSet rs, Set<String> metrics,
                                           boolean needRepurchase) throws java.sql.SQLException {
        ProductResult item = new ProductResult();
        try { item.setCommodityId(Long.parseLong(rs.getString("commodity_id"))); }
        catch (Exception ignored) {}
        item.setGoodsName(rs.getString("goods_name"));
        item.setGoodsImage(rs.getString("goods_image"));
        String catName = rs.getString("category_name");
        item.setCategoryName(catName != null ? catName : "");
        try { item.setIsSingle(rs.getInt("is_single")); }
        catch (Exception ignored) {}
        try {
            int isPurch = rs.getInt("is_purchase");
            if (isPurch == 1) { item.setIsPurchase(1); }
        }
        catch (Exception ignored) {}

        if (metrics.contains("salesAmount")) {
            item.setSalesAmount(rs.getDouble("salesAmount"));
        }
        if (metrics.contains("allSalesVolume")) {
            item.setAllSalesVolume(rs.getInt("allSalesVolume"));
        }
        if (metrics.contains("salesVolume")) {
            item.setSalesVolume(rs.getInt("salesVolume"));
        }
        if (metrics.contains("singleSalesVolume")) {
            item.setSingleSalesVolume(rs.getInt("singleSalesVolume"));
        }
        if (metrics.contains("orderCount")) {
            item.setOrderCount(rs.getInt("orderCount"));
        }
        if (metrics.contains("customerCount") || needRepurchase) {
            item.setCustomerCount(rs.getLong("customerCount"));
        }
        if (metrics.contains("storeCount")) {
            item.setStoreCount(rs.getLong("storeCount"));
        }
        if (needRepurchase) {
            item.setRepurchaseUserCount(rs.getLong("repurchaseUserCount"));
            if (item.getCustomerCount() != null && item.getCustomerCount() > 0
                && item.getRepurchaseUserCount() != null) {
                item.setRepurchaseRate(NumberUtil.div(item.getRepurchaseUserCount(), item.getCustomerCount()));
            }
        }
        item.setNewCustomerRate(BigDecimal.ZERO);
        return item;
    }

    // ==================== repurchase counts (separate query, with FINAL) ====================

    private void fillRepurchaseCounts(List<ProductResult> list, ProductPageRequest request) {
        if (CollectionUtils.isEmpty(list)) return;

        // 收集当页 commodity_id
        List<String> commodityIds = list.stream()
            .map(ProductResult::getCommodityId)
            .filter(id -> id != null)
            .map(String::valueOf)
            .collect(Collectors.toList());
        if (commodityIds.isEmpty()) return;

        // 构建 WHERE 条件（不含时间范围）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        // commodity_id IN
        String cidPlaceholders = commodityIds.stream().map(id -> "?").collect(Collectors.joining(","));
        whereSql.append(" AND commodity_id IN (").append(cidPlaceholders).append(")");
        commodityIds.forEach(id -> whereParams.add(String.valueOf(id)));
        // 门店 (过滤 null, 对齐 ES)
        if (!CollectionUtils.isEmpty(request.getStoreIds())) {
            List<String> nonNullStoreIds = request.getStoreIds().stream()
                .filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
            if (!nonNullStoreIds.isEmpty()) {
                String storePlaceholders = nonNullStoreIds.stream().map(id -> "?").collect(Collectors.joining(","));
                whereSql.append(" AND store_id IN (").append(storePlaceholders).append(")");
                nonNullStoreIds.forEach(whereParams::add);
            }
        }
        // 商品类型
        if (request.getIsSingle() != null) {
            if (request.getIsSingle() == 3) {
                whereSql.append(" AND is_purchase = 1");
            } else {
                whereSql.append(" AND is_single = ").append(request.getIsSingle());
            }
        }

        // 去重策略: 历史数据(已OPTIMIZE)不加FINAL + 当天数据加FINAL + UNION ALL
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = request.getCurrentTimeStart() != null ? request.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = request.getCurrentTimeEnd() != null ? request.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(1024);
        sql.append("SELECT commodity_id, countDistinct(member_id) AS repurchaseUserCount")
           .append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT commodity_id, member_id, count() AS doc_cnt FROM ")
           .append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }
        sql.append(" GROUP BY commodity_id, member_id HAVING doc_cnt >= 2");

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT commodity_id, member_id, count() AS doc_cnt FROM ")
           .append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }
        sql.append(" GROUP BY commodity_id, member_id HAVING doc_cnt >= 2");

        sql.append(" )")
           .append(" GROUP BY commodity_id")
           .append(" SETTINGS optimize_aggregation_in_order = 1");

        log.info("CH product repurchase SQL: {}, params count: {}", sql, params.size());

        // 查询并构建 Map<commodityId, repurchaseUserCount>
        Map<String, Long> repurchaseMap = executeChQuery("product repurchase", () -> clickHouseJdbc.query(sql.toString(), rs -> {
            var map = new java.util.HashMap<String, Long>();
            while (rs.next()) {
                String cid = rs.getString("commodity_id");
                long count = rs.getLong("repurchaseUserCount");
                map.put(cid, count);
            }
            return map;
        }, params.toArray()));

        // 回填到分页结果
        for (ProductResult item : list) {
            if (item.getCommodityId() != null) {
                long count = repurchaseMap.getOrDefault(String.valueOf(item.getCommodityId()), 0L);
                item.setRepurchaseUserCount(count);
                // 重新计算复购率
                if (item.getCustomerCount() != null && item.getCustomerCount() > 0) {
                    item.setRepurchaseRate(NumberUtil.div(Long.valueOf(count), item.getCustomerCount()));
                }
            }
        }
    }

    // ==================== event UV counts (click / addCart, via ES EventService) ====================

    private void fillEventCounts(List<ProductResult> list, ProductPageRequest request) {
        Set<String> metrics = request.getMetrics();
        try {
            if (metrics.contains("clickUserCount")) {
                setEventCount(request, list, EventType.CLICK_PRODUCT, ProductResult::setClickUserCount);
            }
            if (metrics.contains("addCartUserCount")) {
                setEventCount(request, list, EventType.ADD_CART, ProductResult::setAddCartUserCount);
            }
        } catch (Exception e) {
            log.error("fillEventCounts error", e);
        }
    }

    /**
     * 通用事件 UV 查询: 复用 ES EventService.batchQueryUV
     * 与 ES 实现 ProductAggerationServiceImplNew.setEventCount 逻辑一致
     */
    private void setEventCount(ProductPageRequest request, List<ProductResult> results,
                              EventType eventType, BiConsumer<ProductResult, Long> setter) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        List<String> commodityIds = results.stream()
            .map(ProductResult::getCommodityId)
            .filter(id -> id != null)
            .map(String::valueOf)
            .collect(Collectors.toList());
        if (commodityIds.isEmpty()) return;

        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setEventType(eventType);
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setEventIds(commodityIds);

        Map<String, Long> uvMap = eventService.batchQueryUV(queryDTO);

        for (ProductResult item : results) {
            if (item.getCommodityId() != null) {
                String cid = String.valueOf(item.getCommodityId());
                setter.accept(item, uvMap.getOrDefault(cid, 0L));
            }
        }
    }

    // ==================== CH query wrapper (DataAccessException → CH_QUERY_ERROR) ====================

    /**
     * 统一包装 CH SQL 执行: 仅捕获 JdbcTemplate 抛出的 DataAccessException，
     * log.error（含异常栈）后转为 CH_QUERY_ERROR 业务异常 —— 对齐 ES 链路
     * ES_QUERY_ERROR 既有模式；其余 RuntimeException（如 Dubbo RPC 业务异常、
     * 参数校验异常）原样上抛，不会被误转为 CH_QUERY_ERROR。
     */
    private <T> T executeChQuery(String sqlDesc, Supplier<T> queryAction) {
        try {
            return queryAction.get();
        } catch (DataAccessException e) {
            log.error("CH query [{}] failed", sqlDesc, e);
            throw exception(ErrorCodeConstants.CH_QUERY_ERROR);
        }
    }

    // ==================== shared filter conditions ====================

    private void appendConditions(StringBuilder sql, List<Object> params, ProductPageRequest request) {
        appendConditions(sql, params, request, true, true);
    }

    private void appendConditions(StringBuilder sql, List<Object> params,
                                  ProductPageRequest request, boolean includeGoodsName) {
        appendConditions(sql, params, request, includeGoodsName, true);
    }

    private void appendConditions(StringBuilder sql, List<Object> params,
                                  ProductPageRequest request, boolean includeGoodsName, boolean includeTime) {
        // 时间范围 (create_time 是 DateTime 类型，用格式化字符串比较)
        if (includeTime && request.getCurrentTimeStart() != null && request.getCurrentTimeEnd() != null) {
            sql.append(" AND create_time >= ? AND create_time <= ?");
            params.add(request.getCurrentTimeStart().format(CH_DT_FMT));
            params.add(request.getCurrentTimeEnd().format(CH_DT_FMT));
        }

        // 门店 (store_id 是 String 类型)
        if (!CollectionUtils.isEmpty(request.getStoreIds())) {
            appendInClause(sql, params, "store_id", request.getStoreIds());
        }

        // 商品名称 (CH position 子串匹配，等效 ES match_phrase)
        // 复购查询跳过此筛选: CH JDBC 驱动在子查询内不识别 position() 的 ? 参数
        if (includeGoodsName && StringUtils.isNotBlank(request.getGoodsName())) {
            sql.append(" AND position(goods_name, ?) > 0");
            params.add(request.getGoodsName());
        }

        // 商品类型: 1=单品(is_single=1), 2=套餐(is_single=2), 3=加购(is_purchase=1)
        // is_single Int32, is_purchase UInt8 — 用内联字面量，不走 JDBC 参数
        if (request.getIsSingle() != null) {
            if (request.getIsSingle() == 3) {
                sql.append(" AND is_purchase = 1");
            } else {
                sql.append(" AND is_single = ").append(request.getIsSingle());
            }
        }

        // 指定商品ID (commodity_id 是 Nullable(String))
        if (!CollectionUtils.isEmpty(request.getCommodityIds())) {
            appendInClause(sql, params, "commodity_id", request.getCommodityIds());
        }

        // 自定义时间段 (用 create_time DateTime 的 toHour 替代 ES 的 star 字段)
        if (!CollectionUtils.isEmpty(request.getRangeHours())) {
            Set<Integer> hours = request.getRangeHours().get(0).calcHours();
            if (!hours.isEmpty()) {
                String hourValues = hours.stream().map(h -> "'" + h + "'").collect(Collectors.joining(","));
                sql.append(" AND toString(toHour(create_time)) IN (").append(hourValues).append(")");
            }
        }

        // 营销分析筛选 (与 ES 一致: statType 非空时只统计主商品，排除套餐子品和加购)
        if (StringUtils.isNotBlank(request.getStatType())) {
            sql.append(" AND source_type = 'product'");
            switch (request.getStatType()) {
                case "coupon" -> sql.append(" AND activity_discount_amount > 0");
                case "activity" -> sql.append(" AND promotion_discount_amount > 0");
                case "all" -> sql.append(" AND (activity_discount_amount > 0 OR promotion_discount_amount > 0)");
            }
        }

        // 优惠券筛选 (coupon_id 是 String)
        if (!CollectionUtils.isEmpty(request.getCouponIds())) {
            appendInClause(sql, params, "coupon_id", request.getCouponIds());
        }

        // 营销活动筛选 (activity_id 是 String)
        if (!CollectionUtils.isEmpty(request.getActivityIds())) {
            appendInClause(sql, params, "activity_id", request.getActivityIds());
        }

        // 注意: channelIds 筛选暂不支持 (CH视图无 channel 列)
    }

    // ==================== hidden commodity filter ====================

    /**
     * 调用商品服务获取隐藏商品过滤条件。
     * <p>
     * 与 ES 实现 ProductAggerationServiceImplNew.applyHiddenCommodityFilter 口径一致:
     * 仅 isSingle=1(单品)/2(套餐) 时启用过滤，加购(isSingle=3)跳过。
     * RPC 返回 null、DTO 为 null 时跳过过滤；RPC 调用异常时降级为不过滤并记日志，不阻断主查询。
     */
    private CommodityHiddenFilterDTO fetchHiddenCommodityFilter(ProductPageRequest request) {
        if (!Objects.equals(request.getIsSingle(), 1) && !Objects.equals(request.getIsSingle(), 2)) {
            return null;
        }
        try {
            CommonResult<CommodityHiddenFilterDTO> result = commodityApi.filterHiddenCommodityIds();
            if (result == null) {
                return null;
            }
            return result.getCheckedData();
        } catch (Exception e) {
            log.error("filterHiddenCommodityIds failed, fallback to no-filter, isSingle={}",
                request.getIsSingle(), e);
            return null;
        }
    }

    /**
     * 向 WHERE 追加隐藏商品过滤条件 (commodity_id 是 Nullable(String)，参数以 String.valueOf 加入)。
     * <p>
     * DTO 语义 (返回较小集合的传输优化):
     * isHidden=1 时 commodityIds 是隐藏商品清单 → NOT IN 排除 (空清单不加条件);
     * isHidden=0 时 commodityIds 是可见商品白名单 → IN 只保留这些
     * (白名单为空 = 全部隐藏，拼 AND 1 = 0 恒假兜底，等价 ES 的 matchNone)。
     * isHidden 为未知取值时不拼条件不加参数，保证 ? 占位符与参数严格同数量。
     */
    private void appendHiddenCommodityFilter(StringBuilder sql, List<Object> params,
                                             CommodityHiddenFilterDTO hiddenFilter) {
        if (hiddenFilter == null || hiddenFilter.getIsHidden() == null) {
            return;
        }
        boolean exclude = Objects.equals(hiddenFilter.getIsHidden(), 1);
        boolean include = Objects.equals(hiddenFilter.getIsHidden(), 0);
        // 未知取值: 既不拼 SQL 也不加参数
        if (!exclude && !include) {
            return;
        }
        List<Long> nonNullIds = filterNonNullCommodityIds(hiddenFilter);
        if (nonNullIds.isEmpty()) {
            // include 空白名单 = 全部商品隐藏，恒假条件兜底; exclude 空清单 = 无隐藏商品，不加条件
            if (include) {
                sql.append(" AND 1 = 0");
            }
            return;
        }
        String placeholders = nonNullIds.stream().map(id -> "?").collect(Collectors.joining(","));
        if (exclude) {
            sql.append(" AND commodity_id NOT IN (").append(placeholders).append(")");
        } else {
            sql.append(" AND commodity_id IN (").append(placeholders).append(")");
        }
        nonNullIds.forEach(id -> params.add(String.valueOf(id)));
    }

    /**
     * 过滤 null 值商品 ID，对齐 ES toFieldValues() 的 null 过滤逻辑。
     */
    private List<Long> filterNonNullCommodityIds(CommodityHiddenFilterDTO hiddenFilter) {
        if (CollectionUtils.isEmpty(hiddenFilter.getCommodityIds())) {
            return List.of();
        }
        return hiddenFilter.getCommodityIds().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ==================== helpers ====================

    private void appendInClause(StringBuilder sql, List<Object> params, String column, Collection<?> values) {
        // 过滤 null 值, 对齐 ES toFieldValues() 的 null 过滤逻辑
        List<?> nonNull = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (nonNull.isEmpty()) return;
        String placeholders = nonNull.stream().map(v -> "?").collect(Collectors.joining(","));
        sql.append(" AND ").append(column).append(" IN (").append(placeholders).append(")");
        nonNull.forEach(v -> params.add(String.valueOf(v)));
    }

    private void appendSorting(StringBuilder sql, ProductPageRequest request, Set<String> metrics) {
        String sortBy = request.getSortBy();
        if (StringUtils.isBlank(sortBy)) {
            // 默认: 按订单数降序 (若查了orderCount), 否则按 commodity_id
            if (metrics.contains("orderCount")) {
                sql.append(" ORDER BY orderCount DESC, commodity_id ASC");
            } else {
                sql.append(" ORDER BY commodity_id ASC");
            }
            return;
        }
        // 将 ES 指标名映射到 CH SQL 列别名，只排序已查询的指标
        String column = switch (sortBy) {
            case "salesAmount" -> metrics.contains("salesAmount") ? "salesAmount" : "commodity_id";
            case "allSalesVolume" -> metrics.contains("allSalesVolume") ? "allSalesVolume" : "commodity_id";
            case "salesVolume" -> metrics.contains("salesVolume") ? "salesVolume" : "commodity_id";
            case "singleSalesVolume" -> metrics.contains("singleSalesVolume") ? "singleSalesVolume" : "commodity_id";
            case "orderCount" -> metrics.contains("orderCount") ? "orderCount" : "commodity_id";
            case "customerCount" -> metrics.contains("customerCount") ? "customerCount" : "commodity_id";
            case "repurchaseUserCount" -> "commodity_id";
            default -> "commodity_id";
        };
        boolean asc = "asc".equalsIgnoreCase(request.getSortOrder());
        sql.append(" ORDER BY ").append(column).append(asc ? " ASC" : " DESC");
    }

    /**
     * 构建 FROM 子句，始终使用 FINAL 确保 ReplacingMergeTree 去重准确。
     * 16GB 内存下 FINAL 性能可接受 (30M 行级 2-5s)。
     *
     * @param sql          SQL 构建器
     * @param params       参数列表（时间参数会被追加）
     * @param table        表名
     * @param whereSql     WHERE 条件（不含时间范围）
     * @param whereParams  WHERE 条件参数
     * @param timeStart    时间范围起点 (yyyy-MM-dd HH:mm:ss 格式), null 表示不限
     * @param timeEnd      时间范围终点 (yyyy-MM-dd HH:mm:ss 格式), null 表示不限
     */
    private void appendFinalAwareFrom(StringBuilder sql, List<Object> params,
                                      String table, String whereSql, List<Object> whereParams,
                                      String timeStart, String timeEnd) {
        sql.append(" FROM ").append(table).append(" FINAL");

        if (whereSql != null && !whereSql.isEmpty()) {
            sql.append(" WHERE ").append(whereSql);
            params.addAll(whereParams);
        }

        if (timeStart != null && timeEnd != null) {
            sql.append(" AND create_time >= ? AND create_time <= ?");
            params.add(timeStart);
            params.add(timeEnd);
        }
    }

    // ==================== product page download (CH data source) ====================

    /**
     * 商品明细下载（CH 数据源）: 导出范围/表头/行 VO 与旧 ES 实现 productPageDownload 一致。
     * <p>
     * 差异: 主查询走 CH 门店×商品矩阵 SQL（一次聚合出全部分桶，替代逐门店循环查询）、
     * 事件 UV 批量化、isSingle=3 无售后单过滤、复购窄口径。
     * 行序 = 门店外层 × 当页商品内层，与旧 ES 下载接口一致。
     */
    @Override
    public List<ProductPageDownloadExcelVO> productPageDownloadFromCh(ProductPageRequest request) {
        Set<String> metrics = request.getMetrics();
        // 1. 隐藏商品过滤 (isSingle=3 自动返回 null 跳过过滤)
        CommodityHiddenFilterDTO hiddenFilter = fetchHiddenCommodityFilter(request);
        // 2. isHidden=0 且可见白名单为空: 所有商品均隐藏，直接返回空列表
        if (hiddenFilter != null && Objects.equals(hiddenFilter.getIsHidden(), 0)
                && filterNonNullCommodityIds(hiddenFilter).isEmpty()) {
            return List.of();
        }
        // 3. 当页商品清单 (复用 queryPage 的过滤/排序/分页语义，total 不参与导出)
        List<ProductResult> pageProducts = queryPage(request, -1L, hiddenFilter).getList().stream()
            .filter(item -> item.getCommodityId() != null)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pageProducts)) {
            return List.of();
        }
        List<Long> commodityIds = pageProducts.stream()
            .map(ProductResult::getCommodityId).collect(Collectors.toList());
        List<Long> storeIds = request.getStoreIds();
        // 门店列表为空: 无导出行，与 buildStoreNameMap/buildSaleStoreCountMap 的防御等级一致
        if (CollectionUtils.isEmpty(storeIds)) {
            return List.of();
        }
        // 4. 门店名称
        Map<Long, String> storeNameMap = buildStoreNameMap(storeIds);
        // 5. 在售门店数 (仅 metrics 含 storeCount 时发起 RPC)
        Map<Long, Long> saleStoreCountMap = buildSaleStoreCountMap(commodityIds, storeIds, metrics);
        // 6. 门店 × 商品指标矩阵 (一次 SQL 聚合，替代旧接口的逐门店 ES 查询)
        Map<Long, Map<Long, ProductResult>> matrix = queryStoreCommodityMatrix(request, commodityIds, hiddenFilter);
        // 7. 复购矩阵 (窄口径，仅 metrics 含 repurchaseRate 时查询)
        Map<Long, Map<Long, Long>> repurchaseMap = metrics.contains("repurchaseRate")
            ? queryStoreRepurchaseCounts(commodityIds, storeIds, request) : Map.of();
        // 8. 事件 UV 批量查询 (每事件类型一次 RPC，异常静默返回空 Map)
        List<String> eventIdList = commodityIds.stream().map(String::valueOf).collect(Collectors.toList());
        Map<Long, Map<String, Long>> clickUvMap = metrics.contains("clickUserCount")
            ? queryEventUvBatch(storeIds, eventIdList, EventType.CLICK_PRODUCT,
                request.getCurrentTimeStart(), request.getCurrentTimeEnd()) : Map.of();
        Map<Long, Map<String, Long>> addCartUvMap = metrics.contains("addCartUserCount")
            ? queryEventUvBatch(storeIds, eventIdList, EventType.ADD_CART,
                request.getCurrentTimeStart(), request.getCurrentTimeEnd()) : Map.of();
        // 9. 笛卡尔组装: 门店外层 × 当页商品内层
        String timeLabel = buildExportTimeLabel(request);
        List<ProductPageDownloadExcelVO> result = new ArrayList<>(storeIds.size() * pageProducts.size());
        for (Long storeId : storeIds) {
            String storeName = storeNameMap.getOrDefault(storeId, String.valueOf(storeId));
            Map<Long, ProductResult> storeMatrix = matrix.getOrDefault(storeId, Map.of());
            for (ProductResult base : pageProducts) {
                ProductResult row = buildEmptyStoreProductCh(base);
                ProductResult hit = storeMatrix.get(base.getCommodityId());
                if (hit != null) {
                    mergeStoreProductMetricsCh(row, hit);
                }
                // 复购回填 (窄口径查询命中时覆盖占位 0)
                if (metrics.contains("repurchaseRate")) {
                    Long repurchaseCount = repurchaseMap
                        .getOrDefault(base.getCommodityId(), Map.of()).get(storeId);
                    if (repurchaseCount != null) {
                        row.setRepurchaseUserCount(repurchaseCount);
                        if (row.getCustomerCount() != null && row.getCustomerCount() > 0) {
                            row.setRepurchaseRate(NumberUtil.div(repurchaseCount, row.getCustomerCount()));
                        }
                    }
                }
                // 事件 UV 回填 (eventId 即 commodityId 字符串)
                if (metrics.contains("clickUserCount")) {
                    row.setClickUserCount(clickUvMap.getOrDefault(storeId, Map.of())
                        .getOrDefault(String.valueOf(base.getCommodityId()), 0L));
                }
                if (metrics.contains("addCartUserCount")) {
                    row.setAddCartUserCount(addCartUvMap.getOrDefault(storeId, Map.of())
                        .getOrDefault(String.valueOf(base.getCommodityId()), 0L));
                }
                // 在售门店数回填 (同一商品全行同值)
                if (metrics.contains("storeCount")) {
                    row.setStoreCount(saleStoreCountMap.getOrDefault(base.getCommodityId(), 0L));
                }
                result.add(buildDownloadVO(row, storeName, timeLabel));
            }
        }
        return result;
    }

    /**
     * 门店 × 商品指标矩阵: 镜像 queryPage 的两段 UNION ALL 骨架，
     * 差异仅在外层 GROUP BY store_id, commodity_id 且无排序/分页。
     * commodity_id IN / store_id IN 由 copyForMatrix 设置字段后经 appendConditions 自动拼接。
     */
    private Map<Long, Map<Long, ProductResult>> queryStoreCommodityMatrix(ProductPageRequest request,
                                                                          List<Long> commodityIds,
                                                                          CommodityHiddenFilterDTO hiddenFilter) {
        ProductPageRequest matrixRequest = copyForMatrix(request, commodityIds);
        Set<String> metrics = matrixRequest.getMetrics();
        boolean needRepurchase = metrics.contains("repurchaseRate");

        // ---- 外层聚合列 (GROUP BY store_id, commodity_id) ----
        StringBuilder aggCols = new StringBuilder(256);
        aggCols.append(" commodity_id, store_id,")
               .append(" any(goods_name) AS goods_name,")
               .append(" any(goods_image) AS goods_image,")
               .append(" any(category_name) AS category_name,")
               .append(" any(is_single) AS is_single,")
               .append(" any(is_purchase) AS is_purchase");
        if (metrics.contains("allSalesVolume")) {
            aggCols.append(", sum(goods_num) AS allSalesVolume");
        }
        if (metrics.contains("salesVolume")) {
            aggCols.append(", sumIf(goods_num, is_son = 0) AS salesVolume");
        }
        if (metrics.contains("singleSalesVolume")) {
            aggCols.append(", sumIf(goods_num, is_son = 1) AS singleSalesVolume");
        }
        if (metrics.contains("salesAmount")) {
            aggCols.append(", sum(COALESCE(money_amount, 0) * goods_num) AS salesAmount");
        }
        if (metrics.contains("orderCount")) {
            aggCols.append(", count(DISTINCT order_sn) AS orderCount");
        }
        if (metrics.contains("customerCount") || needRepurchase) {
            aggCols.append(", countDistinct(member_id) AS customerCount");
        }
        if (needRepurchase) {
            aggCols.append(", toUInt64(0) AS repurchaseUserCount");
        }
        // storeCount 列不拼: 组装阶段由 saleStoreCountByCommodityIds RPC 结果覆盖

        // ---- 最内层列: 与 queryPage 一致 (store_id 已在列清单中) ----
        StringBuilder innerCols = new StringBuilder("commodity_id, goods_name, goods_image, category_name,"
            + " is_single, is_purchase, goods_num, money_amount, order_sn, member_id, is_son, store_id");
        if (StringUtils.isNotBlank(matrixRequest.getStatType())) {
            innerCols.append(", source_type, activity_discount_amount, promotion_discount_amount");
        }
        if (!CollectionUtils.isEmpty(matrixRequest.getCouponIds())) {
            innerCols.append(", coupon_id");
        }
        if (!CollectionUtils.isEmpty(matrixRequest.getActivityIds())) {
            innerCols.append(", activity_id");
        }

        // 构建 WHERE 条件（不含时间范围，与 queryPage 一致）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        appendConditions(whereSql, whereParams, matrixRequest, true, false);
        appendHiddenCommodityFilter(whereSql, whereParams, hiddenFilter);

        // 今天0点作为分界线
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = matrixRequest.getCurrentTimeStart() != null
            ? matrixRequest.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = matrixRequest.getCurrentTimeEnd() != null
            ? matrixRequest.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(1280);
        sql.append("SELECT ").append(aggCols);
        sql.append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT ").append(innerCols).append(" FROM ").append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT ").append(innerCols).append(" FROM ").append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }

        sql.append(" ) t GROUP BY store_id, commodity_id");
        sql.append(" SETTINGS optimize_aggregation_in_order = 1");
        log.info("CH product download matrix SQL: {}", sql);

        return executeChQuery("product download matrix", () -> clickHouseJdbc.query(sql.toString(), rs -> {
            Map<Long, Map<Long, ProductResult>> matrix = new HashMap<>();
            while (rs.next()) {
                Long storeId = null;
                try { storeId = Long.parseLong(rs.getString("store_id")); }
                catch (Exception ignored) {}
                ProductResult item = mapMatrixRow(rs, metrics, needRepurchase);
                if (storeId == null || item.getCommodityId() == null) {
                    continue;
                }
                matrix.computeIfAbsent(storeId, k -> new HashMap<>()).put(item.getCommodityId(), item);
            }
            return matrix;
        }, params.toArray()));
    }

    /**
     * 矩阵结果行映射: CH ResultSet → ProductResult（取值语义与 mapProductResult 一致）
     */
    private ProductResult mapMatrixRow(java.sql.ResultSet rs, Set<String> metrics,
                                        boolean needRepurchase) throws java.sql.SQLException {
        ProductResult item = new ProductResult();
        try { item.setCommodityId(Long.parseLong(rs.getString("commodity_id"))); }
        catch (Exception ignored) {}
        item.setGoodsName(rs.getString("goods_name"));
        item.setGoodsImage(rs.getString("goods_image"));
        String catName = rs.getString("category_name");
        item.setCategoryName(catName != null ? catName : "");
        try { item.setIsSingle(rs.getInt("is_single")); }
        catch (Exception ignored) {}
        try {
            int isPurch = rs.getInt("is_purchase");
            if (isPurch == 1) { item.setIsPurchase(1); }
        }
        catch (Exception ignored) {}
        if (metrics.contains("salesAmount")) {
            item.setSalesAmount(rs.getDouble("salesAmount"));
        }
        if (metrics.contains("allSalesVolume")) {
            item.setAllSalesVolume(rs.getInt("allSalesVolume"));
        }
        if (metrics.contains("salesVolume")) {
            item.setSalesVolume(rs.getInt("salesVolume"));
        }
        if (metrics.contains("singleSalesVolume")) {
            item.setSingleSalesVolume(rs.getInt("singleSalesVolume"));
        }
        if (metrics.contains("orderCount")) {
            item.setOrderCount(rs.getInt("orderCount"));
        }
        if (metrics.contains("customerCount") || needRepurchase) {
            item.setCustomerCount(rs.getLong("customerCount"));
        }
        if (needRepurchase) {
            item.setRepurchaseUserCount(rs.getLong("repurchaseUserCount"));
        }
        return item;
    }

    /**
     * 浅拷贝请求用于矩阵查询: 字段清单对齐旧 ES 下载实现的 copyForSingleStore，
     * 差异仅 commodityIds 换成当页商品、storeIds 保留全量门店，
     * 让 appendConditions 自动拼 commodity_id IN 与 store_id IN 条件。
     */
    private ProductPageRequest copyForMatrix(ProductPageRequest request, List<Long> commodityIds) {
        ProductPageRequest matrixRequest = new ProductPageRequest();
        matrixRequest.setCurrentTimeStart(request.getCurrentTimeStart());
        matrixRequest.setCurrentTimeEnd(request.getCurrentTimeEnd());
        matrixRequest.setSortBy(request.getSortBy());
        matrixRequest.setSortOrder(request.getSortOrder());
        matrixRequest.setStoreIds(request.getStoreIds());
        matrixRequest.setGoodsName(request.getGoodsName());
        matrixRequest.setIsSingle(request.getIsSingle());
        matrixRequest.setRealTime(request.getRealTime());
        matrixRequest.setCommodityIds(commodityIds);
        matrixRequest.setMetrics(new HashSet<>(request.getMetrics()));
        matrixRequest.setRangeHours(request.getRangeHours());
        matrixRequest.setStatType(request.getStatType());
        matrixRequest.setChannelIds(request.getChannelIds());
        matrixRequest.setCouponIds(request.getCouponIds());
        matrixRequest.setActivityIds(request.getActivityIds());
        matrixRequest.setPageNo(1);
        matrixRequest.setPageSize(commodityIds.size());
        return matrixRequest;
    }

    /**
     * 门店 × 商品复购矩阵: 镜像 fillRepurchaseCounts 的两段结构与窄口径 WHERE，
     * 差异仅在内层 GROUP BY 加 store_id、外层按 (commodity_id, store_id) 出复购人数。
     */
    private Map<Long, Map<Long, Long>> queryStoreRepurchaseCounts(List<Long> commodityIds, List<Long> storeIds,
                                                                  ProductPageRequest request) {
        // 构建 WHERE 条件（窄口径: 当页商品 + 门店 + 商品类型，不含 goodsName/statType/券/活动/隐藏过滤）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("business_id = ? AND deleted = '0' AND order_state IN (").append(VALID_IN).append(")");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));
        // commodity_id IN
        String cidPlaceholders = commodityIds.stream().map(id -> "?").collect(Collectors.joining(","));
        whereSql.append(" AND commodity_id IN (").append(cidPlaceholders).append(")");
        commodityIds.forEach(id -> whereParams.add(String.valueOf(id)));
        // 门店 (过滤 null, 对齐 fillRepurchaseCounts)
        if (!CollectionUtils.isEmpty(storeIds)) {
            List<String> nonNullStoreIds = storeIds.stream()
                .filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
            if (!nonNullStoreIds.isEmpty()) {
                String storePlaceholders = nonNullStoreIds.stream().map(id -> "?").collect(Collectors.joining(","));
                whereSql.append(" AND store_id IN (").append(storePlaceholders).append(")");
                nonNullStoreIds.forEach(whereParams::add);
            }
        }
        // 商品类型
        if (request.getIsSingle() != null) {
            if (request.getIsSingle() == 3) {
                whereSql.append(" AND is_purchase = 1");
            } else {
                whereSql.append(" AND is_single = ").append(request.getIsSingle());
            }
        }

        // 去重策略: 历史数据(已OPTIMIZE)不加FINAL + 当天数据加FINAL + UNION ALL
        String todayStart = LocalDate.now().atStartOfDay().format(CH_DT_FMT);
        String timeStart = request.getCurrentTimeStart() != null
            ? request.getCurrentTimeStart().format(CH_DT_FMT) : null;
        String timeEnd = request.getCurrentTimeEnd() != null
            ? request.getCurrentTimeEnd().format(CH_DT_FMT) : null;

        StringBuilder sql = new StringBuilder(1024);
        sql.append("SELECT commodity_id, store_id, countDistinct(member_id) AS repurchaseUserCount")
           .append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT commodity_id, store_id, member_id, count() AS doc_cnt FROM ")
           .append(CH_TABLE).append(" FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }
        sql.append(" GROUP BY commodity_id, store_id, member_id HAVING doc_cnt >= 2");

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT commodity_id, store_id, member_id, count() AS doc_cnt FROM ")
           .append(CH_TABLE).append(" WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (timeStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(timeStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (timeEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(timeEnd);
        }
        sql.append(" GROUP BY commodity_id, store_id, member_id HAVING doc_cnt >= 2");

        sql.append(" )")
           .append(" GROUP BY commodity_id, store_id")
           .append(" SETTINGS optimize_aggregation_in_order = 1");

        log.info("CH product download repurchase matrix SQL: {}, params count: {}", sql, params.size());

        return executeChQuery("product download repurchase matrix", () -> clickHouseJdbc.query(sql.toString(), rs -> {
            Map<Long, Map<Long, Long>> map = new HashMap<>();
            while (rs.next()) {
                Long commodityId = null;
                Long storeId = null;
                try { commodityId = Long.parseLong(rs.getString("commodity_id")); }
                catch (Exception ignored) {}
                try { storeId = Long.parseLong(rs.getString("store_id")); }
                catch (Exception ignored) {}
                if (commodityId == null || storeId == null) {
                    continue;
                }
                map.computeIfAbsent(commodityId, k -> new HashMap<>())
                    .put(storeId, rs.getLong("repurchaseUserCount"));
            }
            return map;
        }, params.toArray()));
    }

    /**
     * 按门店和事件批量查询 UV（一次 RPC 覆盖全部门店×当页商品），
     * 异常静默返回空 Map，对齐旧下载接口的事件查询容错语义。
     */
    private Map<Long, Map<String, Long>> queryEventUvBatch(List<Long> storeIds, List<String> eventIds,
                                                           EventType eventType, LocalDateTime startTime,
                                                           LocalDateTime endTime) {
        try {
            EventQueryDTO queryDTO = new EventQueryDTO();
            queryDTO.setStoreIds(storeIds);
            queryDTO.setEventType(eventType);
            queryDTO.setStartTime(startTime);
            queryDTO.setEndTime(endTime);
            queryDTO.setEventIds(eventIds);
            return eventService.batchQueryUVByStoreAndEvent(queryDTO);
        } catch (Exception e) {
            log.error("batchQueryUVByStoreAndEvent failed, eventType={}", eventType, e);
            return Map.of();
        }
    }

    /**
     * 门店名称映射 (RPC 失败时由 Dubbo 调用方异常向上传播，与旧 ES 下载实现一致)
     */
    private Map<Long, String> buildStoreNameMap(List<Long> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) {
            return Map.of();
        }
        List<StoreSimpleResDto> stores = storeApi.getStoreSimpleResDtoList(storeIds).getCheckedData();
        if (CollectionUtils.isEmpty(stores)) {
            return Map.of();
        }
        return stores.stream().collect(Collectors.toMap(StoreSimpleResDto::getStoreId, StoreSimpleResDto::getStoreName,
            (left, right) -> left));
    }

    /**
     * 在售门店数映射 (仅 metrics 含 storeCount 时调用，口径与旧 ES 下载实现一致)
     */
    private Map<Long, Long> buildSaleStoreCountMap(List<Long> commodityIds, List<Long> storeIds,
                                                    Set<String> metrics) {
        if (CollectionUtils.isEmpty(commodityIds) || CollectionUtils.isEmpty(storeIds)
                || CollectionUtils.isEmpty(metrics) || !metrics.contains("storeCount")) {
            return Map.of();
        }
        List<StoreSpuCountDTO> checkedData = commodityApi.saleStoreCountByCommodityIds(
            new HashSet<>(commodityIds), new HashSet<>(storeIds)).getCheckedData();
        if (CollectionUtils.isEmpty(checkedData)) {
            return Map.of();
        }
        return checkedData.stream()
            .collect(Collectors.toMap(StoreSpuCountDTO::getCommodityId, StoreSpuCountDTO::getStoreCount,
                (left, right) -> left));
    }

    /**
     * 空指标行基准: 拷贝 6 基础字段 + 数值置零，与旧 ES 下载实现的 buildEmptyStoreProduct 一致
     */
    private ProductResult buildEmptyStoreProductCh(ProductResult base) {
        ProductResult result = new ProductResult();
        result.setCommodityId(base.getCommodityId());
        result.setGoodsName(base.getGoodsName());
        result.setCategoryName(base.getCategoryName());
        result.setGoodsImage(base.getGoodsImage());
        result.setIsSingle(base.getIsSingle());
        result.setIsPurchase(base.getIsPurchase());
        result.setSalesAmount(0D);
        result.setSalesVolume(0);
        result.setSingleSalesVolume(0);
        result.setAllSalesVolume(0);
        result.setOrderCount(0);
        result.setCustomerCount(0L);
        result.setRepurchaseUserCount(0L);
        result.setNewCustomerCount(0L);
        return result;
    }

    /**
     * 矩阵命中行指标拷贝 (不拷 clickUserCount/addCartUserCount/storeCount，由组装阶段单独回填)
     */
    private void mergeStoreProductMetricsCh(ProductResult target, ProductResult source) {
        target.setSalesAmount(source.getSalesAmount());
        target.setSalesVolume(source.getSalesVolume());
        target.setSingleSalesVolume(source.getSingleSalesVolume());
        target.setAllSalesVolume(source.getAllSalesVolume());
        target.setIsPurchase(source.getIsPurchase());
        target.setOrderCount(source.getOrderCount());
        target.setCustomerCount(source.getCustomerCount());
        target.setRepurchaseUserCount(source.getRepurchaseUserCount());
        target.setRepurchaseRate(source.getRepurchaseRate());
        target.setNewCustomerCount(source.getNewCustomerCount());
        target.setNewCustomerRate(source.getNewCustomerRate());
    }

    /**
     * 导出时间列: 实时查询取结束时间点，否则时间区间（与旧 ES 下载实现一致）
     */
    private String buildExportTimeLabel(ProductPageRequest request) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (Boolean.TRUE.equals(request.getRealTime())) {
            return request.getCurrentTimeEnd().format(dateTimeFormatter);
        }
        return request.getCurrentTimeStart().format(dateFormatter) + " - "
            + request.getCurrentTimeEnd().format(dateFormatter);
    }

    /**
     * 导出行 VO 映射: 18 字段照抄旧 ES 下载实现语义
     * (事件列与在售门店数在组装阶段已回填；比率/新客人数列 null 兕底)
     */
    private ProductPageDownloadExcelVO buildDownloadVO(ProductResult item, String storeName, String timeLabel) {
        ProductPageDownloadExcelVO vo = new ProductPageDownloadExcelVO();
        vo.setGoodsName(item.getGoodsName());
        vo.setCategoryName(item.getCategoryName());
        vo.setIsSingle(item.getIsSingle());
        vo.setStoreName(storeName);
        vo.setTime(timeLabel);
        vo.setSalesVolume(item.getSalesVolume());
        vo.setSingleSalesVolume(item.getSingleSalesVolume());
        vo.setAllSalesVolume(item.getAllSalesVolume());
        vo.setSalesAmount(item.getSalesAmount());
        vo.setOrderCount(item.getOrderCount());
        vo.setCustomerCount(item.getCustomerCount());
        vo.setRepurchaseUserCount(item.getRepurchaseUserCount());
        vo.setRepurchaseRate(item.getRepurchaseRate() == null ? 0.0 : item.getRepurchaseRate().doubleValue());
        vo.setNewCustomerCount(item.getNewCustomerCount() == null ? 0L : item.getNewCustomerCount());
        vo.setNewCustomerRate(item.getNewCustomerRate() == null ? 0.0 : item.getNewCustomerRate().doubleValue());
        vo.setClickUserCount(item.getClickUserCount());
        vo.setAddCartUserCount(item.getAddCartUserCount());
        vo.setStoreCount(item.getStoreCount());
        return vo;
    }
}

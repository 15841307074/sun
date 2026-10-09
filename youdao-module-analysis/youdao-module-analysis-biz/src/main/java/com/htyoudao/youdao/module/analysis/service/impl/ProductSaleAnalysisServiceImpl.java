package com.htyoudao.youdao.module.analysis.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleDataDTO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleQueryReq;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleStatVO;
import com.htyoudao.youdao.module.analysis.service.ProductSaleAnalysisService;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品销售分析服务实现
 */
@Slf4j
@DS("clickhouse")
@Service
public class ProductSaleAnalysisServiceImpl implements ProductSaleAnalysisService {

    /** 时间格式化器（yyyy-MM-dd HH:mm:ss） */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 分页大小最大值 */
    private static final int MAX_PAGE_SIZE = 200;

    /** SELECT 列（FROM/WHERE 由 appendFinalAwareFrom 动态构建） */
    private static final String SELECT_COLS = "SELECT " +
            "    commodity_id, " +
            "    goods_name, " +
            "    sum(goods_num) AS total_sale_num ";

    /** 分组排序SQL片段 */
    private static final String GROUP_ORDER_SQL = "GROUP BY commodity_id, goods_name " +
            "ORDER BY total_sale_num DESC ";

    /** ClickHouse查询优化设置 */
    private static final String SETTINGS_SQL = "SETTINGS " +
            "    optimize_aggregation_in_order = 1, " +
            "    max_threads = 8";

    /** 商品销售统计结果行映射器 */
    private static final RowMapper<ProductSaleStatVO> PRODUCT_SALE_STAT_ROW_MAPPER = new ProductSaleStatRowMapper();

    @Resource
    private JdbcTemplate clickHouseJdbc;

    /**
     * 查询单品销量排行
     * 去重策略: 今天数据加 FINAL，历史数据不加 FINAL（已 merge），UNION ALL 合并后外层聚合。
     *
     * @param req 查询请求参数，包含时间范围、订单状态、分页信息
     * @return 单品销量统计列表，按销量降序排列
     */
    @Override
    public List<ProductSaleStatVO> querySingleProductSaleRank(ProductSaleQueryReq req) {
        // 分页参数安全校验
        int pageNo = Math.max(req.getPageNo(), 1);
        int pageSize = Math.max(1, Math.min(req.getPageSize(), MAX_PAGE_SIZE));

        // 构建 WHERE 条件（不含时间范围）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("deleted = 'false' AND is_single = 1");

        if (CollectionUtils.isNotEmpty(req.getOrderStates())) {
            String placeholders = req.getOrderStates().stream()
                    .map(s -> "?")
                    .collect(Collectors.joining(","));
            whereSql.append(" AND order_state IN (").append(placeholders).append(")");
            req.getOrderStates().forEach(whereParams::add);
        }

        // 今天0点作为分界线
        String todayStart = java.time.LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER);
        String timeStart = req.getStartTime().format(DATE_TIME_FORMATTER);
        String timeEnd = req.getEndTime().format(DATE_TIME_FORMATTER);

        // 需要的列（用于 UNION ALL 子查询）
        String cols = "commodity_id, goods_name, goods_num";

        StringBuilder sql = new StringBuilder(512);
        sql.append(SELECT_COLS);
        sql.append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT ").append(cols).append(" FROM analytics.bz_order_product_ch FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        sql.append(" AND create_time >= ?");
        params.add(timeStart);
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        sql.append(" AND create_time <= ?");
        params.add(timeEnd);

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT ").append(cols).append(" FROM analytics.bz_order_product_ch WHERE ").append(whereSql);
        params.addAll(whereParams);
        sql.append(" AND create_time >= ?");
        params.add(timeStart);
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        sql.append(" AND create_time <= ?");
        params.add(timeEnd);

        sql.append(") ");
        sql.append(GROUP_ORDER_SQL)
                .append("LIMIT ").append(pageSize).append(" OFFSET ").append((pageNo - 1) * pageSize).append(" ")
                .append(SETTINGS_SQL);

        return clickHouseJdbc.query(sql.toString(), params.toArray(), PRODUCT_SALE_STAT_ROW_MAPPER);
    }

    /**
     * 构建 FROM 子句，始终使用 FINAL 确保 ReplacingMergeTree 去重准确。
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

    /**
     * 批量写入商品销售数据到ClickHouse
     *
     * @param dataList 商品销售数据列表
     */
    @Override
    public void batchInsertProductSaleData(List<ProductSaleDataDTO> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        String insertSql = "INSERT INTO analytics.bz_order_product_ch " +
                "(order_product_id, order_sn, order_state, store_id, store_name, member_id, " +
                "goods_id, goods_name, goods_image, spec_values, goods_show_price, goods_num, " +
                "activity_discount_amount, activity_discount_detail, platform_activity_amount, " +
                "platform_voucher_amount, money_amount, commission_rate, commission_amount, " +
                "attach_commission_amount, spell_team_id, is_gift, gift_id, return_number, " +
                "is_comment, comment_time, send_integral, is_single, flavor_name, flavor_value, " +
                "category_id, category_name, commodity_id, project_owner_ship, business_id, " +
                "creator, create_time, updater, update_time, deleted, sku_strike_price, " +
                "activity_id, activity_type, activity_name, promotion_discount_amount, " +
                "coupon_id, user_coupon_id, coupon_name, original_sku_id, store_sku_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>(dataList.size());
        for (ProductSaleDataDTO dto : dataList) {
            Object[] args = new Object[]{
                    dto.getOrderProductId(),
                    dto.getOrderSn(),
                    dto.getOrderState(),
                    dto.getStoreId(),
                    dto.getStoreName(),
                    dto.getMemberId(),
                    dto.getGoodsId(),
                    dto.getGoodsName(),
                    dto.getGoodsImage(),
                    dto.getSpecValues(),
                    dto.getGoodsShowPrice(),
                    dto.getGoodsNum(),
                    dto.getActivityDiscountAmount(),
                    dto.getActivityDiscountDetail(),
                    dto.getPlatformActivityAmount(),
                    dto.getPlatformVoucherAmount(),
                    dto.getMoneyAmount(),
                    dto.getCommissionRate(),
                    dto.getCommissionAmount(),
                    dto.getAttachCommissionAmount(),
                    dto.getSpellTeamId(),
                    dto.getIsGift(),
                    dto.getGiftId(),
                    dto.getReturnNumber(),
                    dto.getIsComment(),
                    dto.getCommentTime() != null ? dto.getCommentTime().format(DATE_TIME_FORMATTER) : null,
                    dto.getSendIntegral(),
                    dto.getIsSingle(),
                    dto.getFlavorName(),
                    dto.getFlavorValue(),
                    dto.getCategoryId(),
                    dto.getCategoryName(),
                    dto.getCommodityId(),
                    dto.getProjectOwnerShip(),
                    dto.getBusinessId(),
                    dto.getCreator(),
                    dto.getCreateTime().format(DATE_TIME_FORMATTER),
                    dto.getUpdater(),
                    dto.getUpdateTime() != null ? dto.getUpdateTime().format(DATE_TIME_FORMATTER) : null,
                    dto.getDeleted(),
                    dto.getSkuStrikePrice(),
                    dto.getActivityId(),
                    dto.getActivityType(),
                    dto.getActivityName(),
                    dto.getPromotionDiscountAmount(),
                    dto.getCouponId(),
                    dto.getUserCouponId(),
                    dto.getCouponName(),
                    dto.getOriginalSkuId(),
                    dto.getStoreSkuId()
            };
            batchArgs.add(args);
        }

        clickHouseJdbc.batchUpdate(insertSql, batchArgs);
        log.info("批量写入商品销售数据到ClickHouse完成, 数据量: {}", dataList.size());
    }

    /**
     * 商品销售统计结果RowMapper
     */
    private static class ProductSaleStatRowMapper implements RowMapper<ProductSaleStatVO> {
        @Override
        public ProductSaleStatVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductSaleStatVO vo = new ProductSaleStatVO();
            vo.setCommodityId(rs.getLong("commodity_id"));
            vo.setGoodsName(rs.getString("goods_name"));
            vo.setTotalSaleNum(rs.getLong("total_sale_num"));
            return vo;
        }
    }
}

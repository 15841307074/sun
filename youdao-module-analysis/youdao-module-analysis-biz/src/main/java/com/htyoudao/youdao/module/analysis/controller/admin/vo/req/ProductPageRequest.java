package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * 管理后台 - 商品分析分页请求参数
 * 用于查询商品维度的分析数据，支持按商品类型、门店、渠道等维度筛选
 */
@Data
public class ProductPageRequest extends PageParam {

    /**
     * 当前时间段-开始时间
     */
    @NotNull
    @Schema(description = "当前时间段-开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime currentTimeStart;

    /**
     * 当前时间段-结束时间
     */
    @NotNull
    @Schema(description = "当前时间段-结束时间", example = "2024-01-31 23:59:59")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime currentTimeEnd;

    /**
     * 排序字段
     * 指定按哪个字段进行排序，如销售金额、订单数量等
     */
    @Schema(description = "排序字段", example = "salesAmount")
    public String sortBy = "";

    /**
     * 排序方式
     * asc: 升序
     * desc: 降序
     */
    @Schema(description = "排序方式 asc 或 desc", example = "desc")
    public String sortOrder;

    /**
     * 门店ID集合
     * 用于筛选特定门店的商品数据
     */
    @Schema(description = "门店ID集合")
    public List<Long> storeIds;

    /**
     * 商品名称
     * 用于按商品名称模糊查询
     */
    @Schema(description = "商品名称，用于模糊查询")
    private String goodsName;

    /**
     * 商品类型
     * 1: 单品
     * 2: 套餐
     * 3: 加购
     */
    @Schema(description = "商品类型 1-单品 2-套餐 3-加购", example = "1")
    private Integer isSingle;

    /**
     * 是否实时查询
     * true: 增加在售门店数返回
     * false: 普通查询
     */
    @Schema(description = "是否为实时查询，true时增加在售门店数返回", example = "false")
    private Boolean realTime = false;

    /**
     * 商品原始ID列表
     * 用于精确查询特定商品的数据
     */
    @Schema(description = "商品原始ID列表")
    private List<Long> commodityIds;

    /**
     * 商品指标列表
     * 指定需要查询的指标类型
     */
    @Schema(description = "商品指标列表")
    private Set<String> metrics;

    /**
     * 时间段信息
     * 用于按小时维度进行数据统计
     */
    @Schema(description = "时间段信息，用于按小时统计")
    private List<RangeDTO> rangeHours;



    /**
     * 统计类型
     * all: 全部订单（含优惠）
     * coupon: 优惠券订单
     * activity: 营销活动订单
     */
    @Schema(description = "统计类型：all-全部, coupon-优惠券, activity-营销活动", example = "all")
    private String statType;

    /**
     * 渠道ID列表
     * 用于按渠道筛选数据，null表示全部渠道
     */
    @Schema(description = "渠道ID列表")
    private List<Long> channelIds;

    /**
     * 优惠券ID列表
     * 用于筛选特定优惠券的数据，空列表表示全部优惠券
     */
    @Schema(description = "优惠券ID列表")
    private List<Long> couponIds;

    /**
     * 营销活动ID列表
     * 用于筛选特定营销活动的数据，空列表表示全部活动
     */
    @Schema(description = "营销活动ID列表")
    private List<Long> activityIds;

    /**
     * 游标分页位置
     * 用于游标分页的定位信息，首次查询时为空，后续查询需传入上次返回的 afterKey
     */
    @Schema(description = "游标分页位置，后续分页时传入上次返回的 afterKey")
    private Map<String, Object> afterKey;

}

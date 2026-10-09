package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 管理后台 - 分组聚合分页请求参数
 * 用于分页查询分组聚合数据，如按门店、按商品等维度进行聚合统计
 */
@Data
public class AggregationPageRequest extends AggregationRequestVO {

    /**
     * 门店名称
     * 用于按门店名称筛选数据
     */
    @Schema(description = "门店名称，用于筛选特定门店")
    private String storeName;

    /**
     * 城市名称
     * 用于按城市筛选数据
     */
    @Schema(description = "城市名称，用于筛选特定城市")
    private String cityName;

    /**
     * 排序字段
     * 指定按哪个指标进行排序，如 orderAmount、orderCount 等
     */
    @Schema(description = "排序字段，如 orderAmount, orderCount", example = "orderAmount")
    private String orderField;

    /**
     * 排序方式
     * asc: 升序
     * desc: 降序
     */
    @Schema(description = "排序方式 asc 或 desc", example = "desc")
    private String orderType;

    /**
     * 页码
     * 从1开始
     */
    @NotNull
    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer pageNo;

    /**
     * 每页条数
     * 每页返回的记录数
     */
    @NotNull
    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pageSize;

    /**
     * 指标列表
     * 指定需要查询的指标类型
     */
    @Schema(description = "指标列表，指定需要查询的指标")
    private List<String> metrics;

    /**
     * 是否显示UV数据
     * true: 返回UV（独立访客数）数据
     * false: 不返回UV数据
     */
    @Schema(description = "是否显示UV数据", example = "true")
    private Boolean showUV = true;

    /**
     * 是否显示关联数据
     * 用于控制是否返回关联的其他数据
     */
    @Schema(description = "是否显示关联数据", example = "true")
    private Boolean showGYL = true;

}

package com.htyoudao.youdao.module.commodity.controller.admin.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 商品活动 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CommodityActivityRespVO {

    @Schema(description = "活动的唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "21524")
    @ExcelProperty("活动的唯一标识符")
    private Long activityId;

    @Schema(description = "商品表-商品唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "22250")
    @ExcelProperty("商品表-商品唯一标识符")
    private Long commodityId;

    @Schema(description = "商品表-商品分类ID", example = "6731")
    @ExcelProperty("商品表-商品分类ID")
    private Long categoryId;

    @Schema(description = "商品表-商品名称", example = "0090")
    @ExcelProperty("商品表-商品名称")
    private String commodityName;

    @Schema(description = "商品缩略图", example = "https://www.iocoder.cn")
    @ExcelProperty("商品缩略图")
    private String thumbnailUrl;

    @Schema(description = "是否是单品（1是 2 否）")
    @ExcelProperty("是否是单品（1是 2 否）")
    private Integer isSingle;

    @Schema(description = "商品关联 skuId")
    @ExcelProperty("商品关联 skuId")
    private String skuIds;

    @Schema(description = "活动类型（如1-抽奖,2-折扣,3-促销）", example = "1")
    @ExcelProperty("活动类型（如1-抽奖,2-折扣,3-促销）")
    private Integer activityType;

    @Schema(description = "活动开始时间")
    @ExcelProperty("活动开始时间")
    private LocalDateTime activityStartDate;

    @Schema(description = "活动结束时间")
    @ExcelProperty("活动结束时间")
    private LocalDateTime activityEndDate;

    @Schema(description = "活动描述", example = "你猜")
    @ExcelProperty("活动描述")
    private String activityDescription;

    @Schema(description = "是否启用（1表示启用，0表示停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否启用（1表示启用，0表示停用）")
    private Integer isActive;

    @Schema(description = "活动创建时间")
    @ExcelProperty("活动创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "最后修改时间")
    @ExcelProperty("最后修改时间")
    private LocalDateTime updatedTime;

    @Schema(description = "项目标识", example = "8842")
    @ExcelProperty("项目标识")
    private Long businessId;

    @Schema(description = "0灰色 1显示 ", example = "0")
    private String isShow;
}
package com.htyoudao.youdao.module.commodity.controller.admin.activity.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 商品活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommodityActivityPageReqVO extends PageParam {

    @Schema(description = "商品表-商品唯一标识符", example = "22250")
    private Long commodityId;

    @Schema(description = "商品表-商品分类ID", example = "6731")
    private Long categoryId;

    @Schema(description = "商品表-商品名称", example = "0090")
    private String commodityName;

    @Schema(description = "商品缩略图", example = "https://www.iocoder.cn")
    private String thumbnailUrl;

    @Schema(description = "是否是单品（1是 2 否）")
    private Integer isSingle;

    @Schema(description = "商品关联 skuId")
    private String skuIds;

    @Schema(description = "活动类型（如1-抽奖,2-折扣,3-促销）", example = "1")
    private Integer activityType;

    @Schema(description = "活动开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] activityStartDate;

    @Schema(description = "活动结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] activityEndDate;

    @Schema(description = "活动描述", example = "你猜")
    private String activityDescription;

    @Schema(description = "是否启用（1表示启用，0表示停用）")
    private Integer isActive;

    @Schema(description = "活动创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createdTime;

    @Schema(description = "最后修改时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updatedTime;

    @Schema(description = "项目标识", example = "8842")
    private Long businessId;

}
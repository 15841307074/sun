package com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 订单生成后加购商品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AfterOrderPageReqVO extends PageParam {

    @Schema(description = "商品ID", example = "5573")
    private Long commodityId;

    @Schema(description = "加购价格", example = "14445")
    private BigDecimal afterPrice;

    @Schema(description = "划线价格", example = "23423")
    private BigDecimal strikeThroughPrice;

    @Schema(description = "状态 1开启 0下架", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "商品名称", example = "王五")
    private String commodityName;

    @Schema(description = "商品缩略图", example = "https://www.iocoder.cn")
    private String thumbnailUrl;

}
package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店费用关系表 Request VO。
 */
@Schema(description = "门店费用关系表 Request VO")
@Data
public class SystemStoreExpensesVO extends BaseDO {

    /**
     * 费用类型：0 堂食/外带打包费，1 外卖打包费，2 外卖配送费，3 校园配送代取起送费。
     */
    @Schema(description = "费用类型：0 堂食/外带打包费，1 外卖打包费，2 外卖配送费，3 校园配送代取起送费")
    private Integer storeExpensesType;

    /**
     * 起送费/打包费限额；当 type=3 时表示代取起送费。
     */
    @Schema(description = "起送费/打包费限额；type=3 时表示代取起送费")
    private BigDecimal minimumDeliveryFee;

    /**
     * 配送费/打包费；当 type=3 时无需传，后端默认保存 0。
     */
    @Schema(description = "配送费/打包费；type=3 时无需传，后端默认保存 0")
    private BigDecimal additionaaCosts;

    /**
     * 费用计算方式：0 按商品，1 按订单；type=3 时无需传。
     */
    @Schema(description = "费用计算方式：0 按商品，1 按订单；type=3 时无需传")
    private Integer storeCalculationType;
}

package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreSimpleResVO {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("门店id")
    private Long storeId;
    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("组织id")
    private Long orgId;
    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @ExcelProperty("门店名称")
    private String storeName;
    @Schema(description = "门店是否可见 0不可见 1可见")
    private Integer visible;
    /**
     * 仓库ID
     */
    private Long warehouseId;

    @Schema(description = "模板标识")
    private String identificationTemplate;
    /**
     * 抖音id
     */
    private String tiktokId;
    /**
     * 美团id
     */
    private String meituanId;
    /**
     * 饿了么id
     */
    private String hungryId;
    /**
     * 门店状态（0 正常营业 1 闭店）
     */
    private Integer storeStatus;
    /**
     * 门店状态（1 经营中 2 闭店）
     */
    private Integer openStatus;
    /**
     * 门店地址
     */
    private String storeAddress;
    /**
     * 门店是否支持外卖（0支持，1不支持）
     */
    private Integer storeTakeaway;

    /**
     * 校园配送开关：0支持 1不支持
     */
    private Integer campusDeliveryStatus;

    /**
     * 校园配送补贴
     */
    private BigDecimal campusDeliverySubsidy;

    /**
     * 校园配送起送费
     */
    private BigDecimal campusMinimumDeliveryFee;

    /**
     * 校园配送配送费
     */
    private BigDecimal campusDeliveryFee;

    /**
     * 校园配送费用计算方式：0按商品 1按订单
     */
    private Integer campusDeliveryCalculationType;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 是否和配送路线相同 0：是 1：否
     */
    private Integer isSameLine;


    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 项目id
     */
    private Long projectId;


    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 配送线路id
     */
    private Long deliveryLineId;

    /**
     * 配送线路名称
     */
    private String deliveryLineName;

    /**
     * 起订金额
     */
    private BigDecimal startBuyAmount;

    /**
     * 预存款金额
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private BigDecimal advanceAmount;

    /**
     * 授信额度
     */
    private BigDecimal believeAmount;

    /**
     * 供应链收货地址
     */
    private String supplyAddress;

    /**
     * 供应链门店状态 1正常 2停用
     */
    private Integer useStatus;

    /**
     * 项目归属ID
     */
    private Long projectOwnerShip;

    /**
     * 门店状态（1 经营中 2 闭店）
     */
    private Integer isOpenStatus;

    /**
     * 额外营业时间
     */
    private String storeHoursExtra;

    /**
     * 外卖额外时间
     */
    private String deliveryTimeExtra;

    /**
     * 打包费设置
     */
    private String packageSetting;

    /**
     * 外卖开始时间
     */
    private LocalTime deliveryStartTime;

    /**
     * 外卖结束时间
     */
    private LocalTime deliveryEndTime;
    /**
     * 是否开启密码
     */
    private Integer isOpenPassword;

    /**
     * 是否是全部套餐 0是 1否
     */
    private Integer isAllProduct;

    /**
     * 是否是全部商品 0是 1否 2啥也不选
     */
    private Integer isAllCommdity;
    /**
     * 高峰时段
     */
    private String peakHours;
    /**
     * 出餐时间
     */
    private String mealTime;

    /**
     * 是否弹出提示窗  0 否 1是
     */
    private Integer isPrompt;
    /**
     * 弹窗提示文案
     */
    private String promptText;
    @Schema(description = "门店背景图片")
    private String storeBackgroundImage;
}

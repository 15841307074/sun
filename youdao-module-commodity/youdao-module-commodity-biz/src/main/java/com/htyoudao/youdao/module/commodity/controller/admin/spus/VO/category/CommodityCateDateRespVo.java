package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 查询分类下的商品 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityCateDateRespVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


    @Schema(description = "关联商品分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;


    @Schema(description = "商品描述")
    private String description;

    /** 商品顺序 */
    @Schema(description = "商品顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;



    @Schema(description = "回显备注")
    private String dictValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否是单品 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isSingle;

    @Schema(description = "单点不送（0关闭  1开启）",requiredMode = Schema.RequiredMode.REQUIRED)
    private String spuDetailUrl;


    @Schema(description = "门店上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店上下架状态不能为空")
    private Integer storeStatus;


    @Schema(description = "小程序上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "小程序上下架状态不能为空")
    private Integer wxStatus;




    @Schema(description = "销量")
    private Long salesVolumes;



    @Schema(description = "商品所属分组名")
    private String categoryName;


    @Schema(description = "打包没多少份",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer manyCopy;

    @Schema(description = "打包费（单位元）",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal packageFee;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer saleRule;

    @Schema(description = "最少购买多少份",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitBuyNumber;


    @Schema(description = "每天限购",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitDayBuyNumber;

    @Schema(description = "每单限购",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitOrderBuyNumber;

    @Schema(description = "套餐类型（1.固定搭配套餐，2.分组可选套餐）",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer setmealType;

    @Schema(description = "适用人数",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicableNumber;

    @Schema(description = "小料是否多选 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer condimentIsMore;

    @Schema(description = "最多可加小料数量",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCondimentNumber;

    @Schema(description = "最低价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal lowPrice;

    @Schema(description = "最高价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal highPrice;

    @Schema(description = "最低划线价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal lowStrikePrice;

    @Schema(description = "最高划线价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal highStrikePrice;

    @Schema(description = "是否多规格",requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isMoreSku;

    @Schema(description = "商品里 sku 列表",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommoditySkus> commoditySkusList = new ArrayList<>();

    @Schema(description = "商品标签集合",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityTag> commodityTagReqVOList = new ArrayList<>();

    @Schema(description = "图片地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> imageUrlVO;

    /**
     * 是否开启分时置顶 1是 0否
     */
    private Integer timeSharingTopping;


    /**
     * 商品是否上架锁定 1锁定 0未锁定
     */
    @Schema(description = "商品是否上架锁定 1锁定 0未锁定",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer shelfLock;


    /**
     * 是否置顶
     */
    @TableField(exist = false)
    private Boolean isUp;


    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endDate;

    /**
     * 指定日期逗号分割
     */
    private String dayNumbers;

    /**
     * 指定周几逗号分割
     */
    private String weekNumbers;
    /**
     * 是否全天时段 1是 0否
     */
    private Integer isAllDay;

    /**
     * 指定时间段
     */
    private String timeRange;



    private List<Integer> dayNumberList = new ArrayList<>();

    private List<Integer> weekNumberList = new ArrayList<>();

    private List<String> timeRangeList  = new ArrayList<>();

    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if(ObjectUtil.isNotEmpty(dayNumberList)){
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.dayNumbers = "";
        }
    }
    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if(ObjectUtil.isNotEmpty(weekNumberList)){
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.weekNumbers = "";
        }
    }
    public void setTimeRangeList(List<String> timeRangeList) {
        this.timeRangeList = timeRangeList;
        if(ObjectUtil.isNotEmpty(timeRangeList)){
            this.timeRange = timeRangeList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.timeRange = "";
        }
    }
    public void setDayNumbers(String dayNumbers) {
        this.dayNumbers = dayNumbers;
        if(ObjectUtil.isNotEmpty(dayNumbers)){
            String[] array = dayNumbers.split(",");
            this.dayNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.dayNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.dayNumberList = null;
        }
    }
    public void setWeekNumbers(String weekNumbers) {
        this.weekNumbers = weekNumbers;
        if(ObjectUtil.isNotEmpty(weekNumbers)){
            String[] array = weekNumbers.split(",");
            this.weekNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.weekNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.weekNumbers = null;
        }
    }
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        if(ObjectUtil.isNotEmpty(timeRange)){
            String[] array = timeRange.split(",");
            this.timeRangeList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.timeRangeList.add(array[i]);
            }
        }else {
            this.timeRangeList = null;
        }
    }
}

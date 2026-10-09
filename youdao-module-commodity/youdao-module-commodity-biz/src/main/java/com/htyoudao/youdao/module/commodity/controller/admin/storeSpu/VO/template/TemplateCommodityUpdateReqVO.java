package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCondiments;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityFlavor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
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
import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "管理后台 - 模板商品更新 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCommodityUpdateReqVO {

    @Schema(description = "模板商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板商品ID不能为空")
    private Long commodityTemplateId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;

    @Schema(description = "关联模板分类ID")
    private Long categoryTemplateId;

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "排序")
    private Long sort;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "字典键值")
    private String dictValue;

    @Schema(description = "是否单品")
    private Integer isSingle;

    @Schema(description = "小程序状态")
    private Integer wxStatus;

    @Schema(description = "门店状态")
    private Integer storeStatus;

    @Schema(description = "打包费")
    private BigDecimal packageFee;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）")
    private Integer saleRule;

    @Schema(description = "最少购买数量")
    private Integer limitBuyNumber;

    @Schema(description = "每天限购")
    private Integer limitDayBuyNumber;

    @Schema(description = "每单限购")
    private Integer limitOrderBuyNumber;

    @Schema(description = "套餐类型")
    private Integer setmealType;

    @Schema(description = "适用人数")
    private Integer applicableNumber;

    @Schema(description = "打包份数")
    private Integer manyCopy;

    @Schema(description = "小料是否多选")
    private Integer condimentIsMore;

    @Schema(description = "最多小料数量")
    private Integer maxCondimentNumber;

    @Schema(description = "图片地址")
    private List<String> imageUrlVO = new ArrayList<>();

    @Schema(description = "规格列表")
    private List<CommodityTemplateSkus> commodityTemplateSkusList = new ArrayList<>();

    @Schema(description = "属性列表")
    private List<CommodityFlavor> flavorList = new ArrayList<>();

    @Schema(description = "小料列表")
    private List<CommodityCondiments> condimentsList = new ArrayList<>();

    @Schema(description = "套餐分组列表")
    private List<TemplateCommodityGroupSaveVO> groupList = new ArrayList<>();

    // ==================== 可编辑的业务字段 ====================

    @Schema(description = "商品标签")
    private String spusTag;

    @Schema(description = "商品标签集合")
    private List<CommodityTag> commodityTagList = new ArrayList<>();

    @Schema(description = "是否开启分时置顶")
    private Integer timeSharingTopping;


    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "商品详情图")
    private String spuDetailUrl;


    @Schema(description = "商品说明")
    @NotNull(message = "商品说明不能为空")
    private String goodsExplain;


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
     * 指定时间段
     */
    private String timeRange;

    /**
     * 是否全天时段 1是 0否
     */
    private Integer isAllDay;


    private List<Integer> dayNumberList;

    private List<Integer> weekNumberList;

    private List<String> timeRangeList;
}

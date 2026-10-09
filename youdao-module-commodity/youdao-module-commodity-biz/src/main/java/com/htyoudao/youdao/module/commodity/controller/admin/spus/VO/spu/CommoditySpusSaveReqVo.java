package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group.CommodityGroupSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku.CommoditySkuSaveVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 商品新增 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySpusSaveReqVo {


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品名称不能为空")
    private String commodityName;


    @Schema(description = "关联商品分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联商品分类ID不能为空")
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
    @NotNull(message = "单品类型不能为空")
    private Integer isSingle;





    @Schema(description = "门店上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;


    @Schema(description = "小程序上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;


    @Schema(description = "商品所属分组名")
    @NotNull(message = "商品所属分组名不能为空")
    private String categoryName;

    @Schema(description = "商品说明")
    @NotNull(message = "商品说明不能为空")
    private String goodsExplain;




    @Schema(description = "打包每多少份",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "打包每多少份不能为空")
    private Integer manyCopy;

    @Schema(description = "打包费（单位元）",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal packageFee;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售卖规则不能为空")
    private Integer saleRule;

    @Schema(description = "单点不送（0关闭  1开启）",requiredMode = Schema.RequiredMode.REQUIRED)
    private String spuDetailUrl;

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

    @Schema(description = "图片地址",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片地址不能为空")
    private List<String> imageUrlVO= new ArrayList<>();



    @Schema(description = "商品标签集合",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> tagIdList = new ArrayList<>();

    /**商品里的属性集合 */
    @Schema(description = "商品里的属性集合",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityFlavor> commodityFlavorList = new ArrayList<>();


    @Schema(description = "商品列表里所有添加的小料",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityCondiments> commodityCondimentsList  =  new ArrayList<>();

    @Schema(description = "商品里sku列表",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品里sku列表不能为空")
    private List<CommoditySkuSaveVO> commoditySkusList = new ArrayList<>();

    @Schema(description = "商品里分组列表",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityGroupSaveVO> commodityGroupList = new ArrayList<>();



    @Schema(description = "是否为分时场景",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer timeSharingTopping;







    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @Schema(description = "开始时间")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @Schema(description = "结束时间")
    private Date endDate;

    /**
     * 是否全天时段 1是 0否
     */
    @Schema(description = "是否全天时段 1是 0否")
    private Integer isAllDay;



    @Schema(description = "日期集合")
    private List<Integer> dayNumberList;
    @Schema(description = "周几集合")
    private List<Integer> weekNumberList;
    @Schema(description = "时间段集合")
    private List<String> timeRangeList;


}

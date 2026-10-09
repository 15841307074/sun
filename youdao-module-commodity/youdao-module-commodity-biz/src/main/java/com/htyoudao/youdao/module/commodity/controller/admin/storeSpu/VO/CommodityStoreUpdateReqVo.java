package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeGroup.CommodityStoreGroupSaveVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCondiments;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityFlavor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
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

@Schema(description = "管理后台 - 更新门店 RequestVO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreUpdateReqVo {


    @Schema(description = "门店下商品的唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityStoreSpuId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long storeId;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityId;


    @Schema(description = "门店下商品分类的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityStoreCategoryId;

    @Schema(description = "商品原始 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityStorePrimitiveSpuId;

    @Schema(description = "门店下商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private String commodityStoreSpuName;

    @Schema(description = "门店下商品的详情", requiredMode = Schema.RequiredMode.REQUIRED)

    private String commodityStoreSpuDescription;


    @Schema(description = "门店下商品的排序顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSpuSort;

    @Schema(description = "门店下商品的单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSpuUnit;

    @Schema(description = "字典键值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;


    @Schema(description = "门店下商品是否是单品（TRUE是 FALSE否）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Integer commodityStoreSpuIsSingle;




    @Schema(description = "门店下商品的点餐机状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSpuMachineStatus;


    @Schema(description = "门店下商品的小程序状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSpuAppletStatus;


    @Schema(description = "门店下商品是否锁", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean commodityStoreSpuLock;


    @Schema(description = "总部强制门店下架状态（0 否， 1是）", requiredMode = Schema.RequiredMode.REQUIRED)
    private int storeForceStatus;

    @Schema(description = "总部强制微信小程序下架状态（0 否， 1是）", requiredMode = Schema.RequiredMode.REQUIRED)
    private int wxForceStatus;


    @Schema(description = "销量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer salesVolumes;



    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;

    @Schema(description = "商品说明")
    @NotNull(message = "商品说明不能为空")
    private String goodsExplain;


    @Schema(description = "打包费（单位元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal packageFee;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer saleRule;

    @Schema(description = "单点不送（0关闭  1开启）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSpuDetailUrl;

    @Schema(description = "最少购买多少份", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitBuyNumber;

    @Schema(description = "每天限购", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitDayBuyNumber;

    @Schema(description = "每单限购", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitOrderBuyNumber;

    @Schema(description = "套餐类型（1.固定搭配套餐，2.分组可选套餐）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer setmealType;

    @Schema(description = "适用人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicableNumber;

    @Schema(description = "模版属性（1.允许门店自己管理，2.品牌方统一管理）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer templateFlavor;

    @Schema(description = "是否只允许修改价格 1 是 2 否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer choosePrice;


    @Schema(description = "打包份每份多少钱", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer manyCopy;

    @Schema(description = "小料是否多选", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer condimentIsMore;

    @Schema(description = "最多可加小料数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCondimentNumber;





    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> imageUrlVO = new ArrayList<>();;

    @Schema(description = "商品里 sku 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityStoreSkuRespVo> commoditySkusList = new ArrayList<>();


    @Schema(description = "商品里的属性集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityFlavor> flavorList = new ArrayList<>();


    @Schema(description = "商品列表里所有添加的小料", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityCondiments> condimentsList  =  new ArrayList<>();


    @Schema(description = "套餐里分组添加的单品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityStoreGroupSaveVO> groupList = new ArrayList<>();

    @Schema(description = "标签 ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> tagIds = new ArrayList<>();

    /**
     * 是否开启分时置顶 1是 0否
     */
    private Integer timeSharingTopping;





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
     * 是否全天时段 1是 0否
     */
    private Integer isAllDay;





    private List<Integer> dayNumberList;

    private List<Integer> weekNumberList;

    private List<String> timeRangeList;



}

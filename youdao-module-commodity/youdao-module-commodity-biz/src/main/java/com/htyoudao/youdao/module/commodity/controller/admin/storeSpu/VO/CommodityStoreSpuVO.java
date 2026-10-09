package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.TableField;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CommodityStoreSpuVO {

    /**
     * 门店下商品的唯一ID
     */

    private Long commodityStoreSpuId;
    /**
     * 商品ID（原始 id）
     */
    private Long commodityId;

    /**
     * 门店ID
     */

    private Long storeId;


    private List<Long> storeIds;


    private List<Long> commodityStoreSpuIds;

    /**
     * 门店下商品分类ID
     */

    private Long commodityStoreCategoryId;

    /**
     * 门店下商品分类ID（多选）
     */

    private List<Long> commodityStoreCategoryIds;

    /**
     * 门店下商品名称
     */
    private String commodityStoreSpuName;

    private List<String>  commodityStoreSpuDetailUrlList;

    /**
     * 门店下商品的详情
     */
    private String commodityStoreSpuDescription;

    /**
     * 门店下商品的排序顺序
     */
    private Integer commodityStoreSpuSort;

    /**
     * 门店下商品的单位
     */
    private String commodityStoreSpuUnit;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 上次修改人
     */
    private String updateBy;

    /**
     * 上次修改时间
     */
    private Date updateAt;

    /**
     * 逻辑删除标识 （0 正常 2删除）
     */
    private String delFlag;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 门店下商品是否是单品（TRUE是 FALSE否）
     */
    private Integer commodityStoreSpuIsSingle;

    /**
     * 门店下商品的详情图
     */
    private String commodityStoreSpuDetailUrl;

    /**
     * 门店下商品的点餐机状态
     */
    private Integer commodityStoreSpuMachineStatus;

    /**
     * 门店下商品的小程序状态
     */
    private Integer commodityStoreSpuAppletStatus;

    /**
     * 门店下商品是否锁
     */
    private Boolean commodityStoreSpuLock;




    /**
     * 查询条件 1 全部 2 上架 3 下架
     */
    @TableField(exist = false)
    private Integer selectView;

}

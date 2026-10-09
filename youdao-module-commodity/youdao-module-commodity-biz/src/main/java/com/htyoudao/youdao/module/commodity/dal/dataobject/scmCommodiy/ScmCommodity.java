package com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 供应链商品表实体类
 * 对应数据库表：scm_commodity
 */
@Data
@TableName("scm_commodity")
public class ScmCommodity {

    /**
     * ID 主键
     */
    private Long id;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 类别ID
     */
    private Long categoryId;

    /**
     * 类别
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品编号
     */
    private String commodityCode;

    /**
     * 商品统计名称
     */
    private String stasticsCommodityName;

    /**
     * 规格
     */
    private String specifications;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 类型
     */
    private String commodityType;

    /**
     * 最小单位
     */
    private String minUnit;

    /**
     * 入库单价
     */
    private BigDecimal incomePrice;

    /**
     * 入库单位
     */
    private String incomeUnit;

    /**
     * 出库单价
     */
    private BigDecimal outPrice;

    /**
     * 出库单位
     */
    private String outUnit;

    /**
     * 起订数量
     */
    private Integer minBuyNumber;

    /**
     * 起订单位
     */
    private String minBuyUnit;

    /**
     * 订货限量
     */
    private Integer limitedNumber;

    /**
     * 订货限量单位
     */
    private String limitedUnit;

    /**
     * 库存下限单位
     */
    private String lowerUnit;

    /**
     * 库存上限单位
     */
    private String upUnit;

    /**
     * 库存下限预警天数
     */
    private Integer lowerWarningDays;

    /**
     * 库存上限预警天数
     */
    private Integer upWarningDays;

    /**
     * 买赠活动，购买数量
     */
    private Integer buyNumber;

    /**
     * 买赠活动，购买单位
     */
    private String buyUnit;

    /**
     * 买赠活动，赠送数量
     */
    private Integer presentNumber;

    /**
     * 买赠活动，赠送单位
     */
    private String presentUnit;

    /**
     * 是否上架，0否 1是
     */
    private Integer onTheShelf;

    /**
     * 负库存销售开关，0否 1是
     */
    private Integer negativeSales;

    /**
     * 仅套餐售卖开关，0否 1是
     */
    private Integer justPackageSales;

    /**
     * 详情，富文本
     */
    private String packageDetail;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 类别编码
     */
    private String categoryCode;

    /**
     * 促销开关(0 关闭  1 开启)
     */
    private Integer promotionSwitch;

    /**
     * 库存下限数量
     */
    private Integer lowerLimitNumber;

    /**
     * 库存上限数量
     */
    private Integer upLimitNumber;

    /**
     * 停用状态(0停用 1启用)
     */
    private Integer isEnable;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 删除标识(0正常 1删除)
     */
    private Integer isDelete;

    /**
     * 商品备注
     */
    private String remarks;
}

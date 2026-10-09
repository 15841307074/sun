package com.htyoudao.youdao.module.analysis.dal.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(indexName = "scm_order_full")
@Setting(shards = 3, replicas = 1)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmOrderDetailDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = -4348691934074969889L;

    @Id
    @Field(name = "id", type = FieldType.Long)
    private Long id;

    @Field(name = "orderId", type = FieldType.Long)
    private Long orderId;

    @Field(name = "warehouseName", type = FieldType.Keyword)
    private String warehouseName;

    @Field(name = "warehouseId", type = FieldType.Long)
    private Long warehouseId;

    @Field(name = "storeId", type = FieldType.Long)
    private Long storeId;

    @Field(name = "storeName", type = FieldType.Keyword)
    private String storeName;

    @Field(name = "projectName", type = FieldType.Keyword)
    private String projectName;

    @Field(name = "projectId", type = FieldType.Long)
    private Long projectId;

    @Field(name = "projectCode", type = FieldType.Keyword)
    private String projectCode;

    @Field(name = "orderSn", type = FieldType.Keyword)
    private String orderSn;

    @Field(name = "parentOrderDetailId", type = FieldType.Long)
    private Long parentOrderDetailId;

    @Field(name = "parentOrderStatus", type = FieldType.Integer)
    private Integer parentOrderStatus;

    @Field(name = "orderType", type = FieldType.Integer)
    private Integer orderType;

    @Field(name = "groupId", type = FieldType.Long)
    private Long groupId;

    @Field(name = "packageId", type = FieldType.Long)
    private Long packageId;

    @Field(name = "packageName", type = FieldType.Keyword)
    private String packageName;

    @Field(name = "packageNumber", type = FieldType.Integer)
    private Integer packageNumber;

    @Field(name = "categoryId", type = FieldType.Long)
    private Long categoryId;

    @Field(name = "categoryName", type = FieldType.Keyword)
    private String categoryName;

    @Field(name = "categoryCode", type = FieldType.Keyword)
    private String categoryCode;

    @Field(name = "customId", type = FieldType.Long)
    private Long customId;

    @Field(name = "customName", type = FieldType.Keyword)
    private String customName;

    @Field(name = "customPhone", type = FieldType.Keyword)
    private String customPhone;

    @Field(name = "orderStatus", type = FieldType.Integer)
    private Integer orderStatus;

    @Field(name = "deliveryDate", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date deliveryDate;

    @Field(name = "deliveryLineId", type = FieldType.Long)
    private Long deliveryLineId;

    @Field(name = "deliveryLineName", type = FieldType.Keyword)
    private String deliveryLineName;

    @Field(name = "brandName", type = FieldType.Keyword)
    private String brandName;

    @Field(name = "brandId", type = FieldType.Long)
    private Long brandId;

    @Field(name = "specifications", type = FieldType.Keyword)
    private String specifications;

    @Field(name = "commodityId", type = FieldType.Long)
    private Long commodityId;

    @Field(name = "commodityName", type = FieldType.Keyword)
    private String commodityName;

    @Field(name = "commodityCode", type = FieldType.Keyword)
    private String commodityCode;

    @Field(name = "commodityType", type = FieldType.Keyword)
    private String commodityType;

    @Field(name = "stasticsCommodityName", type = FieldType.Keyword)
    private String stasticsCommodityName;

    @Field(name = "commoditySingleAmount", type = FieldType.Double)
    private BigDecimal commoditySingleAmount;

    @Field(name = "commodityUnit", type = FieldType.Keyword)
    private String commodityUnit;

    @Field(name = "minUnit", type = FieldType.Keyword)
    private String minUnit;

    @Field(name = "minNumber", type = FieldType.Integer)
    private Integer minNumber;

    @Field(name = "maxNumber", type = FieldType.Double)
    private Double maxNumber;

    @Field(name = "minAmount", type = FieldType.Double)
    private BigDecimal minAmount;

    @Field(name = "unitConversion", type = FieldType.Keyword)
    private String unitConversion;

    @Field(name = "commodityNumber", type = FieldType.Integer)
    private Integer commodityNumber;

    @Field(name = "commodityAmount", type = FieldType.Double)
    private BigDecimal commodityAmount;

    @Field(name = "commodityImage", type = FieldType.Keyword)
    private String commodityImage;

    @Field(name = "returnNumber", type = FieldType.Integer)
    private Integer returnNumber;

    @Field(name = "refundReturnNumber", type = FieldType.Integer)
    private Integer refundReturnNumber;

    @Field(name = "maxRefundReturnNumber", type = FieldType.Double)
    private Double maxRefundReturnNumber;

    @Field(name = "returnAmount", type = FieldType.Double)
    private BigDecimal returnAmount;

    @Field(name = "returnUnit", type = FieldType.Keyword)
    private String returnUnit;

    @Field(name = "costAmount", type = FieldType.Double)
    private BigDecimal costAmount;

    @Field(name = "breakNumber", type = FieldType.Integer)
    private Integer breakNumber;

    @Field(name = "maxBreakNumber", type = FieldType.Double)
    private Double maxBreakNumber;

    @Field(name = "enterAgainNumber", type = FieldType.Integer)
    private Integer enterAgainNumber;

    @Field(name = "maxEnterAgainNumber", type = FieldType.Double)
    private Double maxEnterAgainNumber;

    @Field(name = "enterAgainAmount", type = FieldType.Double)
    private BigDecimal enterAgainAmount;

    @Field(name = "receiveReturnNumber", type = FieldType.Integer)
    private Integer receiveReturnNumber;

    @Field(name = "receiveReturnAmount", type = FieldType.Double)
    private BigDecimal receiveReturnAmount;

    @Field(name = "breakAmount", type = FieldType.Double)
    private BigDecimal breakAmount;

    @Field(name = "createTime", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date createTime;

    @Field(name = "createUserName", type = FieldType.Keyword)
    private String createUserName;

    @Field(name = "updateTime", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date updateTime;

    @Field(name = "updateUserName", type = FieldType.Keyword)
    private String updateUserName;

    @Field(name = "isDelete", type = FieldType.Integer)
    private Integer isDelete;

    @Field(name = "groupByOrderField", type = FieldType.Keyword)
    private String groupByOrderField;

    @Field(name = "groupByReturnField", type = FieldType.Keyword)
    private String groupByReturnField;


    //-------------------------以下为order主表数据字段（退款字段特殊 改名为returnAmountTotal）--------------------------
    @Field(name = "parentOrderSn", type = FieldType.Keyword)
    private String parentOrderSn;

    @Field(name = "parentOrderDate", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date parentOrderDate;

    @Field(name = "pickUpOrderCount", type = FieldType.Integer)
    private Integer pickUpOrderCount;

    @Field(name = "deliveryOrderCount", type = FieldType.Integer)
    private Integer deliveryOrderCount;

    @Field(name = "receiveAddress", type = FieldType.Keyword)
    private String receiveAddress;

    @Field(name = "receiveName", type = FieldType.Keyword)
    private String receiveName;

    @Field(name = "receivePhone", type = FieldType.Keyword)
    private String receivePhone;

    @Field(name = "commodityNames", type = FieldType.Keyword)
    private String commodityNames;

    @Field(name = "commodityCodes", type = FieldType.Keyword)
    private String commodityCodes;

    @Field(name = "logisticsName", type = FieldType.Keyword)
    private String logisticsName;

    @Field(name = "logisticsCode", type = FieldType.Keyword)
    private String logisticsCode;

    @Field(name = "sendNumber", type = FieldType.Integer)
    private Integer sendNumber;

    @Field(name = "orderAmount", type = FieldType.Double)
    private BigDecimal orderAmount;

    @Field(name = "returnAmountTotal", type = FieldType.Double)
    private BigDecimal returnAmountTotal;

    @Field(name = "payMethod", type = FieldType.Keyword)
    private String payMethod;

    @Field(name = "auditName", type = FieldType.Keyword)
    private String auditName;

    @Field(name = "auditStatus", type = FieldType.Integer)
    private Integer auditStatus;

    @Field(name = "auditDate", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date auditDate;

    @Field(name = "remark", type = FieldType.Keyword)
    private String remark;

    @Field(name = "orderIsDelete", type = FieldType.Integer)
    private Integer orderIsDelete;

    @Field(name = "orderCreateTime", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date orderCreateTime;

    @Field(name = "orderUpdateTime", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date orderUpdateTime;
}
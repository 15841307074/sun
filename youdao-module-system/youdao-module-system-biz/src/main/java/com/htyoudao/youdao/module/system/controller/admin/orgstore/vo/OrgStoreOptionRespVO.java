package com.htyoudao.youdao.module.system.controller.admin.orgstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 门店选择项")
public class OrgStoreOptionRespVO {

    private Long storeId;
    private String storeName;
    private Long orgId;
    private String orgName;
    private String storeCity;
    private String storeCityName;
    private String storeAddress;
    private Integer storeStatus;
    private Integer openStatus;
    private Integer useStatus;
    private String identificationTemplate;
    private Integer isAllProduct;
    private Integer isAllCommdity;
    private Integer isSameLine;
    private Long warehouseId;
    private String warehouseName;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long deliveryLineId;
    private String deliveryLineName;
    private Long tiktokId;
}

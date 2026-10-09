package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 门店查询分页 Resp VO")
@ToString(callSuper = true)
@Data
public class OrgStorePageRespVO implements Serializable {

    @Schema(description = "组织ID", example = "19880")
    private Long orgId;

    @Schema(description = "组织名称", example = "张三")
    private Long storeId;

    @Schema(description = "门店名称", example = "张三")
    private String storeName;
}
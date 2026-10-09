package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 门店查询 Resp VO")
@ToString(callSuper = true)
@Data
public class OrgStoreRespVO implements Serializable {

    @Schema(description = "组织名称", example = "张三")
    private Long storeId;

    @Schema(description = "门店名称", example = "张三")
    private String storeName;

    @Schema(description = "门店状态(0 正常营业 1 闭店)", example = "0")
    private Integer storeStatus;

    /**
     * 门店负责人 id
     */
    @Schema(description = "门店负责人 id", example = "1024")
    private Long userId;

    /**
     * 门店负责人姓名
     */
    @Schema(description = "门店负责人姓名", example = "张三")
    private String storeLeader;

    /**
     * 门店电话
     */
    private String storePhone;

    @Schema(description = "组织id", example = "1")
    private Long orgId;

}

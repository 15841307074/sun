package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 通过组织查询门店 Response VO")
public class AppOrgStoreRespVO {

    @Schema(description = "组织ID", example = "1")
    private Long id;

    @Schema(description = "组织名称", example = "张三")
    private String name;

    @Schema(description = "父部门 ID", example = "1024")
    private Long parentId;

    @Schema(description = "0部门 1门店", example = "1")
    private Integer orgFlag;

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
}

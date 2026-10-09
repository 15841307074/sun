package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "app - 老板助手全部人员 + 部门 Response VO")
@Data
public class AppOrgRespVO {

    @Schema(description = "组织ID", example = "1")
    private Long id;

    @Schema(description = "组织名称", example = "张三")
    private String name;

    @Schema(description = "父部门 ID", example = "1024")
    private Long parentId;

    @Schema(description = "0组织 1门店", example = "1")
    private Integer orgFlag;

    @Schema(description = "等级", example = "1")
    private Integer level;


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

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer sort;

}

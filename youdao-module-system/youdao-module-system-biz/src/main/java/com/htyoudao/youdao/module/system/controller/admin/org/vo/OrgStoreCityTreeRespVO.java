package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构树 Resp VO")
@Data
@ToString(callSuper = true)
public class OrgStoreCityTreeRespVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @ExcelProperty("组织ID")
    private Long id;

    @Schema(description = "城市名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("城市名称")
    private String storeCity;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("组织名称")
    private String name;

    @Schema(description = "祖级列表")
    @ExcelProperty("祖级列表")
    private String ancestors;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @ExcelProperty("上级ID")
    private Long parentId;

    @Schema(description = "层级编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("层级编号")
    private Integer level;
    @Schema(description = "门店Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("门店Id")
    private Long storeId;
    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("门店名称")
    private String  storeName;
    @Schema(description = "空数组，前端需要", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SystemStoreInfoDO> children;


    @Schema(description = "是否是门店 0 组织 1 门店")
    private int isStore ;

    @Schema(description = "门店数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeNum =0;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer sort;
}

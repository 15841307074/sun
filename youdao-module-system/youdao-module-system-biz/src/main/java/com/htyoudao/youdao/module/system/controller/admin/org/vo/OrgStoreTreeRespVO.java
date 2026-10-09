package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import com.alibaba.excel.annotation.ExcelProperty;
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
public class OrgStoreTreeRespVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @ExcelProperty("组织ID")
    private Long id;

    @Schema(description = "城市名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("城市名称")
    private String storeCity;

    private String storeCityName;

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
    private List<Long> children;

    @Schema(description = "是否在本级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isMyOrg= 0;
    @Schema(description = "是否是门店 0 组织 1 门店")
    private int isStore ;

    @Schema(description = "门店数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeNum =0;

    @Schema(description = "是否需要点击 true  不能点默认")
    private Boolean isFlag = true;

    @Schema(description = "模板标识")
    private String identificationTemplate;

    /**
     * 是否是全部套餐 0是 1否
     */
    @Schema(description = "是否是全部套餐 0是 1否")
    private Integer isAllProduct;

    /**
     * 是否是全部商品 0是 1否 2啥也不选
     */
    @Schema(description = "是否是全部商品 0是 1否 2啥也不选")
    private Integer isAllCommdity;

    /**
     * 是否和配送路线相同 0：是 1：否
     */
    @Schema(description = "是否和配送路线相同 0：是 1：否")
    private Integer isSameLine;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private Long projectId;

    /**
     * 项目编号
     */
    @Schema(description = "项目编号")
    private String projectCode;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 配送线路id
     */
    @Schema(description = "配送线路id")
    private Long deliveryLineId;

    /**
     * 配送线路名称
     */
    @Schema(description = "配送线路名称")
    private String deliveryLineName;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer sort;
}

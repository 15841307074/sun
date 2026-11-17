package com.htyoudao.youdao.module.system.controller.admin.orgstore.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 平铺门店列表项")
public class OrgStoreListRespVO extends OrgStoreOptionRespVO {

    @Schema(description = "节点编号，与 storeId 一致")
    private Long id;

    @Schema(description = "节点名称，与 storeName 一致")
    private String name;

    @Schema(description = "所属组织编号，没有有效所属组织时为 0")
    private Long parentId;

    @Schema(description = "所属组织层级加 1，没有有效所属组织时为 1")
    private Integer level;

    @Schema(description = "兼容平铺节点字段，门店节点为空")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String ancestors;

    @Schema(description = "兼容平铺节点字段，门店节点为空")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<Long> children;

    @Schema(description = "门店节点固定为 1")
    private int isStore = 1;

    @Schema(description = "门店节点固定为 0")
    private int storeNum;

    @Schema(description = "兼容平铺节点字段，门店节点为空")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Integer sort;
}

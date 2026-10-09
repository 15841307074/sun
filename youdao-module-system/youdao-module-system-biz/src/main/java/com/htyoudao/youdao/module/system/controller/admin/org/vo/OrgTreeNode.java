package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

// 组织树节点VO
@Schema(description = "管理后台 - 组织树节点 VO")
@Data
public class OrgTreeNode {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "是否门店 1门店 0组织")
    private Integer isStore;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "是否允许操作，true允许，false不允许")
    private Boolean operable;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer sort;

    @Schema(description = "子级列表")
    private List<OrgTreeNode> children = new ArrayList<>();


    @Schema(description = "门店是否停用 停用:true 正常:false")
    private Boolean storeUseStatus;

}
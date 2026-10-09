package com.htyoudao.youdao.module.system.api.org.dto;

import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "RPC 服务 - 组织 Response DTO")
@Data
public class OrgRespDTO implements VO, Serializable {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private Long id;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Long parentId;

    @Schema(description = "层级编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;

    @Schema(description = "门店数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeNum;

    @Schema(description = "人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userNum;

    @Schema(description = "空数组，前端需要", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> children;

}

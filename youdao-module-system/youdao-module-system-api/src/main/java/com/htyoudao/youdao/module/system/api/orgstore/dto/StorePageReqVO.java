package com.htyoudao.youdao.module.system.api.orgstore.dto;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorePageReqVO extends PageParam {
    @Schema(description = "门店名称/门店编码，模糊匹配", example = "youdao")
    private String text;
    @Schema(description = "门店状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer storeStatus;
    @Schema(description = "经营状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer openStatus;
    @Schema(description = "部门编号，同时筛选子部门", example = "1024")
    private Long orgId;
    @Schema(description = "标签id", example = "1024")
    private Long tagId;
    private Set<Long> orgIds;
}

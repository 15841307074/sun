package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 33483
 */
@Schema(description = "管理后台 - 门店查询分页 Request VO")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class OrgStorePageReqVO extends PageParam {

    @Schema(description = "组织ID", example = "19880")
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    @Schema(description = "门店名称", example = "19880")
    private String storeName;
}

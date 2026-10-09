package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;


import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 审核请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核请求参数")
public class AuditReqVO {

    @NotNull(message = "跑腿员ID不能为空")
    @Schema(description = "跑腿员ID", required = true)
    private Long runnerId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1通过 2失败", required = true)
    private Integer auditStatus;

    @Schema(description = "审核失败原因（审核失败时必填）")
    private String auditReason;

}

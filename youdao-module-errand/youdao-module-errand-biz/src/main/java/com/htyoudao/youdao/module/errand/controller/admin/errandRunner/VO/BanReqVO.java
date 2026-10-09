package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 封禁请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "封禁请求参数")
public class BanReqVO {

    @NotNull(message = "跑腿员ID不能为空")
    @Schema(description = "跑腿员ID", required = true)
    private Long runnerId;

}

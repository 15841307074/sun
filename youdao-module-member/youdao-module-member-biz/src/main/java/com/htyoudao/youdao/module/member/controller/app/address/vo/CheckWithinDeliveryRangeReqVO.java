package com.htyoudao.youdao.module.member.controller.app.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-20
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckWithinDeliveryRangeReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5597446828126765517L;

    /**
     * 门店ID
     */
    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long storeId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @Schema(description = "范围标识", example = "0 跑腿不在门店范围内 ")
    private Integer rangFlag;

}

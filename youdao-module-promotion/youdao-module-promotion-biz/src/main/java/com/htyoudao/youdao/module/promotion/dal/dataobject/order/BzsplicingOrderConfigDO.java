package com.htyoudao.youdao.module.promotion.dal.dataobject.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BzsplicingOrderConfigDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6427856317165028489L;

    @Schema(description = "banner图片")
    private String bannerUrl;

    @Schema(description = "拼单折扣")
    private String discount;
}

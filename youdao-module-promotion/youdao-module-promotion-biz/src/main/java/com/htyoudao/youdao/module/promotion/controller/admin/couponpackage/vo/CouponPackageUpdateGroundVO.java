package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Data
public class CouponPackageUpdateGroundVO {

    @NotNull(message = "id 不能为空")
    private  Long id;

    @NotNull(message = "是否上架不能为空")
    private  Integer isGround;
}

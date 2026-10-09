package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 领取抖音优惠券请求参数
 * @author dht
 */
@Data
public class ClaimTiktokCouponReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4703355183888013413L;

    @Schema(description = "优惠券id", example = "赵六")
    @NotNull(message = "优惠券id不能为空")
    private Long couponId;

    /**
     * 到期时间
     */
    @Schema(description = "到期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    /**
     * 有效开始日期
     */
    @Schema(description = "有效开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date vaildStartTime;


    @Schema(description = "店铺id", example = "1")
    private Long storeId;
}

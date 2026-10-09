package com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@TableName("user_coupon_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Table(value = "user_coupon_record")
public class UserCouponStoreIdDO extends BusinessBaseDO {

    @Schema(description = "门店Id")
    private Long storeId;

    @Schema(description = "使用数量")
    private Long usedCount;
}

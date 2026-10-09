package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author dht
 */
@Schema(description = "优惠券使用的数据 按门店分组 Request VO")
@Data
@ToString(callSuper = true)
public class GoodCouponDateRespV2VO {

    PageResult<GoodCouponDateRespVO> pageResult;

    @Schema(description = "店铺数量")
    private Long storeNum;
}

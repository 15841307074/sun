package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class ActivitySeckillMemberVO {
    /**
     * 会员名称
     */
    @Schema(description = "会员名称")
    private String memberName;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    private String  memberMobile;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private String  payTime;

    /**
     * 购买商品
     */
    @Schema(description = "购买商品")
    private String commodityName;

    /**
     * 支付金额
     */
    @Schema(description = "支付金额")
    private Double payAmount;

    /**
     * 来源渠道
     */
    @Schema(description = "来源渠道")
    private String channelName;

    /**
     * 来源渠道
     */
    @Schema(description = "门店名称")
    private String storeName;





}
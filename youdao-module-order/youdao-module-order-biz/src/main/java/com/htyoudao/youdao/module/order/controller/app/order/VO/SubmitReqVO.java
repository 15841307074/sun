package com.htyoudao.youdao.module.order.controller.app.order.VO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * V3 下单入参
 * </p>
 *
 * @author zhangjihe
 * @since 2025-02-13
 */
@Data
public class SubmitReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2431658871189077889L;

    @Schema(description = "订单来源 1 小程序付款单 2 点餐机付款单 3 小程序现金单 4 点餐机现金单 5 外卖单 6 拼单 7 秒杀 8 代取单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单来源不能为空")
    private Integer source;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "拼单ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1278042588310429696")
    private String mainId;

    @Schema(description = "订单来源 0点餐机 1微信小程序 2支付宝", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单来源不能为空")
    private Integer orderFrom;

    @Schema(description = "订单类型 0堂食 1打包 2外卖 3代取", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "支付方式 0现金 1微信 2支付宝", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "支付方式不能为空")
    private Integer paymentCode;

    @Schema(description = "预约时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 11:12:30")
    private String appointmentTime;

    @Schema(description = "外卖手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "18202394837")
    private String takeAwayTel;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "132.19856")
    private String longitude;

    @Schema(description = "维度", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "46.334328")
    private String latitude;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "xxxxx")
    private String remark;

    @Schema(description = "防重复提交 token", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "xxxxx")
    private String antiRepeatToken;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1278042588310429696")
    private Long memberId;

    @Schema(description = "渠道")
    private Integer channel;

    @Schema(description = "跑腿员性别限制：0不限 1男 2女", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Integer errandGenderLimit;

    @Schema(description = "收货信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "{}")
    private ReceiverInfoVO receiverInfo;

    @Data
    public static class ReceiverInfoVO {

        @Schema(description = "收货人名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "张三")
        @Length(max = 10, message = "收货人名称长度不能超过10")
        @NotBlank(message = "收货人名称不能为空")
        private String receiverName;

        @Schema(description = "收货人电话", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "18202394837")
        @Length(max = 11, message = "收货人电话长度不能超过11")
        @NotBlank(message = "收货人电话不能为空")
        private String receiverMobile;

        @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "{}")
        @Length(max = 200, message = "收货地址长度不能超过200")
        @NotBlank(message = "收货地址不能为空")
        private String receiverAddress;
    }

    /**
     * 缓存key
     */
    @JsonIgnore
    private String cacheKey;

    /**
     * 取餐码
     */
    @JsonIgnore
    private String pickUpCode;

    /**
     * 取餐码
     */
    @JsonIgnore
    private LocalDateTime createTime;


}

package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExchangeReqVO {


    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会员ID
     */
    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;


    @Schema(name = "prizeState", description = "0 已发放   商品类型  1未填写收货地址  2 已填写地址 待发货  3已发货  9 退回 ")
    private Integer prizeState;


    /** 快递单号 */
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;


    /** 快递公司 */
    @Schema(name = "expressCompany", description = "快递公司")
    private String expressCompany;

    /** 收货地址 */
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;

    /** 收件人 */
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;

    /** 联系电话 */
    @Schema(name = "receiveMobile", description = "联系电话")
    private String receiveMobile;


}

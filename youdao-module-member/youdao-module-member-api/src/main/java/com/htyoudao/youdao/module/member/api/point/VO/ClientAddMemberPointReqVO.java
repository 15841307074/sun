package com.htyoudao.youdao.module.member.api.point.VO;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-23
 */
public class ClientAddMemberPointReqVO implements Serializable {
    private static final long serialVersionUID = 8103888194672130530L;

    private BigDecimal payAmount;

    private BigDecimal orderAmount;

    private String orderSn;

    private Long memberId;

    private Long projectOwnerShip;

    private Long businessId;

    public Long getProjectOwnerShip() {
        return projectOwnerShip;
    }

    public ClientAddMemberPointReqVO setProjectOwnerShip(Long projectOwnerShip) {
        this.projectOwnerShip = projectOwnerShip;
        return this;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public ClientAddMemberPointReqVO setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
        return this;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public ClientAddMemberPointReqVO setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
        return this;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public ClientAddMemberPointReqVO setOrderSn(String orderSn) {
        this.orderSn = orderSn;
        return this;
    }

    public Long getMemberId() {
        return memberId;
    }

    public ClientAddMemberPointReqVO setMemberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public ClientAddMemberPointReqVO setBusinessId(Long businessId) {
        this.businessId = businessId;
        return this;
    }
}

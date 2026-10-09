package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberHistoryOrderRebuildReqVO {

    @NotNull(message = "businessId 不能为空")
    private Long businessId;

    /**
     * 是否同时重建历史订单总数
     */
    private Boolean resetTotalOrderNum = Boolean.FALSE;
}


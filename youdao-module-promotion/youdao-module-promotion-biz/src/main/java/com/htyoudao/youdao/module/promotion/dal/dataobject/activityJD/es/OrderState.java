package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

// OrderState 枚举（订单状态）
@Getter
@AllArgsConstructor
public enum OrderState {
    IN_PROGRESS(1), COMPLETED(2), CANCELLED(3), FAILED(4);
    private final int state;
}

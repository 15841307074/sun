package com.htyoudao.youdao.module.order.controller.app.order.VO;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 下单响应体
 * </p>
 *
 * @author zhangjihe
 * @since 2025-02-13
 */
@Builder
@Data
public class SubmitResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4344301082770797010L;

    private String orderSn;

    private String pickUpNum;

    private String createTime;
}

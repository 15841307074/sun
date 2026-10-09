package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassName MaxwellVo
 * @Description MaxwellVo
 * @Author wangwei
 * @Date 2021/8/18 9:03
 * @Version 1.0
 **/
@Data
@Builder
public class NotifyOrderDTO implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 消息类型， INSERT, DELETE
     */
    private String type;

    /**
     * 消息体
     */
    private String orderSn;
}

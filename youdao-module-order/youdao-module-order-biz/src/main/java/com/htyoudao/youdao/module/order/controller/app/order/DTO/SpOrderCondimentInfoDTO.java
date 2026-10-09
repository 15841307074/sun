package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 订单创建，小料信息
 * </p>
 *
 * @author zhangjihe
 * @since 2024-06-16
 */
@Data
public class SpOrderCondimentInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4088968136927759183L;
    private String condimentId;

    private Integer condimentNumber;
}

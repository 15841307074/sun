package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 */
@Data
public class OrderCountByOrderTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8039029615819273765L;

    private Integer orderType;

    private Integer orderNum;
}

package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpBzFlavorWithUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5021757542575640558L;
    private String flavorName;

    private String flavorValue;
}

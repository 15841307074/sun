package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SingleDetailsDTO implements Serializable {
    private static final long serialVersionUID = -7704302310057209491L;

    String singleName;

    Integer copies;
}

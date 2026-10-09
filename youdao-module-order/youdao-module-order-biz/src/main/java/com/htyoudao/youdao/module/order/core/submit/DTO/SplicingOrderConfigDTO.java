package com.htyoudao.youdao.module.order.core.submit.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SplicingOrderConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9156307899060355536L;

    private String bannerUrl;

    private String discount;
}

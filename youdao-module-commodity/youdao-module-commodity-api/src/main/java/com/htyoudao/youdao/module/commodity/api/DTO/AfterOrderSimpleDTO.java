package com.htyoudao.youdao.module.commodity.api.DTO;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AfterOrderSimpleDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3304523208396693438L;

    private Long afterId;

    private Long commodityId;

    private String commodityName;

    private String thumbnailUrl;
}

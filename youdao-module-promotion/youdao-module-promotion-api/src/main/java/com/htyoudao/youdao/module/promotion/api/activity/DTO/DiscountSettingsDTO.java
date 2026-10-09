package com.htyoudao.youdao.module.promotion.api.activity.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiscountSettingsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4300021895791323466L;



    @Schema(description = "满多少元或者多少件")
    private String mPriceOrCount;
    @Schema(description = "减多少元或者打多少折")
    private String jPriceOrSale;

}

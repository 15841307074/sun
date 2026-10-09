package com.htyoudao.youdao.module.promotion.api.seckill.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ActivityCommodityDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8074372004709925980L;


    private Long commodityId;
    private String commodityName;

    private List<ActivityCommodityPriceDTO> commodityPriceList = new ArrayList<>();

    @Data
    public static class ActivityCommodityPriceDTO implements Serializable{

        @Serial
        private static final long serialVersionUID = 8074372004709925980L;
        private Long skuId;
        private BigDecimal commodityPrice;
    }

}

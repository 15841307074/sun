package com.htyoudao.youdao.module.commodity.dal.dto.inventory;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 获取库存变动金额
 */
@Data
public class FlowAmountDTO {
    /**
     * 变动类型 StockChangeEnum
     */
    private List<String> changeTypes;

    private Long storeId;

    private Date startTime;

    private Date endTime;
}
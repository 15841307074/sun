package com.htyoudao.youdao.module.promotion.dal.dataobject.market;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 短信营销和门店关系
 * </p>
 *
 * @author dht
 * @since 2025-04-18
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_market_store")
public class SmsMarketStoreDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短信营销表id
     */
    private Long smsMarketId;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;
}

package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketStoreDO;

import java.util.List;

/**
 * @author dht
 */
public interface SmsMarketStoreService extends IService<SmsMarketStoreDO> {
    /**
     *  批量插入
     * @param smsMarketStores smsMarketStores
     */
    void insertBatch(List<SmsMarketStoreDO> smsMarketStores);
}

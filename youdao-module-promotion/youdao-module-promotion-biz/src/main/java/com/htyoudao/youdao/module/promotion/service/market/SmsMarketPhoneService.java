package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketPhoneDO;

import java.util.List;

/**
 * @author dht
 */
public interface SmsMarketPhoneService extends IService<SmsMarketPhoneDO> {

    /**
     *  批量插入
     * @param list list
     */
    void insertBatch(List<SmsMarketPhoneDO> list);
}

package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDeptDO;

import java.util.List;

/**
 * @author dht
 */
public interface SmsMarketDeptService extends IService<SmsMarketDeptDO> {
    /**
     * 批量插入
     * @param smsMarketDepts smsMarketDepts
     */
    void insertBatch(List<SmsMarketDeptDO> smsMarketDepts);
}

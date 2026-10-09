package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketStoreMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class SmsMarketStoreServiceImpl extends ServiceImpl<SmsMarketStoreMapper, SmsMarketStoreDO> implements SmsMarketStoreService{

    @Resource
     private SmsMarketStoreMapper smsMarketStoreMapper;

    @Override
    public void insertBatch(List<SmsMarketStoreDO> smsMarketStores) {
        smsMarketStoreMapper.insertBatchSomeColumn(smsMarketStores);
    }
}

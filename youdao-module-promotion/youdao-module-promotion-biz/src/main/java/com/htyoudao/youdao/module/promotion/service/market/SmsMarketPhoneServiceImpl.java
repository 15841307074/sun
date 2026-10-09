package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketPhoneDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketPhoneMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class SmsMarketPhoneServiceImpl extends ServiceImpl<SmsMarketPhoneMapper, SmsMarketPhoneDO> implements SmsMarketPhoneService{

    @Resource
    private SmsMarketPhoneMapper smsMarketPhoneMapper;

    @Override
    public void insertBatch(List<SmsMarketPhoneDO> list) {
        smsMarketPhoneMapper.insertBatchSomeColumn(list);
    }
}

package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDeptDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketDeptMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class SmsMarketDeptServiceImpl extends ServiceImpl<SmsMarketDeptMapper, SmsMarketDeptDO> implements SmsMarketDeptService{

    @Resource
     private SmsMarketDeptMapper smsMarketDeptMapper;

    @Override
    public void insertBatch(List<SmsMarketDeptDO> smsMarketDepts) {
        smsMarketDeptMapper.insertBatchSomeColumn(smsMarketDepts);
    }
}

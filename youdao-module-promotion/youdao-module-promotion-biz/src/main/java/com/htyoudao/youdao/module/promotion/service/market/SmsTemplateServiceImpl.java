package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsTemplateDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsTemplateMapper;
import org.springframework.stereotype.Service;

/**
 * @author dht
 */
@Service
public class SmsTemplateServiceImpl extends ServiceImpl<SmsTemplateMapper, SmsTemplateDO> implements SmsTemplateService{
}

package com.htyoudao.youdao.module.promotion.service.exchangecommodity;

import com.htyoudao.youdao.module.promotion.dal.mysql.exchangecommodity.ExchangeCommodityMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * 兑换券下单时必选商品 Service 实现类
 *
 * @author lzw
 */
@Service
@Validated
public class ExchangeCommodityServiceImpl implements ExchangeCommodityService {

    @Resource
    private ExchangeCommodityMapper exchangeCommodityMapper;

}
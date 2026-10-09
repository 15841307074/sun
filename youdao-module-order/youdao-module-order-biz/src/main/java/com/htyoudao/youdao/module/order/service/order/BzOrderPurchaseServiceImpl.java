package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderPurchaseMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-08
 */
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class BzOrderPurchaseServiceImpl extends ServiceImpl<BzOrderPurchaseMapper, BzOrderPurchaseDO> implements BzOrderPurchaseService {
    @Override
    public List<BzOrderPurchaseDO> getOrderPurchaseList(List<String> orderSns, LocalDateTime[] createTimes) {
        return baseMapper.selectList(orderSns, createTimes);
    }
}

package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderCondimentsDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderCondimentsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单小料
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-08
 */
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class BzOrderCondimentsServiceImpl extends ServiceImpl<BzOrderCondimentsMapper, BzOrderCondimentsDO> implements BzOrderCondimentsService {
    @Resource
    private BzOrderCondimentsMapper bzOrderCondancesMapper;

    @Override
    public List<BzOrderCondimentsDO> getOrderCondimentsList(List<String> orderSns, LocalDateTime[] createTimes) {
        return bzOrderCondancesMapper.selectList(orderSns, createTimes);
    }
}

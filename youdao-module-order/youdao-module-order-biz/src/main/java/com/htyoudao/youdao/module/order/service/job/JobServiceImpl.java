package com.htyoudao.youdao.module.order.service.job;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import com.htyoudao.youdao.module.order.service.report.ReportSerive;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

    @Resource
    private BzOrderService bzOrderService;
    @Resource
    private ReportSerive reportSerive;

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public void updateCancelOrderTask() {

        List<BzOrderDO> orderList = bzOrderService.list(
                new LambdaQueryWrapper<BzOrderDO>()
                        .between(BzOrderDO::getCreateTime, LocalDateTime.now().minusMinutes(20), LocalDateTime.now())
                        .eq(BzOrderDO::getOrderState, OrderStateEnum.UNPAID.getCode())
        );

        for (BzOrderDO order : orderList) {
            if (ObjectUtils.isEmpty(order.getCreateTime())) {
                continue;
            }

            //  超过15分钟还没有取消订单，则取消订单
            if (order.getCreateTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                order.setOrderState(OrderStateEnum.CANCELED.getCode());
                bzOrderService.updateOrderState(order);
            }
        }
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void cancelTodayWaitingAcceptErrandOrders() {
        bzOrderService.cancelTodayWaitingAcceptErrandOrders();
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void completeTodayAcceptedErrandOrders() {
        bzOrderService.completeTodayAcceptedErrandOrders();
    }

    @Override
    public void reportTotalWeek() {
        reportSerive.reportTotalWeek();
    }

    @Override
    public void reportTotalMonth() {
        reportSerive.reportTotalMonth();
    }

    @Override
    public void reportWarehouseWeek() {
        reportSerive.reportWarehouseWeek();
    }

    @Override
    public void reportWarehouseMonth() {
        reportSerive.reportWarehouseMonth();
    }

    @Override
    public void reportCommodityWeek() {
        reportSerive.reportCommodityWeek();
    }

    @Override
    public void reportCommodityMonth() {
        reportSerive.reportCommodityMonth();
    }

    @Override
    public void reportStoreSalesWeek() {
        reportSerive.reportStoreSalesWeek();
    }

    @Override
    public void reportStoreSalesMonth() {
        reportSerive.reportStoreSalesMonth();
    }

    @Override
    public void reportStasticsWeek() {
        reportSerive.reportStasticsWeek();
    }

    @Override
    public void reportStasticsMonth() {
        reportSerive.reportStasticsMonth();
    }

    @Override
    public void reportAppSalesnumWeek() {
        reportSerive.reportAppSalesnumWeek();
    }

    @Override
    public void reportAppSalesnumMonth() {
        reportSerive.reportAppSalesnumMonth();
    }

    @Override
    public void reportBuyProportionMonth() {
        reportSerive.reportBuyProportionMonth();
    }

    @Override
    public void reportBuyAmountMonth() {
        reportSerive.reportBuyAmountMonth();
    }
}

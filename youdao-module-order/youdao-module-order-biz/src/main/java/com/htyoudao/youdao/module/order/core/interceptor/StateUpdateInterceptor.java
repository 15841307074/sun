package com.htyoudao.youdao.module.order.core.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderLogDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderLogMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderMapper;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 订单状态变更统一处理
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-30
 */
@Slf4j
@Component(value = "StateUpdateInterceptor")
public class StateUpdateInterceptor implements InnerInterceptor {

    @Autowired
    private BzOrderService bzOrderService;
    @Autowired
    private BzOrderLogMapper logMapper;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        try {
            //判断是否为订单表的更新操作
            if (!ms.getId().contains("BzOrderMapper.updateOrderState")) return;

            //获取更新的实体对象
            Object entity = ((Map<?, ?>) parameter).get("et");
            if (!(entity instanceof BzOrderDO newOrder)) return;

            //查询旧数据
            BzOrderDO oldOrder = bzOrderService.getOne(
                    new LambdaQueryWrapper<BzOrderDO>().eq(BzOrderDO::getOrderSn, newOrder.getOrderSn())
                            .eq(BzOrderDO::getCreateTime, newOrder.getCreateTime())
            );
            log.info("订单[{}]状态变更：{} -> {}", oldOrder.getOrderSn(), oldOrder.getOrderState(), newOrder.getOrderState());

            //检查状态字段是否变更
            if (oldOrder.getOrderState().equals(newOrder.getOrderState())) return;

            //前置判断：例如状态流转是否合法
            if (!isStatusValid(oldOrder.getOrderState(), newOrder.getOrderState())) {
                throw new RuntimeException("状态变更非法");
            }

            //记录状态变更日志
            BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
            bzOrderLogDO.setOrderSn(oldOrder.getOrderSn());
            bzOrderLogDO.setOrderStateLog(this.getOrderLogState(oldOrder, newOrder));
            bzOrderLogDO.setLogContent(this.getOrderLogContent(oldOrder, newOrder));
            bzOrderLogDO.setLogTime(new Date());
            logMapper.insert(bzOrderLogDO);
        } catch (Exception e) {
            log.error("状态更新拦截器异常", e);
            throw e;
        }
    }

    private boolean isStatusValid(Integer oldState, Integer newState) {
        // TODO 状态的前置判断，比如取消的订单就不能做其他操作了 等等 待完善
        return true;
    }

    private Integer getOrderLogState(BzOrderDO oldOrder, BzOrderDO newOrder) {
        return newOrder.getOrderState();
    }

    private String getOrderLogContent(BzOrderDO oldOrder, BzOrderDO newOrder) {
        return OrderStateEnum.getMessageByCode(newOrder.getOrderState());
    }
}

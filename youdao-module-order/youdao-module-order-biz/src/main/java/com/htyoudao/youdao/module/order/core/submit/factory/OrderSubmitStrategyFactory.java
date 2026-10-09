package com.htyoudao.youdao.module.order.core.submit.factory;

import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.core.submit.strategy.IOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单提交来源策略工厂
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Component
public class OrderSubmitStrategyFactory<T extends SubmitReqVO> {
    private final Map<OrderSourceEnum, IOrderSubmitStrategy<T>> strategyMap;

    @Autowired
    public OrderSubmitStrategyFactory(List<IOrderSubmitStrategy<T>> strategies) {
        //搜集所有策略
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(IOrderSubmitStrategy::getSource, Function.identity()));
    }

    public IOrderSubmitStrategy<T> getStrategy(OrderSourceEnum source) {
        IOrderSubmitStrategy<T> strategy = strategyMap.get(source);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的订单来源：" + source);
        }
        return strategy;
    }
}
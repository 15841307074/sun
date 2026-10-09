package com.alibaba.csp.sentinel.dashboard.controller.base;

import lombok.extern.log4j.Log4j;

import java.util.concurrent.TimeUnit;


/**
 * Nacos持久化通用处理类
 *
 * @author lqman
 */
@Log4j
public class BaseRuleController {
    /**
     * 延迟一下
     * 解释：列表加载数据的时候，Nacos持久化还没做完，导致加载数据不对
     */
    public static void delayTime() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("-------------睡100毫秒-----------");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}

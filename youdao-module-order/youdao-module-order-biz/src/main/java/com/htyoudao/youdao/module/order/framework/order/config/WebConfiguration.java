package com.htyoudao.youdao.module.order.framework.order.config;

import com.htyoudao.youdao.module.order.util.HashedWheelTimerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-07
 */
@Configuration
public class WebConfiguration {

    @Bean
    public HashedWheelTimerProxy HashedWheelTimerProxy() {
        HashedWheelTimerProxy wheelTimer =  new HashedWheelTimerProxy();
        wheelTimer.getWheelTimer().start();
        return wheelTimer;
    }

}

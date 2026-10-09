package com.htyoudao.youdao.module.order.framework.order.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.htyoudao.youdao.framework.mybatis.core.util.MyBatisUtils;
import com.htyoudao.youdao.module.order.core.interceptor.StateUpdateInterceptor;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */
@Configuration
public class MyBatisPlusConfiguration {

    private StateUpdateInterceptor stateUpdateInterceptor;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, stateUpdateInterceptor, 0);
        return interceptor;
    }

    @Autowired
    public void setStateUpdateInterceptor(StateUpdateInterceptor stateUpdateInterceptor) {
        this.stateUpdateInterceptor = stateUpdateInterceptor;
    }
}

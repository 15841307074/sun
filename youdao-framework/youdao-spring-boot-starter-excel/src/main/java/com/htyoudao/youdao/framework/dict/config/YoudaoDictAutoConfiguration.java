package com.htyoudao.youdao.framework.dict.config;

import com.htyoudao.youdao.framework.dict.core.DictFrameworkUtils;
import com.htyoudao.youdao.module.system.api.dict.DictDataApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class YoudaoDictAutoConfiguration {

    @DubboReference
    private DictDataApi dictDataApi;

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils() {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}

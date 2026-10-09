package com.htyoudao.youdao.framework.sharding.core.spi;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereURLLoader;

import java.util.Properties;

/**
 * 实现SPI，读取远程的nacos配置
 *
 * @author liuzhaowang
 */
public class CustomNacosURLLoader implements ShardingSphereURLLoader {
   /**
     * 定义jdbc:shardingsphere:后的类型为nacos:
     */
    private static final String NACOS_TYPE = "nacos:";

    /**
     * @param configurationSubject configuration dataId
     * @param queryProps           url参数，已经解析成为Properties
     * @return nacos配置
     */
    @Override
    @SneakyThrows
    public String load(String configurationSubject, Properties queryProps) {
        ConfigService configService = NacosFactory.createConfigService(queryProps);
        //获取nacos配置
        String config = configService.getConfig(configurationSubject, queryProps.getProperty(Constants.GROUP, Constants.DEFAULT_GROUP), 2000);
        Preconditions.checkArgument(config != null, "Nacos config [" + configurationSubject + "] is Empty.");
        return config;
    }

    @Override
    public Object getType() {
        return NACOS_TYPE;
    }
}

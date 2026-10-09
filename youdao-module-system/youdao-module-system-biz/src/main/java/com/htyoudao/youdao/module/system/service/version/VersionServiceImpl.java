package com.htyoudao.youdao.module.system.service.version;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.system.controller.app.version.vo.VersionDataRequestVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RefreshScope
@Service
public class VersionServiceImpl implements VersionService{

    @Value("${versionCode}")
    String versionCode;

//    @Value("${bossVersion}")
//    String bossVersion;

    @Value("${version}")
    String version;

    /**
     * 支付宝配置兼容原有 switch0，便于未传端类型的旧客户端平滑升级。
     */
    @Value("${switch.alipay}")
    Integer alipaySwitch;

    /**
     * 新支付宝
     */
    @Value("${switch.alipay1}")
    Integer alipaySwitch1;

    @Value("${switch.wechat}")
    Integer wechatSwitch;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Integer getSwitch0(String clientType) {
        if ("wechat".equals(clientType)) {
            return wechatSwitch;
        }
        if ("alipay1".equals(clientType)) {
            return alipaySwitch1;
        }
        return alipaySwitch;
    }

    @Override
    public VersionDataRequestVo selectActive() {
        VersionDataRequestVo versionDataRequestVo = new VersionDataRequestVo();
        versionDataRequestVo.setVersionCode(versionCode);
        versionDataRequestVo.setName("0090");
        return versionDataRequestVo;

    }
}

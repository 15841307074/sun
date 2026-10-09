package com.htyoudao.youdao.module.system.service.applet.impl;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Getter
@RefreshScope
@Component
public class AppletPageConfig {

    @Value("${applet.page.count:6}")
    private Integer appletPageCount;

}

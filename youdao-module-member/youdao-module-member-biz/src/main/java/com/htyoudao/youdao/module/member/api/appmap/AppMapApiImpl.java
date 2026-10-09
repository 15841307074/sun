package com.htyoudao.youdao.module.member.api.appmap;

import com.htyoudao.youdao.module.member.service.appmap.AppMapService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class AppMapApiImpl implements AppMapApi{

    @Resource
    private AppMapService appMapService;

    @Override
    public String geocoderV3(String lon, String lat) {
        return appMapService.geocoderV3(lon, lat);
    }
}

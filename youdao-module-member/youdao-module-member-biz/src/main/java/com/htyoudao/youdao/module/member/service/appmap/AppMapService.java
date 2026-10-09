package com.htyoudao.youdao.module.member.service.appmap;

public interface AppMapService {

    /**
     * 获取经纬度对应的地址信息
     *
     * @param lon 经度
     * @param lat 纬度
     * @return 地址信息
     */
    String geocoderV3(String lon, String lat);

    /**
     * 根据关键词获取地点输入提示
     *
     * @param keyword 搜索关键词
     * @param region  搜索地区，腾讯地图接口使用
     * @return 腾讯地图提示词 data 结构
     */
    String suggestionV3(String keyword, String region);
}

package com.htyoudao.youdao.module.promotion.controller.app.appletnotice;

import lombok.Data;

/**
 * @author dht
 */
@Data
public class AppletNoticePushVO {
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 模板编码
     */
    private Integer templateCode;

    /**
     * 模板类别
     */
    private Integer templateType;

    private String projectOwnerShip;

    private String openId;

    private Long businessId;

}
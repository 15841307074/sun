package com.htyoudao.youdao.module.member.api.wx.VO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author dht
 */
@Data
public class AppletNoticePushVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -6791910551587179108L;
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
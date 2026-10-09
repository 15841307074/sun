package com.htyoudao.youdao.module.system.controller.app.wxstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author DHT
 */
@Data
@Schema(description = "微信门店信息")
public class AppWeChatStoreRespVO {

    /** 主键 */
    @Schema(description = "id")
    private Long id;

    /** 门店id */
    @Schema(description = "门店id")
    private Long storeId;

    /** 企微二维码 */
    @Schema(description = "企微二维码")
    private String qrCode;

    /**
     * 门店经度
     */
    @Schema(description = "门店经度")
    private double longitude;

    /**
     * 门店维度
     */
    @Schema(description = "门店维度")
    private double latitude;

    /**
     * 店铺距离
     */
    @Schema(description = "店铺距离")
    private double distance;
}

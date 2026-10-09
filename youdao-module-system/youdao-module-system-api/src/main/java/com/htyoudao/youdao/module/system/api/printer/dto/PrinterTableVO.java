package com.htyoudao.youdao.module.system.api.printer.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PrinterTableVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -953516147169052388L;


    /**
     * 打印机表主键
     */
    private Long printerTableId;

    /**
     * 门店 ID
     */

    private Long storeId;

    /**
     * 打印机名称
     */
    private String printerName;

    /**
     * 打印机类型（1 USB，2 云打印机）
     */
    private Integer printerType;

    /**
     * 打印机品牌
     */
    private String printerBrand;

    /**
     * USB 端口
     */
    private String usbPort;

    /**
     * 纸张宽度
     */
    private Integer paperWidth;

    /**
     * 打印机序列号
     */
    private String printerSerialNumber;

    /**
     * 打印机设置 IDs
     */
    private String printerSettingIds;

    /**
     * 打印机 key
     */
    private String printerSerialKey;


    private String bindDeviceNumber;

    /**
     * 打印位置
     */
    private String printerLocation;

    /**
     * 打印状态
     */
    private Integer status;



}

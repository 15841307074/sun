package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等对象 printer_interface
 * 
 * @author ruoyi
 * @date 2024-03-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PrinterInterface extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** id */
    private Long printerInterfaceId;

    /** 打印机品牌ID */
    private Long printerBrandId;

    /** 接口功能（1 添加打印机 2 小票机打印订单 3 标签机打印订单 4 删除打印机 5 修改打印机 6 清空打印机待打印订单 7查询订单状态 8 查询打印机订单数 9查询打印机状态 10查询打印机信息） */
    private Long printerInterfaceAbility;

    /** 接口请求方式 */
    private String printerInterfaceMethod;

    /** 接口值 */
    private String printerInterfaceValue;
}

package com.htyoudao.youdao.module.system.dal.dataobject.printer;


import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.io.Serializable;

/**
 * 打印机品牌，存储打印机品牌信息对象 printer_brand_table
 * 
 * @author Qizhongnan
 * @date 2024-03-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PrinterBrandTable extends SysBaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** id */
    private Long printerBrandId;

    /** 品牌名称 */
    private String printerBrandName;

    /** 打印机型号 */
    private String printerBrandType;

    /** 打印机所属账号 */
    private String printerBrandUser;

    /** 打印机所属账号的密码 */
    private String printerBrandUserkey;

    /** 打印请求头 */
    private String contentType;

    /** 请求接口 */
    private String printerBrandInterface;
}

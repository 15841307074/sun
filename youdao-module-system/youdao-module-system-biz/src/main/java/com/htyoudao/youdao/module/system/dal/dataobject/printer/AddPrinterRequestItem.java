package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import lombok.Data;

/**
 * 添加打印机请求项
 *
 * @author RabyGao
 * @date Aug 7, 2019
 */
@Data
public class AddPrinterRequestItem {

    /**
     * 打印机编号
     */
    private String sn;
    /**
     * 打印机名称
     */
    private String name;
}

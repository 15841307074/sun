package com.htyoudao.youdao.module.system.dal.dataobject.printer;

/**
 * 打印机请求参数
 *
 * @author RabyGao
 * @date Aug 7, 2019
 */
public class PrinterRequest extends RestRequest {

    /**
     * 打印机编号
     */
    private String sn;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}

package com.htyoudao.youdao.module.system.dal.dataobject.printer;

/**
 * 删除打印机请求参数
 *
 * @author RabyGao
 * @date Aug 7, 2019
 */
public class DelPrinterRequest extends RestRequest {

    /**
     * 打印机编号集合
     */
    private String[] snlist;

    public String[] getSnlist() {
        return snlist;
    }

    public void setSnlist(String[] snlist) {
        this.snlist = snlist;
    }
}

package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import java.util.List;

/**
 * @author LylJavas
 * @create 2021/4/14 18:01
 * @description 批量打印机请求参数
 */
public class PrintersRequest extends RestRequest {

    /**
     * 打印机编号列表
     */
    private List<String> snlist;

    public List<String> getSnlist() {
        return snlist;
    }

    public void setSnlist(List<String> snlist) {
        this.snlist = snlist;
    }
}

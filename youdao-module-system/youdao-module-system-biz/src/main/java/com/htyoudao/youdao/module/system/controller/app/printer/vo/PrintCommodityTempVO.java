package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import lombok.Data;

/**
 * 该类代表整个打印配置，包含了不同角色（如店铺、会员、厨房、配送）的打印信息。
 */
@Data
public class PrintCommodityTempVO {
    /**
     * 模板
     */
    private  String printCommodityTem;
    /**
     * 标签
     */
    private  PrintCommodityConfigTagVO productInformation;
    /**
     * 模板类型
     */
    private  int templateType;

    /**
     * 打印数量
     */
    private  int printNum;
}
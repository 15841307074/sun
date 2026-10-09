package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 该类代表整个打印配置，包含了不同角色（如店铺、会员、厨房、配送）的打印信息。
 */
@Data
public class PrintCommodityVO {
    /**
     * 标签
     */
    private  PrintCommodityConfigTagVO productInformation;
    /**
     * 模板类型
     */
    private  int templateType;
}
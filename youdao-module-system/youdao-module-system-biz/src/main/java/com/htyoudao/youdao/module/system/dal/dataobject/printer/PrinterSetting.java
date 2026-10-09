package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PrinterSetting implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /** 打印机设置主键 */
    @TableId
    private Long printerSettingId;



    /** 单据类型（1 商家联 2 后厨联 3 顾客联  4 配送联） */
    private Integer documentType;

    /** 打印份数 */
    private Integer printCopies;

    /** 单据尺寸 */
    private String documentSize;

    /** 切纸方式（1 整单，2 按类分单，3 一菜一切，4 一份一切） */
    private Integer paperCuttingMethod;

    /** 可打印商品 */
    private String printableProducts;

    /** 可打印渠道 （1 全部渠道 不选全部时才能选择其他的） 	（2 店内点餐） 	（3 店内点餐-堂食） 	（4店内点餐-打包） 	（5 店内点餐-送餐） 	（6 扫码点餐） 	（7 扫码点餐-堂食） 	（8 扫码点餐-打包） 	（9 自营外卖） 	（10 自营外卖-送餐） 	（11 自营外卖-自取） 	（12 自营外卖-预约堂食） 	（13 美团外卖） 	（14 饿了么外卖） 	（15 快速付款） */
    private String printableChannel;

    /**
     * 打印渠道选择
     */
    @TableField(exist = false)
    List<Integer> printableChannelS;

    private Long projectOwnerShip;

}

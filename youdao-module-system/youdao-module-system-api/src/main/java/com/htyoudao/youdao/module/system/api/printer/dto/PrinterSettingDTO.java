package com.htyoudao.youdao.module.system.api.printer.dto;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 打印机设置对象 printer_setting
 *
 * @author Qizhongnan
 * @date 2024-03-26
 */
@Data
public class PrinterSettingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -953518147169052378L;


    /**
     * 打印机设置主键
     */
    private Long printerSettingId;

    /**
     * 单据类型（1 结账单，2 出品单，3 标签单）
     */
    private Integer documentType;

    /**
     * 打印份数
     */
    private Integer printCopies;

    /**
     * 单据尺寸
     */
    private String documentSize;

    /**
     * 切纸方式（1 整单，2 按类分单，3 一菜一切，4 一份一切）
     */
    private Integer paperCuttingMethod;

    /**
     * 可打印商品
     */
    private String printableProducts;

    /**
     * 可打印渠道 （1 全部渠道 不选全部时才能选择其他的） 	（2 店内点餐） 	（3 店内点餐-堂食） 	（4店内点餐-打包） 	（5 店内点餐-送餐） 	（6 扫码点餐） 	（7 扫码点餐-堂食） 	（8 扫码点餐-打包） 	（9 自营外卖） 	（10 自营外卖-送餐） 	（11 自营外卖-自取） 	（12 自营外卖-预约堂食） 	（13 美团外卖） 	（14 饿了么外卖） 	（15 快速付款）
     */
    private String printableChannel;
    /**
     * 打印渠道选择
     */
    List<Integer> printableChannelS;

    public void setPrinterSettingId(Long printerSettingId) {
        this.printerSettingId = printerSettingId;
    }

    public Long getPrinterSettingId() {
        return printerSettingId;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

    public Integer getDocumentType() {
        return documentType;
    }

    public void setPrintCopies(Integer printCopies) {
        this.printCopies = printCopies;
    }

    public Integer getPrintCopies() {
        return printCopies;
    }

    public void setDocumentSize(String documentSize) {
        this.documentSize = documentSize;
    }

    public String getDocumentSize() {
        return documentSize;
    }

    public void setPaperCuttingMethod(Integer paperCuttingMethod) {
        this.paperCuttingMethod = paperCuttingMethod;
    }

    public Integer getPaperCuttingMethod() {
        return paperCuttingMethod;
    }

    public void setPrintableProducts(String printableProducts) {
        this.printableProducts = printableProducts;
    }

    public String getPrintableProducts() {
        return printableProducts;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("printerSettingId", getPrinterSettingId())
                .append("documentType", getDocumentType())
                .append("printCopies", getPrintCopies())
                .append("documentSize", getDocumentSize())
                .append("paperCuttingMethod", getPaperCuttingMethod())
                .append("printableProducts", getPrintableProducts())
                .append("printableChannel", getPrintableChannel())
                .toString();
    }
}

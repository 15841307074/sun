package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsUpdateErrorDTO {


    @ExcelProperty(index = 0,value = "兑换编码")
    @ColumnWidth(20)
    private String pointsLogId;

    @ExcelProperty(index = 1, value = "用户唯一标识")
    @ColumnWidth(20)
    private String memberId;

    @ExcelProperty(index = 2, value = "商品类型")
    @ColumnWidth(15)
    private String productTypeName;

    @ExcelProperty(index = 3, value = "兑换编号")
    @ColumnWidth(25)
    private String logCode;

    @ExcelProperty(index = 4, value = "积分商品")
    @ColumnWidth(20)
    private String productName;

    @ExcelProperty(index = 5, value = "兑换时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;

    @ExcelProperty(index = 6, value = "积分商品价格")
    @ColumnWidth(15)
    private Long productPrice;

    @ExcelProperty(index = 7, value = "会员昵称")
    @ColumnWidth(20)
    private String memberNickName;

    @ExcelProperty(index = 8, value = "用户手机号")
    @ColumnWidth(15)
    private String memberMobile;

    @ExcelProperty(index = 9, value = "收货地址")
    @ColumnWidth(30)
    private String receiveAddress;

    @ExcelProperty(index = 10, value = "快递单号")
    @ColumnWidth(20)
    private String trackingNumber;

    @ExcelProperty(index = 11, value = "快递公司")
    @ColumnWidth(15)
    private String expressCompany;

    @ExcelProperty(index = 12, value = "失败原因")
    @ColumnWidth(50)
    private String errorMessage;
}

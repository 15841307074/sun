package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLogExportRespVO {


    @ExcelProperty("兑换编码")
    @Schema(description = "兑换编码")
    private String pointsLogId;

    @ExcelProperty("用户唯一标识")
    @Schema(name = "memberId", description = "会员ID")
    private String memberId;

    @ExcelProperty("商品类型")
    @Schema(name = "productTypeName", description = "商品类型")
    private String productTypeName;

    @ExcelProperty("兑换编号")
    @Schema(name = "logCode", description = "兑换编号")
    private String logCode;

    @ExcelProperty("积分商品")
    @Schema(name = "productName", description = "积分商品")
    private String productName;

    @ExcelProperty("兑换时间")
    private LocalDateTime createTime;


    @ExcelProperty("积分商品价格")
    @Schema(description = "积分商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;


    @ExcelProperty("会员昵称")
    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    @ExcelProperty("用户手机号")
    @Schema(description = "用户手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;

    @ExcelProperty("收货地址")
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiveAddress;

    @ExcelProperty("快递单号")
    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;


    @ExcelProperty("快递公司")
    @Schema(description = "快递公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;



}

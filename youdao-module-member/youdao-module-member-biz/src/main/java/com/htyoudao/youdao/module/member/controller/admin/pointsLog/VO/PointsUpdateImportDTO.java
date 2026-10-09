package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
@ExcelIgnoreUnannotated
public class PointsUpdateImportDTO {

    @ExcelProperty(index = 0 ,value = "兑换编码")
    @Schema(description = "兑换编码")
    private String pointsLogId;

    @ExcelProperty(index = 1, value = "用户唯一标识")
    @Schema(name = "memberId", description = "会员ID")
    private String memberId;

    @ExcelProperty(index = 2, value = "商品类型")
    @Schema(name = "productTypeName", description = "商品类型")
    private String productTypeName;

    @ExcelProperty(index = 3, value = "兑换编号")
    @Schema(name = "logCode", description = "兑换编号")
//    @NotBlank(message = "兑换编号不能为空")
    private String logCode;

    @ExcelProperty(index = 4, value = "积分商品")
    @Schema(name = "productName", description = "积分商品")
    private String productName;

    @ExcelProperty(index = 5, value = "兑换时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ExcelProperty(index = 6, value = "积分商品价格")
    @Schema(description = "积分商品价格")
    private Long productPrice;

    @ExcelProperty(index = 7, value = "会员昵称")
    @Schema(description = "会员昵称")
    private String memberNickName;

    @ExcelProperty(index = 8, value = "用户手机号")
    @Schema(description = "用户手机号")
    private String memberMobile;

    @ExcelProperty(index = 9, value = "收货地址")
    @Schema(description = "收货地址")
    private String receiveAddress;

    @ExcelProperty(index = 10, value = "快递单号")
    @Schema(description = "快递单号")
    private String trackingNumber;

    @ExcelProperty(index = 11, value = "快递公司")
    @Schema(description = "快递公司")
    private String expressCompany;

}

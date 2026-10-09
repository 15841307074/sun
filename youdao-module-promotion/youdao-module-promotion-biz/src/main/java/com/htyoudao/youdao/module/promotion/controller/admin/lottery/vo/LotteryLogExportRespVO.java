package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class LotteryLogExportRespVO {


    @ExcelProperty("用户唯一标识")
    @Schema(name = "memberId", description = "会员ID")
    private String memberId;


    @ExcelProperty("会员名称")
    @Schema(name = "memberName", description = "会员名称")
    private String memberName;

    /** 会员手机号 */
    @ExcelProperty("联系方式")
    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;

//    /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
//    @ExcelProperty("奖品类型")
//    @Schema(name = "prizeType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
//    private Integer prizeType;


    @ExcelProperty("奖品类型")
    @Schema(name = "prizeTypeName", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private String prizeTypeName;

    /** 奖品名称 */
    @ExcelProperty("奖品内容")
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;

    @ExcelProperty("抽奖门店")
    @Schema(name = "storeName", description = "抽奖门店")
    private String storeName;

    @ExcelProperty("抽奖时间")
    private LocalDateTime createTime;

    @ExcelProperty("消耗积分")
    @Schema(name = "price", description = "消耗积分")
    private Integer price;


    @ExcelProperty("红包状态")
    @Schema(name = "claimName", description = "红包状态")
    private String claimName;

    @ExcelProperty("收件人")
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;


    @ExcelProperty("联系方式")
    @Schema(name = "receiveMobile", description = "联系方式")
    private String receiveMobile;


    @ExcelProperty("收货地址")
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;

    @ExcelProperty("快递单号")
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;






//    public String getPrizeName() {
//        if (this.prizeTypeName == null) {
//            this.prizeTypeName = convertPrizeTypeToName(this.prizeType);
//        }
//        return this.prizeTypeName;
//    }
//
//    private String convertPrizeTypeToName(Integer prizeType) {
//        if (prizeType == null) {
//            return "";
//        }
//        switch (prizeType) {
//            case 1: return "优惠券";
//            case 2: return "积分";
//            case 3: return "实物";
//            case 4: return "无奖品";
//            default: return "";
//        }
//    }

}

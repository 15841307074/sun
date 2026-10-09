package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券包 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CouponPackageRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26688")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "优惠券包名", example = "赵六")
    @ExcelProperty("优惠券包名")
    private String packageName;

    @Schema(description = "发放总量")
    @ExcelProperty("发放总量")
    private Integer totalNum;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建人", example = "李四")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改人", example = "张三")
    @ExcelProperty("修改人")
    private String updater;

    @Schema(description = "删除标识")
    @ExcelProperty("删除标识")
    private Boolean deleted;

    @Schema(description = "项目标识")
    @ExcelProperty("项目标识")
    private Long businessId;

    @Schema(description = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友")
    private Integer isShare;

    @Schema(description = "剩余数量")
    @ExcelProperty("剩余数量")
    private Integer packageNum;

    @Schema(description = "已领取数目")
    @ExcelProperty("已领取数目")
    private Integer receivedNum;

    @Schema(description = "每日领取限制")
    @ExcelProperty("每日领取限制")
    private Integer dayLimit;

    @Schema(description = "显示时间")
    @ExcelProperty("显示时间")
    private String showTime;

    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("每人限领数目")
    private Integer limitNum;

    @Schema(description = "领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）")
    private Integer userRestrictions;

    @Schema(description = "人群id")
    private String crowdId;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    @ExcelProperty("优惠券图片")
    private String packageImageUrl;

    @Schema(description = "是否上架(0-否,1-是)")
    @ExcelProperty("是否上架(0-否,1-是)")
    private Integer isGround;

    @Schema(description = "微信分享短连接", example = "https://www.iocoder.cn")
    @ExcelProperty("微信分享短连接")
    private String miniSortUrl;

    @Schema(description = "H5分享短连接", example = "https://www.iocoder.cn")
    @ExcelProperty("H5分享短连接")
    private String h5SortUrl;

    @Schema(description = "门店领取限制")
    private Integer storeLimitNum;


    @Schema(description = "0 普通券包 1 周周惠券包")
    private Integer packageType;

    @Schema(description = "0不需要进社群 1需要进社群", example = "0")
    private Integer communityFlag;

    private  List<GoodCouponPackageRespVO> goodCouponPackageRespVOS = new ArrayList<>();

    @Schema(description = "社群二维码")
    private String communityQrImage;

    @Schema(description = "领取时间限制 0 不限制 1 限制时间")
    @NotNull(message = "领取时间限制 0 不限制 1 限制时间限制不能为空")
    private Integer claimTimeLimit;

    @Schema(description = "领取时间 指定日期段 年月日#年月日")
    private String claimTimeSlot;

    @Schema(description = "领取时间指定几号 1#2#6")
    private String claimDayNo;

    @Schema(description = "领取时间指定周几 1#3")
    private String claimWeekNo;

    @Schema(description = "领取时间指定时间 时分秒#时分秒")
    private String claimTime;

}
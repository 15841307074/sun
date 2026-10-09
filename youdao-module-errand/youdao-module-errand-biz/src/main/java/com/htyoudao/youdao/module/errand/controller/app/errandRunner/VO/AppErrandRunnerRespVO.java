package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * App 骑手入驻详情响应 VO。
 */
@Data
@Schema(description = "App - 骑手入驻详情 Response VO")
public class AppErrandRunnerRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "跑腿员 ID")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "会员 ID")
    private Long memberId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "门店 ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "身份证号")
    private String idCardNo;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "身份证正面图片地址")
    private String idCardFront;

    @Schema(description = "身份证反面图片地址")
    private String idCardBack;

    @Schema(description = "学生证照片地址")
    private String studentCardImg;

    @Schema(description = "审核状态：0待审核 1通过 2失败")
    private Integer auditStatus;

    @Schema(description = "是否已弹窗 0未弹窗 1已弹窗")
    private Integer popupStatus;

    @Schema(description = "审核失败原因")
    private String auditReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间")
    private Date auditTime;

    @Schema(description = "封禁状态：0正常 1封禁")
    private Integer banStatus;

    @Schema(description = "封禁原因")
    private String banReason;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "交易密码设置状态：0未设置 1已设置")
    private Integer tranPasswordStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 跑腿员详情 VO，包含入驻资料变更记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跑腿员详情信息")
public class ErrandRunnerDetailVO extends BusinessBaseDO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键 ID")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "会员 ID，对应 wx_member.id")
    private Long memberId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "门店 ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "学校名称")
    private String schoolName;

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

    @Schema(description = "性别文本")
    private String genderText;

    @Schema(description = "身份证正面")
    private String idCardFront;

    @Schema(description = "身份证反面")
    private String idCardBack;

    @Schema(description = "学生证照片")
    private String studentCardImg;

    @Schema(description = "审核状态：0待审核 1通过 2失败")
    private Integer auditStatus;

    @Schema(description = "是否已弹窗 0未弹窗 1已弹窗")
    private Integer popupStatus;

    @Schema(description = "审核状态文本")
    private String auditStatusText;

    @Schema(description = "审核失败原因")
    private String auditReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间")
    private Date auditTime;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "审核人 ID")
    private Long auditUserId;

    @Schema(description = "封禁状态：0正常 1封禁")
    private Integer banStatus;

    @Schema(description = "封禁状态文本")
    private String banStatusText;

    @Schema(description = "封禁原因")
    private String banReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "封禁时间")
    private Date banTime;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "交易密码设置状态：0未设置 1已设置")
    private Integer tranPasswordStatus;

    @Schema(description = "交易密码设置状态文本")
    private String tranPasswordStatusText;

    @Schema(description = "是否有通过审核：0否 1通过")
    private Integer firstAuditStatus;

    @Schema(description = "首次审核状态文本")
    private String firstAuditStatusText;

    @Schema(description = "入驻申请资料变更记录列表")
    private List<ErrandRunnerChangeLogVO> changeLogs;

    /**
     * 获取性别文本。
     */
    public String getGenderText() {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    /**
     * 获取审核状态文本。
     */
    public String getAuditStatusText() {
        if (auditStatus == null) {
            return "未知";
        }
        switch (auditStatus) {
            case 0:
                return "待审核";
            case 1:
                return "通过";
            case 2:
                return "失败";
            default:
                return "未知";
        }
    }

    /**
     * 获取封禁状态文本。
     */
    public String getBanStatusText() {
        if (banStatus == null) {
            return "正常";
        }
        return banStatus == 1 ? "封禁" : "正常";
    }

    /**
     * 获取交易密码设置状态文本。
     */
    public String getTranPasswordStatusText() {
        if (tranPasswordStatus == null) {
            return "未设置";
        }
        return tranPasswordStatus == 1 ? "已设置" : "未设置";
    }

    /**
     * 获取首次审核状态文本。
     */
    public String getFirstAuditStatusText() {
        if (firstAuditStatus == null) {
            return "否";
        }
        return firstAuditStatus == 1 ? "通过" : "否";
    }
}

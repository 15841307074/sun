package com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 跑腿员表实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "bz_errand_runner", autoResultMap = true)
public class ErrandRunnerDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "会员 ID，对应 wx_member.id")
    private Long memberId;

    @Schema(description = "门店 ID")
    private Long storeId;

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

    @Schema(description = "审核失败原因")
    private String auditReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间")
    private Date auditTime;

    @Schema(description = "审核人 ID")
    private Long auditUserId;

    @Schema(description = "封禁状态：0正常 1封禁")
    private Integer banStatus;

    @Schema(description = "封禁原因")
    private String banReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "封禁时间")
    private Date banTime;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "是否有通过审核：0否 1通过")
    private Integer firstAuditStatus;

}

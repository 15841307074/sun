package com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw;


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
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 跑腿员提现表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "bz_errand_runner_withdraw", autoResultMap = true)
public class ErrandRunnerWithdrawDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "提现单号")
    private String withdrawSn;

    @Schema(description = "跑腿员ID")
    private Long runnerId;

    @Schema(description = "跑腿员会员ID")
    private Long runnerMemberId;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal serviceFee;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "状态：1提现中 2已提现 3提现失败")
    private Integer status;

    @Schema(description = "微信提现批次号")
    private String wechatBatchNo;

    @Schema(description = "微信提现明细号")
    private String wechatDetailNo;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "可用余款")
    private BigDecimal balance;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "申请时间")
    private Date applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "完成时间")
    private Date finishTime;

    @Schema(description = "变动记录id")
    private Long logId;


}

package com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog;


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
 * 跑腿员余额流水表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "bz_errand_runner_balance_log", autoResultMap = true)
public class ErrandRunnerBalanceLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "跑腿员ID")
    private Long runnerId;

    @Schema(description = "跑腿员会员ID")
    private Long runnerMemberId;

    @Schema(description = "关联订单号")
    private String orderSn;

    @Schema(description = "提现单ID")
    private Long withdrawId;

    @Schema(description = "流水类型：1赏金入账 2提现扣减 3退款扣回 4提现失败退回 5人工调整 6赏金解冻")
    private Integer flowType;

    @Schema(description = "方向：1收入 2支出")
    private Integer direction;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动前余额")
    private BigDecimal beforeBalance;

    @Schema(description = "变动后余额")
    private BigDecimal afterBalance;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "业务流水号，防重复")
    private String bizNo;

    @Schema(description = "备注")
    private String remark;

}

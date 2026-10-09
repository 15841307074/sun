package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 余额流水VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额流水信息")
public class BalanceLogVO  {

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "流水类型：1赏金入账 2提现扣减 3退款扣回 4提现失败退回 5人工调整 6赏金解冻")
    private Integer flowType;

    @Schema(description = "流水类型名称")
    private String flowTypeName;

    @Schema(description = "方向：1收入 2支出")
    private Integer direction;

    @Schema(description = "方向名称")
    private String directionName;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动后余额")
    private BigDecimal afterBalance;

    @Schema(description = "流水发生后的可用余额")
    private BigDecimal balance;

    @Schema(description = "流水发生后的冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "关联订单号")
    private String orderSn;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：1提现中 2已提现 3提现失败")
    private Integer status;

    @Schema(description = "微信提现批次号")
    private String wechatBatchNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "完成时间")
    private Date finishTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
}

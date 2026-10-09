package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跑腿员余额统计信息")
public class ErrandRunnerBalanceLogRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    @Schema(description = "流水类型：1赏金入账 2提现扣减 3退款扣回 4提现失败退回 5人工调整 6赏金解冻")
    private Integer flowType;

    @Schema(description = "关联订单号")
    private String orderSn;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "冻结")
    private BigDecimal frozenBalance;

    @Schema(description = "可提现")
    private BigDecimal withdrawable;

    @Schema(description = "方向：1收入 2支出")
    private Integer direction;

//
//    @Schema(description = "方向：1收入 2支出")
//    private Integer direction;



//    @Schema(description = "变动前余额")
//    private BigDecimal beforeBalance;
//
//    @Schema(description = "变动后余额")
//    private BigDecimal afterBalance;
//
//    @Schema(description = "业务流水号，防重复")
//    private String bizNo;
//
//    @Schema(description = "提现单ID")
//    private Long withdrawId;

    @Schema(description = "备注")
    private String remark;
}

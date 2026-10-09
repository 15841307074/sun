package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LotteryLogStatisticsRespVO{



    @Schema(name = "参与人数", description = "参与人数")
    private Long participationCount;

    @Schema(name = "参与人数", description = "参与人数")
    private Long participation;


    @Schema(name = "抽奖次数", description = "抽奖次数")
    private Long lotteryCount;


    @Schema(name = "总积分", description = "总积分")
    private BigDecimal prizeValue;


}

package com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName(value = "lottery_transfer_scene_report", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryTransferSceneReportDO extends BusinessBaseDO {


    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(name = "outBillNo", description = "关联转账单号")
    private String outBillNo;

    @Schema(name = "infoType", description = "信息类型")
    private String infoType;

    @Schema(name = "infoContent", description = "信息内容")
    private String infoContent;

}

package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

@TableName(value = "activity_card_log", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCardLogDO extends BusinessBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;


    @Schema(name = "activityId", description = "活动主表id")
    private Long activityId;

    @Schema(name = "cardId", description = "卡片id")
    private Long cardId;

    @Schema(name = "cardType", description = "卡片类型 1 兜底卡 2 套系卡 3 万能卡 4 隐藏卡")
    private Integer cardType;


    @Schema(name = "cardName", description = "卡片名称")
    private String cardName;

    @Schema(name = "cardImgUrl", description = "卡片图片")
    private String cardImgUrl;

    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;

    @Schema(name = "memberName", description = "会员名称")
    private String memberName;


    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;

    @Schema(name = "storeId", description = "门店id")
    private Long storeId;

    @Schema(name = "storeName", description = "门店名称")
    private String storeName;

    @Schema(name = "status", description = "使用状态（0未使用 1已使用）")
    private Integer status;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;
}

package com.htyoudao.youdao.module.promotion.dal.dataobject.lottery;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("lottery_settings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LotterySettingsDO extends BusinessBaseDO {
    /** 范围和配置变更不改变库存批次。 */
    private Long configVersion;
    private Long stockEpoch;
    private Integer runtimeVersion;
    private String prizeTemplateJson;
    private String lastResetScope;


    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;


    /** 抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒 */
    @Schema(name = "lotteryType", description = "抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒")
    private Integer lotteryType;

    /** 状态 0 禁用 1 启用 */
    @Schema(name = "state", description = "状态 0 禁用 1 启用")
    private Integer state;

    /** 活动标题 */
    @Schema(name = "lotteryTitle", description = "活动标题")
    private String lotteryTitle;

    /** 活动规则 */
    @Schema(name = "lotteryRule", description = "活动规则")
    private String lotteryRule;

    /** 活动主表Id*/
    @Schema(name = "activityId", description = "活动主表Id")
    private Long activityId;

    @Schema(name = "shareType", description = "1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友")
    private Integer shareType;


    /** 活动开始时间 */
    @Schema(name = "lotteryStartTime", description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lotteryStartTime;

    /** 活动结束时间 */
    @Schema(name = "lotteryEndTime", description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lotteryEndTime;

    /** 抽奖次数 0 无限制 其余 （每天/次） */
    @Schema(name = "lotteryLimit", description = "抽奖次数 0 无限制 其余 （每天/次）")
    private Integer lotteryLimit;


    @Schema(description = "1 全部商品  2可选商品")
    private Integer activityProduct;

    /** 抽奖积分价格 */
    @Schema(name = "price", description = "抽奖积分价格")
    private Integer price;

    /** 分享标题 */
    @Schema(name = "shareTitle", description = "分享标题")
    private String shareTitle;

    /** 分享内容 */
    @Schema(name = "shareNote", description = "分享内容")
    private String shareNote;

    /** 分享图片 */
    @Schema(name = "shareImgUrl", description = "分享图片")
    private String shareImgUrl;

    /** 门店 id */
    @Schema(name = "storeId", description = "门店 id")
    private Long storeId;

    /** 活动图片 */
    @Schema(name = "activityImgUrl", description = "活动图片")
    private String activityImgUrl;

    @Schema(description = "支付门槛（弃用）")
    private BigDecimal paymentThreshold;





    /**
     * 活动背景图
     */
    @Schema(name = "activityBackground", description = "活动背景图")
    private String activityBackground;


    /**
     * 按钮抽奖图
     */
    @Schema(name = "buttonImgUrl", description = "按钮抽奖图")
    private String buttonImgUrl;



    // 背景色（如#FFFFFF）
    @Schema(description = "背景色（如#FFFFFF）")
    private String backgroundColor;

    @Schema(description = "抽奖方式 1 免费抽奖 2 积分抽奖 3 下单抽奖")
    private Integer lotteryMethod;

    @Schema(name = "lotteryTotalNumber", description = "抽奖总次数 0 无限制 （总次数）")
    private Integer lotteryTotalNumber;



    /** 是否是免费 */
    @Schema(name = "isFree", description = "是否是免费")
    private Integer isFree;
    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name",fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name",fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateUserName;


    @Schema(name = "calculation_rules", description = "计算规则(1 按天计算抽奖次数  2 按场次计算抽奖次数)")
    private Integer calculationRules;

    @Schema(name = "prize_pool_rules", description = "门店奖池规则(1 共用奖池  2 独立奖池)")
    private Integer prizePoolRules;


    @Schema(name = "lottery_rules_reset", description = "奖品及抽奖规则重置（0关闭 1开启）")
    private Integer lotteryRulesReset;

    @Schema(name = "public_button", description = "公共展示（0开启  1 关闭）")
    private Integer publicButton;

    @Schema(description = "1 不开启  2 开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "浏览类型（0关闭 1开启）")
    private Integer browseType;


    @Schema(description = "浏览抽奖次数")
    private Integer browseCount;



    @Schema(description = "免费获得（0关闭 1开启")
    private Integer freeStatus;


    @Schema(description = "免费限制次数")
    private Integer freeCount;

    @Schema(description = "积分获得（0关闭 1开启）")
    private Integer pointsStatus;

    @Schema(description = "积分状态 0 每人每天 1不限制")
    private Integer pointsType;

    @Schema(description = "积分限制次数")
    private Integer pointsCount;

    @Schema(description = "下单获得（0关闭 1开启）")
    private Integer orderStatus;

    @Schema(description = "下单类型（1 按品类限制 2 按商品限制）")
    private Integer placeOrderType;

    @Schema(description = "参与品类（1 全部 2 仅单品 3仅套餐）")
    private Integer categoryType;
    @Schema(description = "支付门槛（0代表不限制 1限制）")
    private Integer paymentType;

    @Schema(description = "支付金额")
    private BigDecimal paymentCount;

    @Schema(description = "下单状态 0 每人每天 1不限制")
    private Integer placeOrderLottery;


    @Schema(description = "下单次数")
    private Integer placeOrderLotteryNumber;


    @Schema(description = "分享活动 （0代表不开启 1开启）")
    private Integer shareEvent;

    @Schema(description = "分享次数")
    private Integer shareCount;






}

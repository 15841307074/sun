package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.TagValueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 活动配置 Response VO")
@Data
public class LotterySettingsRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "活动主表 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    @Schema(description = "应用范围：0 门店，1 标签")
    private Integer appScope = 0;

    @Schema(description = "是否全部门店：1 是，0 否")
    private Integer activityStore;

    @Schema(description = "适用标签 ID")
    private List<Long> tagIds = new ArrayList<>();

    @Schema(description = "适用标签名称及 ID")
    private List<TagValueDTO> tagInfoDTOS = new ArrayList<>();

    @Schema(description = "当前适用门店")
    private List<StoreInfoDTO> storeInfoDTOS = new ArrayList<>();

    @Schema(description = "当前适用门店 ID")
    private List<Long> storeIds = new ArrayList<>();


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

    /** 是否是免费 */
    @Schema(name = "isFree", description = "是否是免费")
    private Integer isFree;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    private LocalDateTime createTime;

    @Schema(description = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    private LocalDateTime updateTime;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "修改人名称")
    private String updateUserName;
}

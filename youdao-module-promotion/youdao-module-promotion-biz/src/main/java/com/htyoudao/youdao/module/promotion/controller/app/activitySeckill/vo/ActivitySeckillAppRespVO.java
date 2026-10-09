package com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillStoreNameRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Schema(description = "APP - 秒杀活动详情 返回 VO")
@Data
public class ActivitySeckillAppRespVO {

    @Schema(description = "id")
    private Long id;
    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 活动备注（最多200字）
     */
    @Schema(description = "活动备注")
    private String remark;

    /**
     * 推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）
     */
    @Schema(description = "推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）")
    private Integer participantGroup;

    @Schema(description = "类型 1优惠券 2或空 商品")
    private Integer discountType;

    /**
     * 选择人群（存储人群ID，逗号分割）
     */
    @Schema(description = "选择人群（存储人群ID，逗号分割）")
    private List<Long> selectedGroups = new ArrayList<>();

    /**
     * 活动规则（富文本内容）
     */
    @Schema(description = "活动规则（富文本内容）")
    private String activityRules;

    @Schema(description = "图片地址")
    @NotNull(message = "图片地址 不能为空")
    private String imageUrl;

    @Schema(description = "背景色（如#FFFFFF）")
    @NotNull(message = "背景色 不能为空")
    private String backgroundColor;

    @Schema(description = "分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）")
    private Integer shareSetting;

    @Schema(description = "是否显示弹幕开关（0-关，1-开）")
    private Integer isBarrageShow;

    @Schema(description = "时间是否满足")
    private Boolean timeValid;

    @Schema(description = "是否开启（0不开启 1开启）")
    private Integer isEnabled = 0;

    @Schema(description = "开始日期）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date endDate;

    @Schema(description = "日期集合")
    private List<Integer> dayNumberList;

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList;

    /**
     * 是否全门店参与（0-否，1-是）
     */
    @Schema(description = "是否全门店参与（0-否，1-是）")
    private Integer activityStore = 0;

    @Schema(description = "秒杀场次集合")
    private List<ActivitySeckillAppTimeResqVO> times = new ArrayList<>();

    @Schema(description = "门店集合包含门店名")
    private List<ActivitySeckillStoreNameRespVO>   activitySeckillStoreNameRespVOS = new ArrayList<>();

    @Schema(description = "门店ID集合")
    private List<Long> storeIds = new ArrayList<>();

}

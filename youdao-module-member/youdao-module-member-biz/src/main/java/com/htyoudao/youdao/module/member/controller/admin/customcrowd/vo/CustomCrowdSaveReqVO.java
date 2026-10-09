package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群新增/修改 Request VO")
@Data
public class CustomCrowdSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21898")
    private Long id;

    @Schema(description = "人群名称")
    private String crowdName;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "0不选  1全部 2男 3女")
    private Integer gender;

    @Schema(description = "会员生日类型 0不选 1区间 2最近 3未来", example = "1")
    private Integer birthdayType;

    @Schema(description = "生日查询条件")
    private String birthdayValue;

    @Schema(description = "会员等级 0不选    id-id")
    private String memberLevel;

    @Schema(description = "会员标签 0不选   1选了找关系表 crowd_tag")
    private Integer memberTag;

    @Schema(description = "会员积分 0不选 1区间 2小于 3大于", example = "2")
    private Integer memberIntegralType;

    @Schema(description = "会员积分查询条件")
    private String memberIntegralValue;

    @Schema(description = "	注册平台  0不选 1微信 2支付宝(多选)")
    private String memberCategory;

    @Schema(description = "访问小程序 0不选 1访问过 2未访问过", example = "2")
    private Integer visitType;

    @Schema(description = "访问小程序查询条件")
    private Integer visitValue;

    @Schema(description = "加入购物车 0不选 1加过 2未加过", example = "1")
    private Integer trolleyType;

    @Schema(description = "加入购物车查询条件")
    private Integer trolleyValue;

    @Schema(description = "分享小程序 0不选 1分享过 2未分享过", example = "2")
    private Integer shareType;

    private Integer shareValue;

    @Schema(description = "所属门店 0不选 1选了找关系表 crowd_store")
    private Integer belongStore;

    @Schema(description = "统计时间 0不选 1指定时间 2自定义时间", example = "2")
    private Integer censusDateType;

    @Schema(description = "0近7天 1近30天 2近90天(按原型顺序)/-号")
    private String censusDateValue;

    @Schema(description = "有效下单频次 0不选 1区间  2小于   3大于", example = "2")
    private Integer orderFrequencyType;

    @Schema(description = "有效下单频次查询条件")
    private String orderFrequencyValue;

    @Schema(description = "有效购买金额 0不选 1区间  2小于   3大于", example = "2")
    private Integer orderAmountType;

    @Schema(description = "有效购买金额查询条件")
    private String orderAmountValue;

    @Schema(description = "单均消费金额 0不选 1区间  2小于   3大于", example = "2")
    private Integer avgAmountType;

    @Schema(description = "单均消费金额查询条件")
    private String avgAmountValue;

    @Schema(description = "购买品类 0不选 1单品 2套餐")
    private String purchaseCategory;

    @Schema(description = "回购周期 0不选 1区间  2小于   3大于", example = "1")
    private Integer repurchaseType;

    @Schema(description = "回购周期查询条件")
    private String repurchaseValue;

    @Schema(description = "末次距今下单时间 0不选 1区间  2小于   3大于", example = "2")
    private Integer finalRepurchaseType;

    @Schema(description = "末次距今下单时间查询条件")
    private String finalRepurchaseValue;

    @Schema(description = "绑定门店的集合")
    private List<CrowdStoreSaveReqVO> crowdStores;

    @Schema(description = "绑定标签的集合")
    private List<CrowdTagSaveReqVO> crowdTags;

    @Schema(description = "基础信息  0没选 1选了")
    private Integer basicInfo;

    @Schema(description = "客户行为 0没选 1选了")
    private Integer customBehavior;

    @Schema(description = "客户分析 0没选 1选了")
    private Integer customAnalysis;

    @Schema(description = "是否在社群 0 没选 1没在 2在群里")
    private Integer communityFlag;
}
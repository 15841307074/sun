package com.htyoudao.youdao.module.member.dal.dataobject.crowd;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

/**
 * 自定义人群 DO
 * @author 芋道源码
 */
@TableName("custom_crowd")
@KeySequence("custom_crowd_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomCrowdDO extends BusinessBaseDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 人群名称
     */
    private String crowdName;
    /**
     * 备注
     */
    private String remark;
    /**
     * 0不选  1全部 2男 3女
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer gender;
    /**
     * 会员生日类型 0不选 1区间 2最近 3未来
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer birthdayType;
    /**
     * 生日查询条件
     */
    private String birthdayValue;
    /**
     * 会员等级 0不选    id-id
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String memberLevel;
    /**
     * 会员标签 0不选   1选了找关系表 crowd_tag
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer memberTag;
    /**
     * 会员积分 0不选 1区间 2小于 3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer memberIntegralType;
    /**
     * 会员积分查询条件
     */
    private String memberIntegralValue;
    /**
     * 	注册平台  0不选 1微信 2支付宝(多选)
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String memberCategory;
    /**
     * 访问小程序 0不选 1访问过 2未访问过
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer visitType;
    /**
     * 访问小程序查询条件
     */
    private Integer visitValue;
    /**
     * 加入购物车 0不选 1加过 2未加过
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer trolleyType;
    /**
     * 加入购物车查询条件
     */
    private Integer trolleyValue;
    /**
     * 分享小程序 0不选 1分享过 2未分享过
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer shareType;

    private Integer shareValue;
    /**
     * 所属门店 0不选 1选了找关系表 crowd_store
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer belongStore;
    /**
     * 统计时间 0不选 1指定时间 2自定义时间
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer censusDateType;
    /**
     * 0近7天 1近30天 2近90天(按原型顺序)/-号
     */
    private String censusDateValue;
    /**
     * 有效下单频次 0不选 1区间  2小于   3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer orderFrequencyType;
    /**
     * 有效下单频次查询条件
     */
    private String orderFrequencyValue;
    /**
     * 有效购买金额 0不选 1区间  2小于   3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer orderAmountType;
    /**
     * 有效购买金额查询条件
     */
    private String orderAmountValue;
    /**
     * 单均消费金额 0不选 1区间  2小于   3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer avgAmountType;
    /**
     * 单均消费金额查询条件
     */
    private String avgAmountValue;
    /**
     * 购买品类 0不选 1单品 2套餐
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String purchaseCategory;
    /**
     * 回购周期 0不选 1区间  2小于   3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer repurchaseType;
    /**
     * 回购周期查询条件
     */
    private String repurchaseValue;
    /**
     * 末次距今下单时间 0不选 1区间  2小于   3大于
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer finalRepurchaseType;
    /**
     * 末次距今下单时间查询条件
     */
    private String finalRepurchaseValue;

    /**
     * 基础信息  0没选 1选了
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer basicInfo;

    /**
     * 客户行为 0没选 1选了
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer customBehavior;

    /**
     * 客户分析 0没选 1选了
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer customAnalysis;

    /**
     * 是否在社群 0 没选 1没在 2在群里
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer communityFlag;

}
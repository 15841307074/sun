package com.htyoudao.youdao.module.promotion.api.enums;

/**
 * Promotion 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author 0090
 */
public interface LogRecordConstants {


// ======================= PROMOTION_GOOD_COUPON 营销优惠券 =======================

    String PROMOTION_GOOD_COUPON_TYPE = "PROMOTION 营销优惠券";

    String PROMOTION_GOOD_COUPON_CREATE_SUB_TYPE = "创建优惠券";
    String PROMOTION_GOOD_COUPON_CREATE_SUCCESS = "创建了优惠券【{{#createReqVO.couponName}}】";


    String PROMOTION_GOOD_COUPON_UPDATE_SUB_TYPE = "修改优惠券";
    String PROMOTION_GOOD_COUPON_UPDATE_SUCCESS = "修改了优惠券【{{#updateReqVO.couponName}}】";


    String PROMOTION_GOOD_COUPON_DELETE_SUB_TYPE = "删除优惠券";
    String PROMOTION_GOOD_COUPON_DELETE_SUCCESS = "删除了优惠券【{{#id}}】";


    String PROMOTION_GOOD_COUPON_GROUND_SUB_TYPE = "上下架优惠券";
    String PROMOTION_GOOD_COUPON_GROUND_SUCCESS = "【{{#ground}}】";

    String PROMOTION_GOOD_COUPON_COPY_SUB_TYPE = "复制优惠券";
    String PROMOTION_GOOD_COUPON_COPY_SUCCESS = "复制了优惠券【{{#goodCoupon.couponName}}】";


    String PROMOTION_TIK_TOK_GOOD_COUPON_COPY_SUB_TYPE = "复制优惠券";
    String PROMOTION_TIK_TOK_GOOD_COUPON_COPY_SUCCESS = "复制了优惠券【{{#id}}】";

    String PROMOTION_GOOD_COUPON_ISSUE_SUB_TYPE = "推广优惠券";
    String PROMOTION_GOOD_COUPON_ISSUE_SUCCESS = "推广了优惠券【{{#goodCoupon.couponName}}】";

    // ======================= PROMOTION_GOOD_COUPON_PACKAGE 营销优惠券包 =======================
    String PROMOTION_GOOD_COUPON_PACKAGE_TYPE = "PROMOTION 营销优惠券包";

    String PROMOTION_GOOD_COUPON_PACKAGE_CREATE_SUB_TYPE = "创建优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_CREATE_SUCCESS = "创建了优惠券包【{{#createReqVO.packageName}}】";


    String PROMOTION_GOOD_COUPON_PACKAGE_UPDATE_SUB_TYPE = "修改优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_UPDATE_SUCCESS = "修改优惠券包【{{#updateReqVO.packageName}}】";


    String PROMOTION_GOOD_COUPON_PACKAGE_DELETE_SUB_TYPE = "删除优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_DELETE_SUCCESS = "删除了优惠券包【{{#id}}】";


    String PROMOTION_GOOD_COUPON_PACKAGE_GROUND_SUB_TYPE = "上下架优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_GROUND_SUCCESS = "【{{#ground}}】";

    String PROMOTION_GOOD_COUPON_PACKAGE_COPY_SUB_TYPE = "复制优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_COPY_SUCCESS = "复制了优惠券包【{{#goodCoupon.couponName}}】";

    String PROMOTION_GOOD_COUPON_PACKAGE_ISSUE_SUB_TYPE = "推广优惠券包";
    String PROMOTION_GOOD_COUPON_PACKAGE_ISSUE_SUCCESS = "推广了优惠券包【{{#goodCoupon.couponName}}】";


    // ======================= 广告 =======================
    String PROMOTION_ADVERTISING_TYPE ="广告";
    String PROMOTION_ADVERTISING_CREATE_TYPE = "新增广告";
    String PROMOTION_ADVERTISING_CREATE_SUCCESS = "新增广告【{{#advertising.adName != null ? #advertising.adName : #advertising.adName}}】";
    String PROMOTION_ADVERTISING_UPDATE_TYPE= "修改广告";
    String PROMOTION_ADVERTISING_UPDATE_SUCCESS = "更新了广告【{{#advertising.adName != null ? #advertising.adName : #advertising.adName}}】";
    String PROMOTION_ADVERTISING_DELETE_TYPE= "删除广告";
    String PROMOTION_ADVERTISING_DELETE_SUCCESS = "删除了广告【{{#advertising.adName != null ? #advertising.adName : #advertising.adName}}】";

    String PROMOTION_ADVERTISING_STATUS_TYPE= "修改状态";
    String PROMOTION_ADVERTISING_STATUS_SUCCESS = "修改了广告状态【{{#advertising.isOpen != null ? #advertising.isOpen : #advertising.isOpen}}】";



    // ======================= 营销活动 =======================
    String PROMOTION_ACTIVITY_TYPE ="活动";

    String PROMOTION_ACTIVITY_CREATE_TYPE = "新增活动";
    String PROMOTION_ACTIVITY_CREATE_SUCCESS = "新增活动【{{#activity.activityName}}】";

    String PROMOTION_ACTIVITY_UPDATE_TYPE = "修改活动";
    String PROMOTION_ACTIVITY_UPDATE_SUCCESS = "修改活动【{{#activity.activityName}}】";

    String PROMOTION_ACTIVITY_DELETE_TYPE = "删除活动";
    String PROMOTION_ACTIVITY_DELETE_SUCCESS = "删除活动";


    String PROMOTION_ACTIVITY_STATUS_TYPE = "修改状态";
    String PROMOTION_ACTIVITY_STATUS_SUCCESS = "修改了活动状态【{{#status}}】";

    // ======================= 营销活动渠道名称 =======================
    String PROMOTION_ACTIVITY_CHANNEL_NAME_TYPE = "渠道名称";

    String PROMOTION_ACTIVITY_CHANNEL_NAME_CREATE_TYPE = "新增渠道";
    String PROMOTION_ACTIVITY_CHANNEL_NAME_CREATE_SUCCESS = "新增渠道【{{#channel.name}}】";

    String PROMOTION_ACTIVITY_CHANNEL_NAME_UPDATE_TYPE = "修改渠道";
    String PROMOTION_ACTIVITY_CHANNEL_NAME_UPDATE_SUCCESS = "修改渠道【{{#id}}】：【{{#oldName}}】->【{{#newName}}】";

    String PROMOTION_ACTIVITY_CHANNEL_NAME_DELETE_TYPE = "删除渠道";
    String PROMOTION_ACTIVITY_CHANNEL_NAME_DELETE_SUCCESS = "删除渠道【{{#channel.name}}】";

    String PROMOTION_ACTIVITY_CHANNEL_NAME_STATUS_TYPE = "启用/禁用";
    String PROMOTION_ACTIVITY_CHANNEL_NAME_STATUS_SUCCESS = "修改渠道状态【{{#channel.name}}】，isEnable={{#channel.isEnable}}";
}

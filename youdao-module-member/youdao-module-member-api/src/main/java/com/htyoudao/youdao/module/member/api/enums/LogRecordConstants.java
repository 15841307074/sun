package com.htyoudao.youdao.module.member.api.enums;

/**
 * Promotion 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author 0090
 */
public interface LogRecordConstants {



    // ======================= 积分商品 =======================
    String MEMBER_POINTSPRODUCT_TYPE ="积分商品";
    String MEMBER_POINTSPRODUCT_CREATE_TYPE = "新增积分商品";
    String MEMBER_POINTSPRODUCT_CREATE_SUCCESS = "新增积分商品【{{#pointsProduct.productName != null ? #pointsProduct.productName : #pointsProduct.productName}}】";
    String MEMBER_POINTSPRODUCT_UPDATE_TYPE= "修改积分商品";
    String MEMBER_POINTSPRODUCT_UPDATE_SUCCESS = "更新了积分商品【{{#pointsProduct.productName != null ? #pointsProduct.productName : #pointsProduct.productName}}】";
    String MEMBER_POINTSPRODUCT_DELETE_TYPE= "删除积分商品";
    String MEMBER_POINTSPRODUCT_DELETE_SUCCESS = "删除了积分商品【{{#pointsProduct.productName != null ? #pointsProduct.productName : #pointsProduct.productName}}】";


    // ======================= 积分 =======================
    String MEMBER_POINTS_TYPE ="积分";

    String MEMBER_POINTS_UPDATE_TYPE= "修改快递单号";

    String MEMBER_POINTS_UPDATE_SUCCESS= "修改了快递单号【{{#points.trackingNumber != null ? #points.trackingNumber : #points.trackingNumber}}】";


    // ======================= 人群 =======================
    String MEMBER_CUSTOM_CROWDS_TYPE = "人群";

    String MEMBER_CUSTOM_CROWDS_CREATE_SUB_TYPE = "创建人群";
    String MEMBER_CUSTOM_CROWDS_CREATE_SUCCESS = "创建了人群【{{#createReqVO.crowdName}}】";

    String MEMBER_CUSTOM_CROWDS_UPDATE_SUB_TYPE = "修改人群";
    String MEMBER_CUSTOM_CROWDS_UPDATE_SUCCESS = "修改了人群【{{#updateReqVO.crowdName}}】";

    String MEMBER_CUSTOM_CROWDS_DELETE_SUB_TYPE = "删除人群";
    String MEMBER_CUSTOM_CROWDS_DELETE_SUCCESS = "删除了人群【{{#id}}】";
}

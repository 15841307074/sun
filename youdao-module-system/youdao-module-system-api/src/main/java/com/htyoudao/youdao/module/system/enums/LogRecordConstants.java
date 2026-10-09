package com.htyoudao.youdao.module.system.enums;

/**
 * System 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author 0090
 */
public interface LogRecordConstants {

    // ======================= SYSTEM_BUSINESS 项目 =======================

    String SYSTEM_BUSINESS_TYPE = "SYSTEM 项目";
    String SYSTEM_BUSINESS_CREATE_SUB_TYPE = "创建项目";
    String SYSTEM_BUSINESS_CREATE_SUCCESS = "创建了项目【{{#business.name}}】";
    String SYSTEM_BUSINESS_UPDATE_SUB_TYPE = "更新项目";
    String SYSTEM_BUSINESS_UPDATE_SUCCESS = "更新了项目【{{#business.name}}】: {_DIFF{#updateReqVO}}";
    String SYSTEM_BUSINESS_DELETE_SUB_TYPE = "删除项目";
    String SYSTEM_BUSINESS_DELETE_SUCCESS = "删除了项目【{{#business.name}}】";
    String SYSTEM_BUSINESS_STATUS_SUB_TYPE = "修改项目状态";
    String SYSTEM_BUSINESS_STATUS_SUCCESS = "修改项目状态【{{#business.name}}】: 【{{#business.status}}】";


    // ======================= SYSTEM_USER 用户 =======================

    String SYSTEM_USER_TYPE = "SYSTEM 用户";
    String SYSTEM_USER_CREATE_SUB_TYPE = "创建用户";
    String SYSTEM_USER_CREATE_SUCCESS = "创建了用户【{{#user.nickname != null ? #user.nickname : #user.username}}】";
    String SYSTEM_USER_UPDATE_SUB_TYPE = "更新用户";
    String SYSTEM_USER_UPDATE_SUCCESS = "更新了用户【{{#user.nickname != null ? #user.nickname : #user.username}}】: {_DIFF{#updateReqVO}}";
    String SYSTEM_USER_DELETE_SUB_TYPE = "删除用户";
    String SYSTEM_USER_DELETE_SUCCESS = "删除了用户【{{#user.nickname != null ? #user.nickname : #user.username}}】";
    String SYSTEM_USER_UPDATE_PASSWORD_SUB_TYPE = "重置用户密码";
    String SYSTEM_USER_UPDATE_PASSWORD_SUCCESS = "将用户【{{#user.nickname != null ? #user.nickname : #user.username}}】的密码从【{{#user.password}}】重置为【{{#newPassword}}】";

    // ======================= SYSTEM_ROLE 角色 =======================

    String SYSTEM_ROLE_TYPE = "SYSTEM 角色";
    String SYSTEM_ROLE_CREATE_SUB_TYPE = "创建角色";
    String SYSTEM_ROLE_CREATE_SUCCESS = "创建了角色【{{#role.name}}】";
    String SYSTEM_ROLE_UPDATE_SUB_TYPE = "更新角色";
    String SYSTEM_ROLE_UPDATE_SUCCESS = "更新了角色【{{#role.name}}】: {_DIFF{#updateReqVO}}";
    String SYSTEM_ROLE_DELETE_SUB_TYPE = "删除角色";
    String SYSTEM_ROLE_DELETE_SUCCESS = "删除了角色【{{#role.name}}】";
    // ======================= SYSTEM_STORE 门店 =======================
    String SYSTEM_STORE_TYPE = "SYSTEM 门店";
    String SYSTEM_STORE_CREATE_SUB_TYPE = "创建门店";
    String SYSTEM_STORE_CREATE_SUCCESS = "创建了门店【{{#store.storeName}}】";
    String SYSTEM_STORE_UPDATE_SUB_TYPE = "更新门店";
    String SYSTEM_STORE_UPDATE_SUCCESS = "更新了门店【{{#store.storeName}}】: {_DIFF{#storeUserSaveReqVO}}";
    String SYSTEM_STORE_DELETE_SUB_TYPE = "删除门店";
    String SYSTEM_STORE_DELETE_SUCCESS = "删除了门店【{{#store.storeName}}】";
    String SYSTEM_STORE_UPDATE_SUB_TYPE_DC = "点餐机更新门店";
    String SYSTEM_STORE_UPDATE_SUB_TYPE_BOSS = "老板助手更新门店";

    // ======================= SYSTEM_ORG 组织 =======================

    String SYSTEM_ORG_TYPE = "SYSTEM 组织";
    String SYSTEM_ORG_CREATE_SUB_TYPE = "创建组织";
    String SYSTEM_ORG_CREATE_SUCCESS = "创建了用户【{{#createReqVO.name}}】";

    String SYSTEM_ORG_UPDATE_SUB_TYPE = "修改组织";
    String SYSTEM_ORG_UPDATE_SUCCESS = "修改了用户【{{#updateReqVO.name}}】";


    String SYSTEM_ORG_ADD_STORE_TYPE = "组织添加门店";
    String SYSTEM_ORG_ADD_STORE_SUCCESS = "组织添加门店【{{#orgStoreSaveReqVO.storeIds}}】";


    String SYSTEM_ORG_ADD_USER_TYPE = "组织添加人员";
    String SYSTEM_ORG_ADD_USER_SUCCESS = "组织添加人员【{{#orgUserSaveReqVO.orgId}}】";

    String SYSTEM_ORG_MOVE_USER_TYPE = "组织移动人员";
    String SYSTEM_ORG_MOVE_USER_SUCCESS = "组织移动人员【{{#orgMoveReqVO.userIds}}】";


    String SYSTEM_ORG_REMOVE_USER_TYPE = "组织移除人员";
    String SYSTEM_ORG_REMOVE_USER_SUCCESS = "组织移除人员【{{#orgMoveReqVO.userIds}}】";


    String SYSTEM_ORG_SET_CHARGE_TYPE = "组织设置人员为负责人";
    String SYSTEM_ORG_SET_CHARGE_SUCCESS = "组织设置人员为负责人【{{#orgUserChargedReqVO.orgUserId}}】";


    String SYSTEM_ORG_UN_CHARGE_TYPE = "组织取消人员负责人";
    String SYSTEM_ORG_UN_CHARGE_SUCCESS = "组织撤销人员负责人【{{#orgUserChargedReqVO.orgUserId}}】";


    String SYSTEM_ORG_MOVE_TYPE = "组织删除";
    String SYSTEM_ORG_MOVE_SUCCESS = "组织删除【{{#orgMoveReqVO.newOrgId}}】";

    String SYSTEM_ORG_DELETE_TYPE = "组织删除";
    String SYSTEM_ORG_DELETE_SUCCESS = "组织删除【{{#id}}】";


    String SYSTEM_ORG_MOVE_STORE_TYPE = "组织移动门店";
    String SYSTEM_ORG_MOVE_STORE_SUCCESS = "组织移动门店到【{{#orgStoreMoveReqVO.newOrgId}}】";


    String SYSTEM_ORG_SET_STORE_MANAGER_TYPE = "门店设置负责人";
    String SYSTEM_ORG_SET_STORE_MANAGER_SUCCESS = "门店设置负责人【{{#storeManagerReqVO.userId}}】";

    String SYSTEM_ORG_UN_STORE_MANAGER_TYPE = "门店撤销负责人";
    String SYSTEM_ORG_UN_STORE_MANAGER_SUCCESS = "门店撤销负责人【{{#storeManagerReqVO.userId}}】";


    String SYSTEM_ORG_REMOVE_STORE_USER_TYPE = "组织移除门店人员";
    String SYSTEM_ORG_REMOVE_STORE_USER_SUCCESS = "组织移除门店人员【{{#storeUserRemoveReqVO.storeId}}】";


    String SYSTEM_ORG_STORE_USER_VISIBLE_TYPE = "门店设置人员可见";
    String SYSTEM_ORG_STORE_USER_VISIBLE_TYPE_SUCCESS = "门店设置人员可见【{{#storeManagerReqVO.storeId}}】";


    String SYSTEM_ORG_STORE_USER_UNVISIBLE_TYPE = "门店设置人员不可见";
    String SYSTEM_ORG_STORE_USER_UNVISIBLE_TYPE_SUCCESS = "门店设置人员不可见【{{#storeManagerReqVO.storeId}}】";

    // ======================= SYSTEM_SPLICING_ORDER 拼单 =======================
    String SYSTEM_SPLICING_ORDER_TYPE = "SYSTEM 拼单";
    String SYSTEM_SPLICING_ORDER_UPDATE_TYPE = "更新拼单信息";
    String SYSTEM_SPLICING_ORDER_UPDATE_SUCCESS = "更新拼单信息【{_DIFF{#splicingOrderConfig}}】";


    // ======================= SYSTEM_WX_STORE_CONFIG =======================

    String SYSTEM_WX_STORE_CONFIG_TYPE = "SYSTEM 企业微信";
    String SYSTEM_WX_STORE_CONFIG_ADD_TYPE = "添加二维码";
    String SYSTEM_WX_STORE_CONFIG_ADD_SUCCESS = "添加二维码信息【{{#storeWecomConfig.id == null ? 1 : #storeWecomConfig.id}}】";
    String SYSTEM_WX_STORE_CONFIG_DELETE_TYPE = "删除二维码";
    String SYSTEM_WX_STORE_CONFIG_DELETE_SUCCESS = "删除二维码信息【{{#storeWecomConfigId}}】";

    // ======================= SYSTEM_LOTTERY_LOG =======================

    String SYSTEM_LOTTERY_LOG_TYPE = "SYSTEM 活动与抽奖";
    String SYSTEM_LOTTERY_LOG_TYPE_ADD_EXPRESS_TYPE = "添加快递";
    String SYSTEM_LOTTERY_LOG_TYPE_ADD_EXPRESS_SUCCESS = "添加快递信息【{{#lotteryLogReqVO.id}}】: {{#lotteryLogReqVO.trackingNumber }}";
    String SYSTEM_LOTTERY_LOG_DELETE_TYPE = "刪除活动";
    String SYSTEM_LOTTERY_LOG_DELETE_TYPE_SUCCESS = "刪除活动【{{#deleteID}}】}";
    String SYSTEM_LOTTERY_LOG_UPDATE_TYPE = "更新活动";
    String SYSTEM_LOTTERY_LOG_UPDATE_TYPE_SUCCESS = "更新活动信息【{{#lotterySettingsVO.id}}】";
    String SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE = "更新活动状态";
    String SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE_SUCCESS = "更新活动状态信息【{{#lotterySettingsVO.id}}】: {{#lotterySettingsVO.state }}】";
    String SYSTEM_LOTTERY_LOG_UPDATE_SPREED = "更新活动推广详情";
    String SYSTEM_LOTTERY_LOG_UPDATE_SPREED_SUCCESS = "更新活动推广详情";

    // ======================= SYSTEM_LOTTERY_LOG =======================
    String SYSTEM_APPLET_PAGE_MANAGEMENT_TYPE = "SYSTEM 页面装修";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_ADD_TYPE = "添加页面装修";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_ADD_SUCCESS = "添加页面装修信息";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_UPDATE_TYPE = "修改页面装修信息";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_UPDATE_SUCCESS = "修改页面装修信息";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_STATUS_UPDATE_TYPE = "修改页面装修发布状态";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_STATUS_UPDATE_SUCCESS = "修改页面装修发布状态:【{{#appletPageManagement.appletPageId}}】 : {{#appletPageManagement.appletPageStatus }}";

    String SYSTEM_APPLET_PAGE_MANAGEMENT_DELETE_TYPE = "删除页面装修信息";
    String SYSTEM_APPLET_PAGE_MANAGEMENT_DELETE_SUCCESS = "删除页面装修信息:【{{#appletPageManagement}}】";

    // ======================= SYSTEM_LOTTERY_SETTING =======================
    String SYSTEM_LOTTERY_SETTING_TYPE = "SYSTEM 活动设置";
    String SYSTEM_LOTTERY_SETTING_ADD_TYPE = "添加活动";
    String SYSTEM_LOTTERY_SETTING_ADD_SUCCESS = "添加活动信息【{{#lotterySettingsVO.activityName}}】";

}

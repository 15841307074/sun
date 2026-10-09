package com.htyoudao.youdao.module.commodity.enums;

public interface LogRecordConstans {

    //=======================连锁商品库 ======================
    String COMMODITY_TYPE = "连锁商品库";
    String COMMODITY_CATE_CREATE_SUB_TYPE = "创建分类";
    String COMMODITY_CATE_CREATE_SUCCESS = "创建了分类【{{#commodityCategory.name}}】";
    String COMMODITY_CATE_UPDATE_SUB_TYPE = "修改分类";
    String COMMODITY_CATE_UPDATE_SUCCESS = "修改了分类【{{#commodityCategory.name}}】";
    String COMMODITY_SPU_SINGLE_DOWN_SUB_TYPE = "单品下架";
    String COMMODITY_SPU_SINGLE_DOWN_SUCCESS = "单品下架【{{#singleDownReqVo.commodityId}}】";
    String COMMODITY_SPU_SINGLE_UP_SUB_TYPE = "单品下架";
    String COMMODITY_SPU_SINGLE_UP_SUCCESS = "单品下架【{{#singleUpReqVo.commodityId}}】";
    String COMMODITY_SPU_SETMEAL_UP_DOWN_SUB_TYPE = "套餐上下架";
    String COMMODITY_SPU_SETMEAL_UP_DOWN_SUCCESS = "套餐上下架 【{{#upAndDownReqVo.commodityId}}】: 【{{#upAndDownReqVo.wxStatus}}】  : 【{{#upAndDownReqVo.storeStatus}}】 ";
    String COMMODITY_SPU_CREATE_SUB_TYPE = "创建商品";
    String COMMODITY_SPU_CREATE_SUCCESS = "创建了商品【{{#commoditySpus.commodityName}}】";
    String COMMODITY_SPU_UPDATE_SUB_TYPE = "修改商品";
    String COMMODITY_SPU_UPDATE_SUCCESS = "修改了商品【{{#commoditySpus.commodityName}}】";
    String COMMODITY_SPU_UPDATE_NAME_SUB_TYPE = "修改商品名称";
    String COMMODITY_SPU_UPDATE_NAME_SUCCESS = "修改了商品名称【{{#uNameReqVo.commodityName}}】: 【{{#uNameReqVo.commodityId}}】";
    String COMMODITY_SINGLE_UP= "单品上架";
    String COMMODITY_SINGLE_UP_SUCCESS= "单品上架 {{#singleUpReqVo.singleUpId}}】: 【{{#singleUpReqVo.chooseView}}】";


    //  =======================商品模版库 ======================
    String COMMODITY_TEMP_TYPE= "商品模板";
    String COMMODITY_TEMP_CREATE_SUB_TYPE = "创建商品模板";
    String COMMODITY_TEMP_CREATE_SUCCESS = "创建商品模板【{{#commodityTemplate.commodityTemplateName}}】";
    String COMMODITY_TEMP_UPDATE_SUB_TYPE = "修改商品模板";
    String COMMODITY_TEMP_UPDATE_SUCCESS = "修改商品模板【{{#commodityTemplate.commodityTemplateName}}】";
    String COMMODITY_TEMP_DELETE_SUB_TYPE = "删除商品模板";
    String COMMODITY_TEMP_DELETE_SUCCESS = "删除商品模板 {{#commodityTemplateId}}";
    String COMMODITY_TEMP_DELETE_CATE_SUB_TYPE = "删除模板分类";
    String COMMODITY_TEMP_DELETE_CATE_SUCCESS = "删除模板分类 {{#templateCategoryId}}";
    String COMMODITY_TEMP_SPU_UPDATE_SUB_TYPE = "修改模板商品价格";
    String COMMODITY_TEMP_SPU_UPDATE_SUCCESS = "修改模板商品价格 {{#commodityTemplateId}}";
    String COMMODITY_TEMP_SPU_DELETE_SUB_TYPE = "删除模板下商品";
    String COMMODITY_TEMP_SYNC_SUB_TYPE = "同步到商品模板";
    String COMMODITY_TEMP_SPU_DELETE_SUCCESS = "删除模板下商品 {{#commodityTemplateIds}}";

    //=======================商品门店库 ======================
    String COMMODITY_STORE_TYPE ="门店商品";
    String COMMODITY_STORE_ALL_DELETE_SUB_TYPE = "删除所有门店所有商品";
    String COMMODITY_STORE_ALL_DELETE_SUCCESS = "删除所有门店所有商品";
    String COMMODITY_STORE_IN_DELETE_SUB_TYPE = "删除门店下所有商品";
    String COMMODITY_STORE_IN_DELETE_SUCCESS = "删除门店下所有商品 {{#storeId}}";
    String COMMODITY_STORE_CATE_DELETE_SUB_TYPE = "删除门店下分类";
    String COMMODITY_STORE_CATE_DELETE_SUCCESS = "删除门店下分类 {{#commodityStoreCategoryId}}";
    String COMMODITY_STORE_CATE_UPDATE_SUB_TYPE = "修改门店下分类";
    String COMMODITY_STORE_CATE_UPDATE_SUCCESS = "修改门店下分类 {{#commodityStoreCategory.commodityStoreCategoryName}}";
    String COMMODITY_STORE_SPUS_DELETE_SUB_TYPE = "批量删除门店下商品";
    String COMMODITY_STORE_SPUS_DELETE_SUCCESS = "批量删除门店下商品";
    String COMMODITY_STORE_SPU_DOWN_SUB_TYPE = "门店下单品下架";
    //String COMMODITY_STORE_SPU_DOWN_SUCCESS = "门店下单品下架【{{#commodityStoreSpu.commodityStoreSpuName}}】";
    String COMMODITY_STORE_SPU_DOWN_SUCCESS = "{{#storeName}}门店下单品下架【{{#commodityStoreSpu.commodityStoreSpuName}}】联动的套餐有【{{#packageNames}}】";
    String COMMODITY_STORE_SPU_UP_SUB_TYPE = "门店下单品上架";
    String COMMODITY_STORE_SPU_UP_SUCCESS = "{{#storeName}}门店下单品上架【{{#byId.commodityStoreSpuName}}】联动的套餐有【{{#packageNames}}】";
    String COMMODITY_STORE_SETMEAL_UP_AND_DOWN_SUB_TYPE = "门店下套餐上下架";
    String COMMODITY_STORE_SETMEAL_UP_AND_DOWN_SUCCESS = "{{#storeName}}门店下套餐上下架 【{{#commodityStoreSpu.commodityStoreSpuName}}】";
    String COMMODITY_STORE_SPU_UPDATE_SUB_TYPE = "修改门店下商品";
    String COMMODITY_STORE_SPU_UPDATE_SUCCESS = "修改门店下商品 【{{#commodityStoreSpu.commodityStoreSpuName}}】";
    String COMMODITY_STORE_SPU_BATCH_DOWN= "{{#storeName}}门店批量下架";
    String COMMODITY_STORE_SPU_BATCH_UP= "{{#storeName}}门店批量上架";


    // ======================= 活动商品 =======================
    String COMMODITY_ACTIVITY_TYPE ="活动商品";
    String COMMODITY_ACTIVITY_CREATE_SUB_TYPE = "创建活动商品";
    String COMMODITY_ACTIVITY_CREATE_SUCCESS = "创建活动商品 {{#commodityIds}}】";
    String COMMODITY_ACTIVITY_DELETE_SUB_TYPE = "删除活动商品";
    String COMMODITY_ACTIVITY_DELETE_SUCCESS = "删除活动商品 ";

    // ======================= 加购商品 =======================
    String COMMODITY_AFTER_TYPE ="加购商品";
    String COMMODITY_AFTER_CREATE_SUB_TYPE = "新增加购商品";
    String COMMODITY_AFTER_CREATE_SUCCESS = "新增加购商品 {{#afterOrder.commodityName}}";
    String COMMODITY_AFTER_UPDATE_SUB_TYPE = "修改加购商品";
    String COMMODITY_AFTER_UPDATE_SUCCESS = "修改加购商品： {{#updateObj.commodityName}}";
    String COMMODITY_AFTER_DELETE_SUB_TYPE = "删除加购商品";
    String COMMODITY_AFTER_DELETE_SUCCESS = "删除加购商品 {{#afterId}}";
    String COMMODITY_AFTER_DELETEMORE_SUCCESS = "删除多个加购商品 {{#afterIds}}";


    // ======================= 配方管理 =======================

    String COMMODITY_RECIPE_MANAGER = "商品配方管理";

    String COMMODITY_RECIPE_MANAGER_TRUNCATE_REPLACE_MATERIALS = "清空替换原料";

    String COMMODITY_RECIPE_MANAGER_TRUNCATE_REPLACE_MATERIALS_SUCCESS = "刪除配方原料 {{#truncRecipeMaterial}}";

    String COMMODITY_RECIPE_MANAGER_INSERT_MATERIALS = "增加原料";

    String COMMODITY_RECIPE_MANAGER_INSERT_MATERIALS_SUCCESS = "增加原料 {{#materials}}";

    String COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS = "保存取消选中原料";

    String COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS_SUCCESS = "保存取消选中原料 {{#materials}}";

    String COMMODITY_RECIPE_MANAGER_INSERT_RECIPE = "保存与修改配方";

    String COMMODITY_RECIPE_MANAGER_INSERT_RECIPE_SUCCESS = "保存与修改配方 {{#commodityId}}";

    // ======================= 原材料 =======================
    String RAW_MATERIAL = "原材料";

    String RAW_MATERIAL_SAVE = "保存原料信息";
    String RAW_MATERIAL_SAVE_SUCCESS = "创建原料信息【{{#rawMaterial.commodityCode}}】";


    String RAW_MATERIAL_UPDATE = "更新原料信息";
    String RAW_MATERIAL_UPDATE_SUCCESS = "更新了原料信息【{{#rawMaterial.commodityCode}}】: {_DIFF{#saveReqVO}}";

    String RAW_MATERIAL_MODIFY_STATUS = "更改原材料上下架状态";
    String RAW_MATERIAL_MODIFY_STATUS_SUCCESS = "更改原材料上下架状态：【{{#isEnable}}】";

    String RAW_MATERIAL_REMOVE = "删除原材料";
    String RAW_MATERIAL_REMOVE_SUCCESS = "删除原材料成功";


    // ======================= 盘点 =======================
    String RAW_MATERIAL_STOCKTAKE = "盘点";
    String RAW_MATERIAL_STOCKTAKE_SAVE = "保存盘点";
    String RAW_MATERIAL_STOCKTAKE_SAVE_SUCCESS = "保存盘点成功";

    String RAW_MATERIAL_STOCKTAKE_GENERATE = "生成盘点";
    String RAW_MATERIAL_STOCKTAKE_GENERATE_SUCCESS = "生成盘点成功";

    String RAW_MATERIAL_STOCKTAKE_REMARK = "设置备注";
    String RAW_MATERIAL_STOCKTAKE_REMARK_SUCCESS = "设置备注成功";


}

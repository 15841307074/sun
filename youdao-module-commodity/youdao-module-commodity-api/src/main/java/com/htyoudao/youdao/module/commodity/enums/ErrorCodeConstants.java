package com.htyoudao.youdao.module.commodity.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {




    ErrorCode COMMODITY_DATE_INCOMPLETE = new ErrorCode(1_003_000_001,"日期不完整");

    ErrorCode BASE_CATE_WITH_GROUPING = new ErrorCode(1_003_000_002,"分类已存在开启分组属性");
    ErrorCode BASE_CATE_EXISTING_NAME_CATEGORY = new ErrorCode(1_003_000_003,"分类已有该名称");
    ErrorCode BASE_CATE_HAS_PRODUCTS_CANNOT_DELETE = new ErrorCode(1_003_000_004,"分类下有商品，不允许删除");
    ErrorCode BASE_CATE_NO_PRODUCT_CATEGORY_FOUND = new ErrorCode(1_003_000_005,"分类未查询到");
    ErrorCode BASE_CATE_CATEGORY_ID_NOT_PASSED = new ErrorCode(1_003_000_006,"分类 Id 没传");
    ErrorCode BASE_CATE_CATEGORY_NAME_NOT_PASSED = new ErrorCode(1_003_000_007,"分类名称没传");


    ErrorCode BASE_SPU_PRODUCT_NAME_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_008,"商品名称不能为空");
    ErrorCode BASE_SPU_PACKAGE_TYPE_NOT_SELECTED = new ErrorCode(1_003_000_009,"商品套餐类型未选");
    ErrorCode BASE_SPU_PRODUCT_HAS_ADD_OPTION_SINGLE_SPEC = new ErrorCode(1_003_000_010,"商品已存在加购选项，只可以单规格");
    ErrorCode BASE_SPU_QUERY_CONDITION_NOT_PASSED = new ErrorCode(1_003_000_011,"商品查询条件未传");
    ErrorCode BASE_SPU_PRODUCT_ALREADY_ADDED_CANNOT_DELETE = new ErrorCode(1_003_000_012,"商品已经添加到加购商品，不允许删除");
    ErrorCode BASE_SPU_PRODUCT_USED_BY_PACKAGE_CANNOT_DELETE = new ErrorCode(1_003_000_013,"商品已被套餐使用，不允许删除");
    ErrorCode BASE_SPU_MODIFY_RULE_NOT_PASSED = new ErrorCode(1_003_000_014,"商品修改规则没传");
    ErrorCode COMMODITY_NO_PRODUCT_SELECTED = new ErrorCode(1_003_000_015,"商品未选择");
    ErrorCode BASE_SPU_IS_ALL_DAY_NOT_PASSED = new ErrorCode(1_003_000_016,"商品是否全天展示字段未转");
    ErrorCode BASE_SPU_SPU_ID_NOT_PASSED = new ErrorCode(1_003_000_017,"商品 ID 未传");
    ErrorCode BASE_SPU_PRODUCT_USED_BY_COUPON_CANNOT_DELETE = new ErrorCode(1_003_000_028,"商品已被优惠卷使用，不允许删除");
    ErrorCode BASE_SPU_PRODUCT_NUM_SMAIL = new ErrorCode(1_003_000_029,"套餐商品分组里的子品必选或默认选中数量不可以大于该分组选中数量");
    ErrorCode BASE_SPU_PRODUCT_CHANGE = new ErrorCode(1_003_000_030,"此商品正在被其他用户操作，请稍后再试");


    ErrorCode BASE_SKU_MULTIPLE_SPECIFICATION_NAMES_REQUIRED = new ErrorCode(1_003_000_018,"多规格未填写规格名");
    ErrorCode BASE_SKU_MULTIPLE_SPECIFICATION_VALUES_REQUIRED = new ErrorCode(1_003_000_019,"多规格未填写规格值");
    ErrorCode BASE_SKU_PRODUCT_MUST_HAVE_SPECIFICATION = new ErrorCode(1_003_000_020,"商品不可以没有规格信息");

    ErrorCode BASE_SINGLE_PACKAGE_AT_LEAST_TWO_PRODUCTS = new ErrorCode(1_003_000_021,"子品最少两个商品");
    ErrorCode BASE_SINGLE_SAME_GROUP_CANNOT_MULTISELECT_SAME_PRODUCT_SPEC = new ErrorCode(1_003_000_022,"子品同一分组不可以多选同一商品的同一规格");
    ErrorCode BASE_SINGLE_GROUP_NO_PRODUCTS_ADD = new ErrorCode(1_003_000_023,"分组里无子品");
    ErrorCode BASE_SINGLE_PLEASE_ADD_PRODUCTS = new ErrorCode(1_003_000_024,"没有子品");

    ErrorCode BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_025,"处理属性序列化异常");
    ErrorCode BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_001,"处理属性反序列化异常");

    ErrorCode BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_026,"处理小料序列化异常");
    ErrorCode BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_027,"处理小料反序列化异常");


    ErrorCode BASE_TAG_HANDLE_TAG_SERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_001,"处理标签序列化异常");
    ErrorCode BASE_TAG_HANDLE_TAG_DESERIALIZATION_EXCEPTION = new ErrorCode(1_003_000_002,"处理标签反序列化异常");



    ErrorCode TEMP_ID_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_003,"商品模板ID不能为空");
    ErrorCode TEMP_NAME_DUPLICATED = new ErrorCode(1_003_000_004,"商品模板名字重复");
    ErrorCode TEMP_NAME_EMPTY = new ErrorCode(1_003_000_013,"商品模板名字空");
    ErrorCode TEMP_HAS_PRODUCTS_CANNOT_DELETE = new ErrorCode(1_003_000_005,"模板下有分类不允许删除");
    ErrorCode TEMP_HAS_SPU_CANNOT_DELETE = new ErrorCode(1_003_000_013,"模板分类下有商品不允许删除");

    ErrorCode TEMP_CATE_LIST_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_006,"模板分类列表不能为空");

    ErrorCode TEMP_CATE_ERROR_CODE_CATEGORY_ID_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_007,"分类 ID 不能为空");
    ErrorCode TEMP_SKU_LIST_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_008,"商品SKU列表不能为空");
    ErrorCode TEMP_SPU_LIST_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_009,"模板SPU列表不能为空");
    ErrorCode TEMP_SPU_ID_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_010,"商品ID不能为空");
    ErrorCode TEMP_GROUP_LIST_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_011,"商品套餐分组列表不能为空");
    ErrorCode TEMP_GROUP_ITEM_LIST_CANNOT_BE_EMPTY = new ErrorCode(1_003_000_012,"商品分组单品列表不能为空");




    ErrorCode STORE_CATE_HAS_PRODUCTS= new ErrorCode(1_003_002_001,"该分类下有商品，不允许删除");
    ErrorCode STORE_SPU_ONLY_ONE_REQUIRED_GROUP_ALLOWED= new ErrorCode(1_003_002_002,"必选分组只能有一个");
    ErrorCode STORE_SPU_INCLUDES_PUBLISHED_PRODUCTS= new ErrorCode(1_003_002_003,"包含上架商品，不允许删除");
    ErrorCode STORE_SKU_PRODUCT_CANNOT_HAVE_NO_PUBLISHED_SPECIFICATIONS= new ErrorCode(1_003_002_004,"商品不可以没有上架规格");
    ErrorCode STORE_SKU_NO_HAVE= new ErrorCode(1_003_002_009,"规格列表为空");
    ErrorCode STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_TWO_ITEMS= new ErrorCode(1_003_002_005,"套餐分组需至少有2个单品");
    ErrorCode STORE_GROUP_FIXED_COLLOCATION_PACKAGE_REQUIRES_AT_LEAST_TWO_ITEMS= new ErrorCode(1_003_002_006,"固定搭配套餐至少有两个单品");
    ErrorCode STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_ONE_ITEM= new ErrorCode(1_003_002_007,"套餐分组需至少有1个单品");
    ErrorCode STORE_GROUP_NO_HAVE= new ErrorCode(1_003_002_008,"分组列表为空");
    ErrorCode STORE_SINGLE_NO_ID= new ErrorCode(1_003_002_011,"套餐里子品的原始 ID 不能为空");
    ErrorCode STORE_UP_OR_DOWN = new ErrorCode(1_003_002_013,"当前有人操作门店上下架，请稍后再试");
    ErrorCode SPU_NOT_VALUE = new ErrorCode(1_003_002_014,"未勾选商品");
    ErrorCode SPU_IS_DEL = new ErrorCode(1_003_002_015,"此商品已被删除");
    ErrorCode SPUS_IS_DEL = new ErrorCode(1_003_002_016,"有商品已被删除");
    ErrorCode BASE_SPU_LOCK_NO_PERMISSION = new ErrorCode(1_003_002_017,"%s 没有上下架操作权限，请联系运营");


    ErrorCode TAG_DUPLICATED_TAG_NAMES= new ErrorCode(1_003_003_001,"有重复标签名，请查证");
    ErrorCode TAG_HAS_COMMODITY = new ErrorCode(1_003_003_002,"已有商品使用，请勿删除");

    ErrorCode COUPON_CAN_NOT_USE = new ErrorCode(1_003_003_003,"此商品已下架");

    /**
     * 加购商品
     */
    ErrorCode AFTER_ORDER_NOT_EXISTS = new ErrorCode(1_003_004_000,"加购商品不存在");
    ErrorCode AFTER_ORDER_DUPLICATE = new ErrorCode(1_003_004_001,"加购商品已存在");
    ErrorCode AFTER_ORDER_PRICE_ERROR = new ErrorCode(1_003_004_002,"划线价格不能低于加购价格");
    ErrorCode AFTER_ORDER_TOTAL_ERROR = new ErrorCode(1_003_004_003,"加购商品超出限制");

    /**
     * 活动商品
     */
    ErrorCode ACTIVITY_NOT_EXISTS = new ErrorCode(1_003_005_000,"活动商品不存在");
    ErrorCode ACTIVITY_ALL_EXISTS = new ErrorCode(1_003_005_001,"活动商品全部重复");
    ErrorCode ACTIVITY_COMMODITY_NOT_EXISTS = new ErrorCode(1_003_005_002,"商品不存在");

    ErrorCode COMMODITY_FALLBACK_ERROR = new ErrorCode(1_003_005_003,"系统繁忙，请稍后重试");
    ErrorCode COMMODITY_BLOCK_ERROR = new ErrorCode(1_003_005_004,"系统繁忙，请重试");


    ErrorCode INVENTORY_NOT_PREPARE = new ErrorCode(1_003_006_001,"当前盘点无保存记录");

    ErrorCode MATERIAL_NOT_ZERO = new ErrorCode(1_003_006_006,"当前原材料库存不为0，不可修改");

    ErrorCode MATERIAL_STOCK_TAKE_NOT_ALLOW = new ErrorCode(1_003_006_006,"当前门店状态营业中不能操作盘点");

    ErrorCode RECIPE_COMMODITY_ID_ERROR = new ErrorCode(1_003_006_002,"商品编号缺失");

    ErrorCode RECIPE_COMMODITY_CHANGE = new ErrorCode(1_003_006_003,"商品规格已经发生改变,请刷新页面重新设置配方");

    ErrorCode RECIPE_COMMODITY_INSERT_ERROR = new ErrorCode(1_003_006_004,"配方保存异常");

    ErrorCode RECIPE_COMMODITY_INSERT_ASYNC_ERROR = new ErrorCode(1_003_006_005,"配方正在被其他用户操作，如需保存请刷新页面进行修改");

    ErrorCode TRI_RECIPE_COMMODITY_ID_ERROR = new ErrorCode(1_003_006_006,"三方商品编号缺失");

    ErrorCode TRI_RECIPE_COMMODITY_NAME_ERROR = new ErrorCode(1_003_006_007,"三方商品名称已存在");

    ErrorCode RECIPE_LIST_ERROR = new ErrorCode(1_003_006_007,"商品配方保存失败");
    /**
     * 原材料相关
     */
    ErrorCode MATERIAL_RECIPE_NOT_FOUND = new ErrorCode(1_003_007_001,"没有找到对应的配方");

    ErrorCode MATERIAL_STORE_NOT_FOUND = new ErrorCode(1_003_007_002,"门店下未找到对应的原材料");

    ErrorCode MATERIAL_RECIPE_UNIT_NOT_FOUND = new ErrorCode(1_003_007_003,"配方里面使用单位数据不存在");

    ErrorCode MATERIAL_UNIT_CONVERSION_NOT_FOUND = new ErrorCode(1_003_007_004,"数据不正确，没有找到对应的单位换算列表");

    ErrorCode MATERIAL_UNIT_CORRESPOND_NOT_FOUND = new ErrorCode(1_003_007_005,"没有找到相对应的单位");

    ErrorCode MATERIAL_STORE_CATEGORY_NOT_FOUND = new ErrorCode(1_003_007_006,"当前门店下没有对应二级类目的原材料");

    ErrorCode POINTS_PAGE_1000_NOT_E = new ErrorCode(1_003_007_007, "查询页数不能超过1000页");

}

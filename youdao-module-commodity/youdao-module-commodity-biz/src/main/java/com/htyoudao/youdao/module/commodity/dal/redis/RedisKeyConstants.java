package com.htyoudao.youdao.module.commodity.dal.redis;


/**
 * System Redis Key 枚举类
 *
 * @author 0090
 */
public interface RedisKeyConstants {

    /**
     * hash 分类
     * key storeId
     * entry key categoryId
     * entry value json
     * eg:
     * store:1:category -> {1: "智选套餐", 2: "汉堡套餐"}
     */
    String HASH_CATEGORY = "store:%s:category";


    /**
     * hash 分类下的商品
     * key storeId categoryId
     * entry key spuId
     * entry value json
     * eg:
     * store:1:category:1:product -> {1: "汉堡 111", 2: "汉堡 222"}
     */
    String HASH_PRODUCT ="store:%s:category:%s:product";



}

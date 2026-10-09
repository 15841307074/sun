package com.htyoudao.youdao.module.commodity.dal.redis;

import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import java.util.List;

/**
 * 门店商品缓存dao
 */
public interface CommodityStoreRedisDao {

    /**
     * 获取门店下的所有数据
     */
    List<StoreCategoryDTO> getAllByStoreId(Long storeId);

    /**
     * 获取商品详情数据
     * @return
     */
    SpuDto getSpuDetail(Long storeId, Long categoryId, Long spuId);

    /**
     * 清除门店缓存
     */
    void clearStoreCache(Long storeId);

    /**
     * 清除所有门店的缓存
     */
    void clearAllStoreCache();

    // -------------------- 分类操作 --------------------


    /**
     * 新增或更新分类
     */
    void saveOrUpdateCategory(Long storeId, Long categoryId, CategoryDto categoryData);

    /**
     * 删除分类 or 分类下架
     */
    void deleteCategory(Long storeId, Long categoryId);

    // -------------------- 商品操作 --------------------
    /**
     * 新增或更新商品
     */
    void saveOrUpdateProduct(Long storeId, Long categoryId, Long productId, SpuDto productData);

    /**
     * 删除商品
     */
    void deleteProduct(Long storeId, Long categoryId, Long productId);

}
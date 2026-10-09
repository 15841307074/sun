package com.htyoudao.youdao.module.commodity.dal.redis;

import static com.htyoudao.youdao.module.commodity.dal.redis.RedisKeyConstants.HASH_CATEGORY;
import static com.htyoudao.youdao.module.commodity.dal.redis.RedisKeyConstants.HASH_PRODUCT;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.module.commodity.constant.CommodityConstant;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
class StoreRedisDaoImpl implements CommodityStoreRedisDao {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<StoreCategoryDTO> getAllByStoreId(Long storeId) {
        List<CategoryDto> categoryList = getCategoryListByStoreId(storeId);
        if (CollectionUtils.isEmpty(categoryList)) {
            return Collections.emptyList();
        }
        List<StoreCategoryDTO> list = new ArrayList<>();

        for (CategoryDto categoryVO : categoryList) {
            Long categoryId = categoryVO.getCategoryId();
            StoreCategoryDTO storeProductVO = new StoreCategoryDTO();
            List<SpuDto> spuList = getSpuListByCategoryId(storeId, categoryId);
            storeProductVO.setCategory(categoryVO);
            storeProductVO.setItems(spuList);
            list.add(storeProductVO);
        }
        return list;
    }

    @Override
    public void clearStoreCache(Long storeId) {
        log.info("clearStoreCache:{}", storeId);

        // 删除该门店的分类缓存
        String categoryKey = String.format(HASH_CATEGORY, storeId);
        stringRedisTemplate.delete(categoryKey);

        // 删除该门店下所有商品缓存
        String productPattern = String.format("store:%s:category:*:product", storeId);
        deleteKeysByPattern(productPattern);
    }


    // 清除所有门店的缓存
    @Override
    public void clearAllStoreCache() {
        // 删除所有门店分类缓存
        deleteKeysByPattern("store:*:category");

        // 删除所有门店商品缓存
        deleteKeysByPattern("store:*:category:*:product");
    }


    // 使用 SCAN 命令安全删除匹配模式的键
    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = new HashSet<>();

        // 使用 SCAN 迭代获取所有匹配键
        ScanOptions options = ScanOptions.scanOptions()
            .match(pattern)
            .count(500) // 每批次扫描数量
            .build();

        try (Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
            .getConnection()
            .scan(options)) {

            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }

            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }

        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("Error closing cursor", e);
        }
    }


    private List<CategoryDto> getCategoryListByStoreId(Long storeId) {
        String categoryKey = formatCategoryKey(storeId);

        Map<Object, Object> categoryMap = stringRedisTemplate.opsForHash().entries(categoryKey);
        if (CollectionUtils.isEmpty(categoryMap)) {
            return new ArrayList<>();
        }
        List<CategoryDto> list = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : categoryMap.entrySet()) {
            CategoryDto categoryVO = JSON.parseObject(entry.getValue().toString(), CategoryDto.class);
            list.add(categoryVO);
        }
        return list;
    }


    @Override
    public SpuDto getSpuDetail(Long storeId, Long categoryId, Long spuId) {
        String key = formatProductKey(storeId, categoryId);
        Object entry = stringRedisTemplate.opsForHash().get(key, spuId.toString());
        if (entry == null) {
            return null;
        }
        SpuDto item = JSON.parseObject(entry.toString(), SpuDto.class);

        //过滤掉下架规格
        item.getSkuList().removeIf(sku -> CommodityConstant.DISABLE.equals(sku.getCommodityStoreSkuStatus()));

        //过滤掉下架属性
        item.getCommodityCondiments().removeIf(c -> Objects.equals(c.getStatus(), CommodityConstant.DISABLE));

        //过滤掉下架小料
        item.getCommodityFlavors().removeIf(c -> CollectionUtils.isEmpty(c.getFlavorValues()));

        return item;
    }


    private List<SpuDto> getSpuListByCategoryId(Long storeId, Long categoryId) {
        String key = formatProductKey(storeId, categoryId);
        Map<Object, Object> productMap = stringRedisTemplate.opsForHash().entries(key);
        if (CollectionUtils.isEmpty(productMap)) {
            return new ArrayList<>();
        }
        List<SpuDto> list = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : productMap.entrySet()) {
            SpuDto spuVO = JSON.parseObject(entry.getValue().toString(), SpuDto.class);
            list.add(spuVO);
        }
        return list;
    }


    // -------------------- 分类操作 --------------------
    @Override
    public void saveOrUpdateCategory(Long storeId, Long categoryId, CategoryDto categoryData) {

        // 1. 存储分类元数据
        String categoryKey = formatCategoryKey(storeId);
        String value = JSON.toJSONString(categoryData);
        log.info("saveOrUpdateCategory:key:{},value:{}", categoryKey, value);
        stringRedisTemplate.opsForHash().put(categoryKey, categoryId.toString(), value);
    }


    @Override
    public void deleteCategory(Long storeId, Long categoryId) {
        String categoryKey = formatCategoryKey(storeId);
        log.info("deleteCategory:key:{}", categoryKey);
        stringRedisTemplate.opsForHash().delete(categoryKey, categoryId.toString());
    }


    // -------------------- 操作商品  --------------------
    @Override
    public void saveOrUpdateProduct(Long storeId, Long categoryId, Long productId, SpuDto productData) {
        String key = formatProductKey(storeId, categoryId);
        String value = JSON.toJSONString(productData);
        log.info("saveOrUpdateProduct:key:{},value:{}", key, value);
        stringRedisTemplate.opsForHash().put(key, productId.toString(), value);
    }

    @Override
    public void deleteProduct(Long storeId, Long categoryId, Long productId) {

        //删除商品元数据
        String key = formatProductKey(storeId, categoryId);
        log.info("deleteProduct:key:{},hashKey:{}", key, productId);
        stringRedisTemplate.opsForHash().delete(key, productId.toString());
    }

    /**
     * @return 门店下分类hashkey
     */
    private String formatCategoryKey(Long storeId) {
        return String.format(HASH_CATEGORY, storeId);
    }

    /**
     * @return 分类下商品hashkey
     */
    private String formatProductKey(Long storeId, Long categoryId) {
        return String.format(HASH_PRODUCT, storeId, categoryId);
    }
}
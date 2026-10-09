package com.htyoudao.youdao.module.commodity.dal.redis;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.cloud.test.core.ut.BaseRedisUnitTest;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import jakarta.annotation.Resource;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(StoreRedisDaoImpl.class)
public class StoreRedisDaoImplTest extends BaseRedisUnitTest {

    @Resource
    StoreRedisDaoImpl storeRedisDaoImpl = new StoreRedisDaoImpl();

    @Test
    public void testGetAllByStoreId() throws Exception {
        Long storeId = 1L;
        Long categoryId = 2L;
        Long spuId = 3L;


        CategoryDto categoryVO = new CategoryDto();
        categoryVO.setCategoryId(categoryId);
        categoryVO.setCategoryName("测试分类");
        storeRedisDaoImpl.saveOrUpdateCategory(storeId, categoryId, categoryVO);

        SpuDto spuVO = new SpuDto();
        spuVO.setSpuId(spuId);
        spuVO.setSpuName("测试商品");
        storeRedisDaoImpl.saveOrUpdateProduct(storeId, categoryId, spuId, spuVO);
        List<StoreCategoryDTO> result = storeRedisDaoImpl.getAllByStoreId(storeId);

        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void testGetSpuDetail() throws Exception {
        Long storeId = 1L;
        Long categoryId = 2L;
        Long spuId = 3L;
        SpuDto spuDetail = storeRedisDaoImpl.getSpuDetail(storeId, categoryId, spuId);
        System.out.println(JSON.toJSONString(spuDetail));
    }

    @Test
    public void testClearStoreCache() throws Exception {
        storeRedisDaoImpl.clearStoreCache(Long.valueOf(1));
    }

    @Test
    public void testClearAllStoreCache() throws Exception {
        storeRedisDaoImpl.clearAllStoreCache();
    }

    @Test
    public void testSaveOrUpdateCategory() throws Exception {
        storeRedisDaoImpl.saveOrUpdateCategory(Long.valueOf(1), Long.valueOf(1), new CategoryDto());
    }

    @Test
    public void testDeleteCategory() throws Exception {
        storeRedisDaoImpl.deleteCategory(Long.valueOf(1), Long.valueOf(1));
    }

    @Test
    public void testSaveOrUpdateProduct() throws Exception {
        storeRedisDaoImpl.saveOrUpdateProduct(Long.valueOf(1), Long.valueOf(1), Long.valueOf(1), new SpuDto());
    }

    @Test
    public void testDeleteProduct() throws Exception {
        storeRedisDaoImpl.deleteProduct(Long.valueOf(1), Long.valueOf(1), Long.valueOf(1));
    }
}

//Generated with love by TestMe :) Please raise issues & feature requests at: https://weirddev.com/forum#!/testme
package com.htyoudao.youdao.module.system.service.store;

import com.htyoudao.youdao.module.system.config.GrayStoreConfig;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigResVO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreExpensesMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomConfigMapper;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 城市门店列表缓存查询测试。
 */
@ExtendWith(MockitoExtension.class)
class SystemStoreInfoCityCacheTest {

    @InjectMocks
    private SystemStoreInfoServiceImpl service;
    @Mock
    private StoreCityListCacheService cacheService;
    @Mock
    private GrayStoreConfig grayStoreConfig;
    @Mock
    private SystemStoreInfoMapper storeInfoMapper;
    @Mock
    private SystemStoreExpensesMapper storeExpensesMapper;
    @Mock
    private StoreWecomConfigMapper storeWecomConfigMapper;

    /**
     * 验证缓存命中时不会查询门店、费用和企微配置表。
     */
    @Test
    void shouldNotQueryLocalMappersWhenCacheHits() {
        StoreCityListCacheValue cacheValue = new StoreCityListCacheValue();
        cacheValue.setStoreId(1001L);
        cacheValue.setStoreName("测试门店");
        cacheValue.setCityName("沈阳市");
        cacheValue.setLongitude(123.4D);
        cacheValue.setLatitude(41.8D);
        cacheValue.setOpenStatus(1);
        cacheValue.setStoreHours("");

        when(cacheService.getStoresByCity("沈阳市"))
                .thenReturn(new ArrayList<>(List.of(cacheValue)));
        when(grayStoreConfig.getMemberIdsByStore(1001L)).thenReturn(Collections.emptySet());
        when(cacheService.getBackgroundImages(anyCollection()))
                .thenReturn(Map.of(1001L, "https://example.com/background.png"));

        StoreWecomConfigReqVO request = new StoreWecomConfigReqVO();
        request.setCityName("沈阳市");
        request.setLongitude(123.4D);
        request.setLatitude(41.8D);

        List<StoreWecomConfigResVO> result = service.storeListByCity(request);

        assertEquals(1, result.size());
        assertEquals("https://example.com/background.png", result.get(0).getStoreBackgroundImage());
        verifyNoInteractions(storeInfoMapper, storeExpensesMapper, storeWecomConfigMapper);
    }

    /**
     * 验证城市为空时直接返回空列表且不访问缓存和数据库。
     */
    @Test
    void shouldReturnEmptyWhenCityIsBlank() {
        StoreWecomConfigReqVO request = new StoreWecomConfigReqVO();
        request.setCityName(" ");

        assertTrue(service.storeListByCity(request).isEmpty());
        verifyNoInteractions(cacheService, storeInfoMapper, storeExpensesMapper, storeWecomConfigMapper);
    }

    /**
     * 验证缓存中的门店名称筛选保持数据库默认的不区分大小写语义。
     */
    @Test
    void shouldMatchStoreNameIgnoringCase() {
        StoreCityListCacheValue cacheValue = new StoreCityListCacheValue();
        cacheValue.setStoreId(1002L);
        cacheValue.setStoreName("Campus Store");
        cacheValue.setCityName("沈阳市");
        cacheValue.setLongitude(123.4D);
        cacheValue.setLatitude(41.8D);
        cacheValue.setOpenStatus(1);
        cacheValue.setStoreHours("");

        when(cacheService.getStoresByCity("沈阳市"))
                .thenReturn(new ArrayList<>(List.of(cacheValue)));
        when(grayStoreConfig.getMemberIdsByStore(1002L)).thenReturn(Collections.emptySet());
        when(cacheService.getBackgroundImages(anyCollection())).thenReturn(Collections.emptyMap());

        StoreWecomConfigReqVO request = new StoreWecomConfigReqVO();
        request.setCityName("沈阳市");
        request.setStoreName("campus");

        List<StoreWecomConfigResVO> result = service.storeListByCity(request);

        assertEquals(1, result.size());
        verifyNoInteractions(storeInfoMapper, storeExpensesMapper, storeWecomConfigMapper);
    }
}

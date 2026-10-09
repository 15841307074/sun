package com.htyoudao.youdao.module.promotion.service.activityNjnz;


import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.PromotionServerApplication;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.StockPreheatService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PromotionServerApplication.class)
class ActivitySeckillServiceImplTest {

    @Autowired
    private StockPreheatService stockPreheatService;

    @Test
    public void testQuery(){
        BusinessContextHolder.setBusinessId(10L);

        Integer time = 10;
        stockPreheatService.preheatStock(time);
    }
}
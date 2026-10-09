package com.htyoudao.youdao.module.promotion.service.activityMJ;


import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.PromotionServerApplication;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.service.activityMj.ActivityMJAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = PromotionServerApplication.class)
class ActivityMJAppServiceImplTest {

    @Autowired
    private ActivityMJAppService activityMJAppService;

    @Test
    public void testQuery(){
        BusinessContextHolder.setBusinessId(10L);

        Long storeId = 1241338966192422912L;
        List<Long> commodityIds = List.of(1985592246272724993L);
        Map<Long, List<ActivityMJDTO>> longListMap = activityMJAppService.selectMJActivity(storeId, commodityIds);
        System.out.println(longListMap);
    }
}
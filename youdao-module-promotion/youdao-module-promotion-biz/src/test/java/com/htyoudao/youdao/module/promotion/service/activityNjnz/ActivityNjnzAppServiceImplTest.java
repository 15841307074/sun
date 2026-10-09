package com.htyoudao.youdao.module.promotion.service.activityNjnz;


import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.PromotionServerApplication;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PromotionServerApplication.class)
class ActivityNjnzAppServiceImplTest {

    @Autowired
    private ActivityNjnzAppService activityNjnzAppService;

    @Test
    public void testQuery(){
        BusinessContextHolder.setBusinessId(10L);

        Long storeId = 11L;
        List<Long> commodityIds = List.of(1952250194252398593L);
        Map<Long, List<ActivityNjnzDTO>> longListMap = activityNjnzAppService.selectNjnzActivity(storeId, commodityIds);
        System.out.println(longListMap);
    }
}
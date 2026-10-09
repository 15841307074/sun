package com.htyoudao.youdao.module.order.service.order;


import com.htyoudao.youdao.module.order.OrderServerApplication;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonRequest;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonResult;
import jakarta.annotation.Resource;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrderServerApplication.class)
class BzOrderProductSonServiceImplTest {

    @Resource
    private BzOrderProductSonService bzOrderProductSonService;

    @Test
    void aggregateCommoditySales() {
        ProductSonRequest sonRequest = new ProductSonRequest();
        sonRequest.setCommodityId("111");
        List<ProductSonResult> productSonResults = bzOrderProductSonService.productSonList(sonRequest);
        System.out.println(productSonResults);
    }
}
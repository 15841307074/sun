package com.htyoudao.youdao.module.order.service.order;

import static org.junit.jupiter.api.Assertions.*;

import com.htyoudao.youdao.module.order.OrderServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrderServerApplication.class)
class BzOrderServiceImplTest {

    @Autowired
    private BzOrderService orderService;

    @Test
    void pOrderpage() {
    }

    @Test
    void getBzOrderInfo() {
        orderService.getBzOrderInfo("ORD202506121303389365003");
    }
}
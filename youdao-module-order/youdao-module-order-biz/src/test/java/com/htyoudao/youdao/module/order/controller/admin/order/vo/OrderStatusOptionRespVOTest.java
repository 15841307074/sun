package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusOptionRespVOTest {

    @Test
    void listByOrderTypes_shouldReturnAllStatuses_whenNoOrderTypeSelected() {
        List<Integer> statusCodes = getStatusCodes(List.of());

        assertTrue(statusCodes.contains(OrderStateEnum.WAITING_ACCEPT.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.ACCEPTED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.MAKING.getCode()));
    }

    @Test
    void listByOrderTypes_shouldReturnErrandStatuses_whenOnlyErrandSelected() {
        List<Integer> statusCodes = getStatusCodes(List.of(OrderTypeEnum.ERRAND.getCode()));

        assertTrue(statusCodes.contains(OrderStateEnum.CANCELED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.UNPAID.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.WAITING_ACCEPT.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.ACCEPTED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.DELIVERED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.COMPLETED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.PENDING_REFUND.getCode()));
        assertFalse(statusCodes.contains(OrderStateEnum.MAKING.getCode()));
    }

    @Test
    void listByOrderTypes_shouldExcludeErrandOnlyStatuses_whenCommonTypeSelected() {
        List<Integer> statusCodes = getStatusCodes(List.of(OrderTypeEnum.CANTEEN_FOOD.getCode()));

        assertFalse(statusCodes.contains(OrderStateEnum.WAITING_ACCEPT.getCode()));
        assertFalse(statusCodes.contains(OrderStateEnum.ACCEPTED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.MAKING.getCode()));
    }

    @Test
    void listByOrderTypes_shouldReturnAllStatuses_whenErrandAndCommonTypesSelected() {
        List<Integer> statusCodes = getStatusCodes(List.of(
                OrderTypeEnum.CANTEEN_FOOD.getCode(),
                OrderTypeEnum.ERRAND.getCode()
        ));

        assertTrue(statusCodes.contains(OrderStateEnum.WAITING_ACCEPT.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.ACCEPTED.getCode()));
        assertTrue(statusCodes.contains(OrderStateEnum.MAKING.getCode()));
    }

    private List<Integer> getStatusCodes(List<Integer> orderTypes) {
        return OrderStatusOptionRespVO.listByOrderTypes(orderTypes).stream()
                .map(OrderStatusOptionRespVO::getValue)
                .toList();
    }
}

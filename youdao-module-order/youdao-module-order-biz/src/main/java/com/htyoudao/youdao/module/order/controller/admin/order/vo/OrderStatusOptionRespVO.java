package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "管理后台 - 订单状态筛选项 Response VO")
@Data
@AllArgsConstructor
public class OrderStatusOptionRespVO {

    private static final Set<Integer> ERRAND_STATUS_CODES = Set.of(
            OrderStateEnum.CANCELED.getCode(),
            OrderStateEnum.UNPAID.getCode(),
            OrderStateEnum.WAITING_ACCEPT.getCode(),
            OrderStateEnum.ACCEPTED.getCode(),
            OrderStateEnum.DELIVERED.getCode(),
            OrderStateEnum.COMPLETED.getCode(),
            OrderStateEnum.PENDING_REFUND.getCode()
    );

    private static final Set<Integer> ERRAND_ONLY_STATUS_CODES = Set.of(
            OrderStateEnum.WAITING_ACCEPT.getCode(),
            OrderStateEnum.ACCEPTED.getCode()
    );

    private static final Set<Integer> ALL_ORDER_TYPE_CODES = Arrays.stream(OrderTypeEnum.values())
            .map(OrderTypeEnum::getCode)
            .collect(Collectors.toUnmodifiableSet());

    @Schema(description = "状态值", example = "110")
    private Integer value;

    @Schema(description = "状态名称", example = "待接单")
    private String label;

    public static List<OrderStatusOptionRespVO> listByOrderTypes(Collection<Integer> orderTypes) {
        Set<Integer> selectedOrderTypes = orderTypes == null ? Set.of() : orderTypes.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean selectedAllTypes = selectedOrderTypes.isEmpty() || selectedOrderTypes.containsAll(ALL_ORDER_TYPE_CODES);
        boolean selectedErrand = selectedOrderTypes.contains(OrderTypeEnum.ERRAND.getCode());
        boolean selectedCommonType = selectedOrderTypes.stream()
                .anyMatch(orderType -> !Objects.equals(orderType, OrderTypeEnum.ERRAND.getCode()));

        return Arrays.stream(OrderStateEnum.values())
                .filter(orderState -> includeOrderState(orderState.getCode(), selectedAllTypes, selectedErrand, selectedCommonType))
                .map(orderState -> new OrderStatusOptionRespVO(orderState.getCode(), orderState.getMessage()))
                .toList();
    }

    private static boolean includeOrderState(Integer orderState, boolean selectedAllTypes, boolean selectedErrand, boolean selectedCommonType) {
        if (selectedAllTypes) {
            return true;
        }
        if (selectedErrand && !selectedCommonType) {
            return ERRAND_STATUS_CODES.contains(orderState);
        }
        if (selectedErrand) {
            return true;
        }
        return !ERRAND_ONLY_STATUS_CODES.contains(orderState);
    }
}

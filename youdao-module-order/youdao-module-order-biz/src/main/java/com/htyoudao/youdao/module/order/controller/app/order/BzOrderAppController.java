package com.htyoudao.youdao.module.order.controller.app.order;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.*;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.BzOrderPrintInfoDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.OrderCountByOrderTypeDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.KioskReqConverter;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.KioskSubmitReqVO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderActionReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderDeliveredReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallPageRespVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallRespVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.KioskSubmitReqV2VO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import com.htyoudao.youdao.module.order.service.pay.BzOrderPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_ERROR_SOURCE;

/**
 * <p>
 * 订单
 * </p>
 */
@Tag(name = "app - 订单管理", description = "详情")
@RestController
@RequestMapping("/order/app")
public class BzOrderAppController {

    @Resource
    private BzOrderService bzOrderService;
    @Resource
    private BzOrderPayService bzOrderPayService;
    @Resource
    private RabbitMQService rabbitMQService;

    /**
     * 小程序订单列表
     */
    @Operation(summary = "小程序订单列表")
    @GetMapping("/page")
    public CommonResult<Page<BzOrderAppListRspVO>> cOrderPage(BzOrderAppListReqVO reqVO) {
        reqVO.setOpenId(null);
        if (ObjectUtils.isEmpty(reqVO.getStartTime()) || ObjectUtils.isEmpty(reqVO.getEndTime())) {
            if (reqVO.getOrderStateHistory().equals(OrderConstants.YES)) {
                //历史
                reqVO.setCreateTime(
                        new LocalDateTime[]{
                                DateUtils.localDateToLocalDateTime(LocalDate.now().minusDays(1), DateUtils.T_00_00_00),
                                DateUtils.localDateToLocalDateTime(LocalDate.now().minusDays(1), DateUtils.T_23_59_59)
                        });
            } else {
                //进行中
                reqVO.setCreateTime(
                        new LocalDateTime[]{
                                DateUtils.localDateToLocalDateTime(LocalDate.now(),
                                        DateUtils.T_00_00_00), LocalDateTime.now()
                        });
            }
        } else {
            reqVO.setCreateTime(new LocalDateTime[]{reqVO.getStartTime(), reqVO.getEndTime()});
        }
        return CommonResult.success(bzOrderService.cOrderPage(reqVO));
    }

    @PostMapping("/appPage")
    @Operation(summary = "老板助手订单列表")
    public CommonResult<PageResult<BzOrderAppListRspVO>> appPage(@RequestBody OrderPageReqVO reqVO) {
        if (ObjectUtils.isEmpty(reqVO.getStartTime()) || ObjectUtils.isEmpty(reqVO.getEndTime())) {
            if (reqVO.getOrderStateHistory().equals(OrderConstants.YES)) {
                //历史
                reqVO.setCreateTime(
                        new LocalDateTime[]{
                                DateUtils.localDateToLocalDateTime(LocalDate.now().minusDays(7), DateUtils.T_00_00_00),
                                DateUtils.localDateToLocalDateTime(LocalDate.now().minusDays(1), DateUtils.T_23_59_59)
                        });
            } else {
                //进行中
                reqVO.setCreateTime(
                        new LocalDateTime[]{
                                DateUtils.localDateToLocalDateTime(LocalDate.now(), DateUtils.T_00_00_00),
                                DateUtils.localDateToLocalDateTime(LocalDate.now(), DateUtils.T_23_59_59)
                        });
            }
        } else {
            reqVO.setCreateTime(new LocalDateTime[]{com.htyoudao.youdao.module.order.util.DateUtils.stringToLocalDateTime(reqVO.getStartTime()), com.htyoudao.youdao.module.order.util.DateUtils.stringToLocalDateTime(reqVO.getEndTime())});
        }
        // 获得用户分页列表
        PageResult<BzOrderAppListRspVO> pageResult = bzOrderService.appOrderPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }

    /**
     * 小程序订单详情
     */
    @Operation(summary = "小程序订单详情")
    @GetMapping(value = "/info")
    public CommonResult<OrderDetailRspVO> getAppInfo(@RequestParam String orderSn) {
        return CommonResult.success(bzOrderService.getBzOrderInfo(orderSn));
    }

    /**
     * 跑腿接单大厅订单详情
     */
    @Operation(summary = "跑腿接单大厅订单详情")
    @GetMapping(value = "/errand/hall/info")
    public CommonResult<OrderDetailRspVO> getErrandHallInfo(@RequestParam String orderSn) {
        OrderDetailRspVO detail = bzOrderService.getBzOrderInfo(orderSn);
        this.maskErrandHallInfoIfWaitingAccept(detail);
        return CommonResult.success(detail);
    }

    /**
     * 点餐机订单列表
     */
    @Operation(summary = "点餐机订单列表")
    @GetMapping("/kiosk/page")
    public CommonResult<Page<BzOrderListRspVO>> pcOrderPage(BzOrderReqVO reqVO) {
        reqVO.setIncludeAcceptedErrandWhenMaking(Boolean.TRUE);
        return CommonResult.success(bzOrderService.orderPage(reqVO));
    }

    /**
     * 点餐机订单详情
     */
    @Operation(summary = "点餐机订单详情")
    @GetMapping(value = "/kiosk/info")
    public CommonResult<OrderDetailRspVO> getPcInfo(@RequestParam String orderSn) {
        return CommonResult.success(bzOrderService.getBzOrderInfo(orderSn));
    }

    /**
     * 查询用户近三天订单号
     *
     * @return
     */
    @GetMapping("/selectOrderByMemberIdAndTrhDay")
    @PermitAll
    @Operation(summary = "查询用户近三天订单号")
    public CommonResult<List<String>> selectOrderByMemberIdAndTrhDay(@RequestParam("memberId") Long memberId, @RequestParam("storeId") Long storeId) {
        return CommonResult.success(bzOrderService.selectOrderByMemberIdAndTrhDay(memberId, storeId));
    }

    /**
     * 订单核销
     */
    @Operation(summary = "订单核销")
    @PostMapping("/writeOff")
    public CommonResult<String> writeOff(@RequestParam String orderSn, @RequestParam Long storeId) {
        bzOrderService.writeOff(orderSn, storeId);
        return CommonResult.success("OK");
    }

    /**
     * 订单完成
     */
    @Operation(summary = "订单完成")
    @PostMapping("/completed")
    public CommonResult<String> completed(@RequestParam String orderSn) {
        bzOrderService.completed(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * 叫号取餐
     */
    @Operation(summary = "叫号取餐")
    @PostMapping("/callNumber")
    public CommonResult<String> callNumber(@RequestParam String orderSn) {
        bzOrderService.callNumber(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * 小票扫码
     */
    @Operation(summary = "小票扫码")
    @PostMapping("/receiptScan")
    public CommonResult<String> receiptScan(@RequestParam String orderSn) {
        bzOrderService.receiptScan(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * 配送
     */
    @Operation(summary = "配送")
    @PostMapping("/delivery")
    public CommonResult<String> delivery(@RequestParam String orderSn) {
        bzOrderService.delivery(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * 跑腿接单
     */
    @Operation(summary = "跑腿接单")
    @PostMapping("/errand/accept")
    public CommonResult<String> acceptErrandOrder(@RequestBody @Valid ErrandOrderActionReqVO reqVO) {
        bzOrderService.acceptErrandOrder(reqVO.getOrderSn());
        return CommonResult.success("OK");
    }

    /**
     * 跑腿已取货
     */
    @Operation(summary = "跑腿已取货")
    @PostMapping("/errand/pickup")
    public CommonResult<String> pickupErrandOrder(@RequestBody @Valid ErrandOrderActionReqVO reqVO) {
        bzOrderService.pickupErrandOrder(reqVO.getOrderSn());
        return CommonResult.success("OK");
    }

    /**
     * 跑腿我已送达
     */
    @Operation(summary = "跑腿我已送达")
    @PostMapping("/errand/delivered")
    public CommonResult<String> deliveredErrandOrder(@RequestBody @Valid ErrandOrderDeliveredReqVO reqVO) {
        bzOrderService.deliveredErrandOrder(reqVO.getOrderSn(), reqVO.getDeliveryImages());
        return CommonResult.success("OK");
    }

    /**
     * App代取订单整单退款
     */
    @Operation(summary = "App代取订单整单退款")
    @PostMapping("/errand/full-refund")
    public CommonResult<String> errandFullRefund(@RequestBody @Valid ErrandOrderActionReqVO reqVO) {
        bzOrderPayService.errandFullRefund(reqVO.getOrderSn());
        return CommonResult.success("OK");
    }

    /**
     * App代取订单退款
     */
    @Operation(summary = "App代取订单退款")
    @PostMapping("/errand/refund")
    public CommonResult<String> errandRefund(@RequestBody @Valid ErrandRefundReqVO reqVO) {
        bzOrderPayService.errandRefund(reqVO);
        return CommonResult.success("OK");
    }

    /**
     * 跑腿接单大厅
     */
    @Operation(summary = "跑腿接单大厅")
    @PostMapping("/errand/hall")
    public CommonResult<ErrandOrderHallPageRespVO> errandOrderHall(@RequestBody @Valid ErrandOrderHallReqVO reqVO) {
        return CommonResult.success(bzOrderService.errandOrderHall(reqVO));
    }

    /**
     * 我的跑腿配送订单
     */
    @Operation(summary = "我的跑腿配送订单")
    @PostMapping("/errand/my-delivery")
    public CommonResult<ErrandOrderHallPageRespVO> myErrandDeliveryOrders(@RequestBody(required = false) ErrandOrderHallReqVO reqVO) {
        return CommonResult.success(bzOrderService.myErrandDeliveryOrders(reqVO));
    }

    private void maskErrandHallInfoIfWaitingAccept(OrderDetailRspVO detail) {
        if (detail == null || !Integer.valueOf(OrderStateEnum.WAITING_ACCEPT.getCode()).equals(detail.getOrderState())
                || detail.getDeliveryId() != null) {
            return;
        }
        detail.setTakeAwayTel(null);
        detail.setReceiverMobile(null);
        detail.setPickUpNum("****");
    }

    /**
     * 餐机统计堂食，外卖订单数量
     */
    @Operation(summary = "餐机统计堂食，外卖订单数量")
    @GetMapping("/selectCountByOrderType")
    public CommonResult<Map<String, Integer>> selectCountByOrderType(BzOrderReqVO reqVO) {
        reqVO.setIncludeAcceptedErrandWhenMaking(Boolean.TRUE);
        Map<String, Integer> returnObj = new HashMap<>();
        returnObj.put("ts", 0);
        returnObj.put("db", 0);
        returnObj.put("wm", 0);
        returnObj.put("dq", 0);
        List<OrderCountByOrderTypeDTO> list = bzOrderService.selectCountByOrderType(reqVO);
        Map<Integer, Integer> map = list.stream().collect(Collectors.toMap(OrderCountByOrderTypeDTO::getOrderType, OrderCountByOrderTypeDTO::getOrderNum));
        if (map.containsKey(0)) {
            returnObj.put("ts", map.get(0));
        }
        if (map.containsKey(1)) {
            returnObj.put("db", map.get(1));
        }
        if (map.containsKey(2)) {
            returnObj.put("wm", map.get(2));
        }
        if (map.containsKey(3)) {
            returnObj.put("dq", map.get(3));
        }
        return CommonResult.success(returnObj);
    }

    /**
     * 结算
     */
    @PostMapping("/calculate")
    public CommonResult<CalculateCacheDataV2DTO> calculate(@Valid @RequestBody SettlementReqVO reqVO) {
        //强行走v2
        return CommonResult.success(bzOrderService.calculateV2(KioskReqConverter.toV2(reqVO)));
    }

    /**
     * 结算
     */
    @PostMapping("/v2/calculate")
    public CommonResult<CalculateCacheDataV2DTO> calculateV2(@Valid @RequestBody SettlementReqV2VO reqVO) {
        return CommonResult.success(bzOrderService.calculateV2(reqVO));
    }

    /**
     * 秒杀结算
     */
    @PostMapping("/seckill/calculate")
    public CommonResult<CalculateCacheDataV2DTO> seckillCalculate(@Valid @RequestBody SettlementReqVO reqVO) {
        //强行v2
        return CommonResult.success(bzOrderService.seckillCalculateV2(KioskReqConverter.toV2(reqVO)));
    }

    /**
     * 秒杀结算
     */
    @PostMapping("/v2/seckill/calculate")
    public CommonResult<CalculateCacheDataV2DTO> seckillCalculateV2(@Valid @RequestBody SettlementReqV2VO reqVO) {
        return CommonResult.success(bzOrderService.seckillCalculateV2(reqVO));
    }

    /**
     * 小程序提交订单
     */
    @PostMapping("/submit")
    public CommonResult<SubmitResVO> submit(@Valid @RequestBody SubmitReqVO reqVO) {
        reqVO.setMemberId(WebFrameworkUtils.getLoginUserId());
        List<Integer> canUseSources = Arrays.asList(
                OrderSourceEnum.X_PAYMENT_ORDER.getCode(),
                OrderSourceEnum.X_CASH_ORDER.getCode(),
                OrderSourceEnum.SPLICING_ORDER.getCode(),
                OrderSourceEnum.TAKE_OUT_ORDER.getCode(),
                OrderSourceEnum.ERRAND_ORDER.getCode()
        );

        if (!canUseSources.contains(reqVO.getSource())) {
            throw new ServiceException(ORDER_ERROR_SOURCE);
        }

        return CommonResult.success(bzOrderService.submitOrder(reqVO));
    }

    /**
     * 小程序秒杀提交订单
     */
    @PostMapping("/seckill/submit")
    public CommonResult<SubmitResVO> seckillSubmit(@Valid @RequestBody SubmitReqVO reqVO) {
        reqVO.setMemberId(WebFrameworkUtils.getLoginUserId());
        reqVO.setSource(OrderSourceEnum.SECKILL_ORDER.getCode());
        return CommonResult.success(bzOrderService.seckillSubmit(reqVO));
    }

    /**
     * 点餐机提交订单
     */
    @PostMapping("/kiosk/submit")
    public CommonResult<SubmitResVO> kioskSubmit(@Valid @RequestBody KioskSubmitReqVO reqVO) {
        List<Integer> canUseSources = Arrays.asList(
                OrderSourceEnum.K_PAYMENT_ORDER.getCode(),
                OrderSourceEnum.K_CASH_ORDER.getCode()
        );

        if (!canUseSources.contains(reqVO.getSource())) {
            throw new ServiceException(ORDER_ERROR_SOURCE);
        }

        //强行走V2
        return CommonResult.success(bzOrderService.submitOrder(KioskReqConverter.toV2(reqVO)));
    }

    /**
     * 点餐机提交订单
     */
    @PostMapping("/v2/kiosk/submit")
    public CommonResult<SubmitResVO> kioskSubmitV2(@Valid @RequestBody KioskSubmitReqV2VO reqVO) {
        List<Integer> canUseSources = Arrays.asList(
                OrderSourceEnum.K_PAYMENT_ORDER.getCode(),
                OrderSourceEnum.K_CASH_ORDER.getCode()
        );

        if (!canUseSources.contains(reqVO.getSource())) {
            throw new ServiceException(ORDER_ERROR_SOURCE);
        }
        return CommonResult.success(bzOrderService.submitOrder(reqVO));
    }

    /**
     * 取消订单
     */
    @PutMapping("/cannel")
    public CommonResult<String> cannel(@RequestParam String orderSn) {
        bzOrderService.cannel(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * 推送
     */
    @PostMapping("/changeToGetting")
    public CommonResult<String> changeToGetting(@RequestParam String orderSn) {
        bzOrderService.changeToGetting(orderSn);
        return CommonResult.success("OK");
    }

    /**
     * MQ测试
     */
    @PermitAll
    @GetMapping("/mq/test")
    public CommonResult<String> mqTest() {
        rabbitMQService.sendMessage("exchange1922574905971654658", "", "zzzzzz");
        return CommonResult.success("OK");
    }


    /**
     * 打印订单统计小票
     *
     * @param storeId
     * @return
     */
    @GetMapping("/getOrderPrintInfo")
    @Operation(summary = "打印订单统计小票")
    public CommonResult<BzOrderPrintInfoDTO> getOrderPrintInfo(@RequestParam Long storeId) {
        return CommonResult.success(bzOrderService.getOrderPrintInfo(storeId));
    }
}

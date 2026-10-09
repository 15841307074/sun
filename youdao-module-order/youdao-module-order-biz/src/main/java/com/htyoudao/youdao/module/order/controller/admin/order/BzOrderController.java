package com.htyoudao.youdao.module.order.controller.admin.order;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandFullRefundReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandRefundReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderDetailRspVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderPageReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderResVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderStatusOptionRespVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.OrderPayReqVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import com.htyoudao.youdao.module.order.service.pay.BzOrderPayService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 订单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-07
 */
@Slf4j
@Tag(name = "管理后台 - 订单管理", description = "详情")
@RestController
@RequestMapping("/order")
public class BzOrderController {

    @Resource
    private BzOrderService bzOrderService;
    @Resource
    private BzOrderPayService bzOrderPayService;

    @PostMapping("/page")
    @Operation(summary = "获得订单分页列表")
    @PreAuthorize("@ss.hasPermission('system:order:query')")
    public CommonResult<SearchAfterPage<OrderResVO>> page(@RequestBody OrderPageReqVO pageReqVO) {
        // 获得用户分页列表
        SearchAfterPage<OrderResVO> pageResult = bzOrderService.pOrderpage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new SearchAfterPage<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }

    @GetMapping("/status-options")
    @Operation(summary = "根据订单类型获得订单状态筛选项")
    public CommonResult<List<OrderStatusOptionRespVO>> statusOptions(
            @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "orderTypes", required = false) List<Integer> orderTypes) {
        List<Integer> queryOrderTypes = new ArrayList<>();
        if (orderTypes != null) {
            queryOrderTypes.addAll(orderTypes);
        }
        if (orderType != null) {
            queryOrderTypes.add(orderType);
        }
        return success(OrderStatusOptionRespVO.listByOrderTypes(queryOrderTypes));
    }

    /**
     * PC订单详情
     *
     * @param orderSn
     * @return
     */
    @PreAuthorize("@ss.hasPermission('system:order:info')")
    @GetMapping(value = "/info")
    @Operation(summary = "PC订单详情")
    public CommonResult<OrderDetailRspVO> getOrderDetail(@RequestParam(value = "orderSn") String orderSn) {
        return success(bzOrderService.getBzOrderInfo(orderSn));
    }

    /**
     * 导出订单
     *
     * @param exportReqVO
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/export")
    @Operation(summary = "导出订单")
    @PreAuthorize("@ss.hasPermission('system:order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportOrderList(@RequestBody OrderPageReqVO exportReqVO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("==> 导出订单开始... 入参 | exportReqVO {}", exportReqVO);
        bzOrderService.exportOrderList(exportReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * PC退款
     *
     * @param reqVO
     * @return
     */
    @LogRecord(type = LOG_SYSTEM, subType = LOG_ORDER_REFUND_SUB_TYPE, bizNo = "{{#bzOrderDO.orderId}}", success = LOG_ORDER_REFUND_SUCCESS)
    @PreAuthorize("@ss.hasPermission('system:order:refund')")
    @Operation(summary = "PC订单退款")
    @PostMapping("/refund")
    public CommonResult<String> refund(@Valid @RequestBody OrderPayReqVO reqVO) {
        BzOrderDO bzOrderDO = bzOrderPayService.refund(reqVO);
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("bzOrderDO", bzOrderDO);
        return CommonResult.success("OK");
    }

    @Operation(summary = "PC代取订单整单退款")
    @PostMapping("/errand/full-refund")
    public CommonResult<String> errandFullRefund(@Valid @RequestBody ErrandFullRefundReqVO reqVO) {
        bzOrderPayService.errandFullRefund(reqVO);
        return CommonResult.success("OK");
    }

    @Operation(summary = "PC代取订单退款")
    @PostMapping("/errand/refund")
    public CommonResult<String> errandRefund(@Valid @RequestBody ErrandRefundReqVO reqVO) {
        bzOrderPayService.errandRefund(reqVO);
        return CommonResult.success("OK");
    }

    @PermitAll
    @PostMapping("/refundTest")
    public CommonResult<String> refundTest(@Valid @RequestBody OrderPayReqVO reqVO) {
        log.info("==> 测试退款开始... 入参 | reqVO {}", reqVO.getOrderSn());
        // 3. 记录操作日志上下文
        return CommonResult.success("OK");
    }

    /**
     * 补全订单商品ES
     *
     * @param orderSn
     * @param startTime
     * @param endTime
     * @return
     */
    @PermitAll
    @Operation(summary = "补全订单商品ES")
    @GetMapping("/putProductEs")
    public CommonResult<String> putProductEs(@RequestParam(value = "orderSn", required = false) String orderSn, @RequestParam(value = "startTime", required = false) String startTime, @RequestParam(value = "endTime", required = false) String endTime) {
        bzOrderService.putProductEs(orderSn, startTime, endTime);
        return CommonResult.success("OK");
    }
}

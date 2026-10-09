package com.htyoudao.youdao.module.order.controller.app.pay;

import com.alibaba.fastjson2.JSONObject;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.tracer.core.annotation.LogExecutionTime;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.OrderPayReqVO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import com.htyoudao.youdao.module.order.service.pay.BzOrderPayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.Serial;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 支付
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-11
 */
@Slf4j
@Tag(name = "app - 支付", description = "支付")
@RestController
@RequestMapping("/order/bz-order-pay")
public class BzOrderPayController {

    @Resource
    private BzOrderPayService bzOrderPayService;

    /**
     * 支付
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/doPay")
    public CommonResult<JSONObject> doPay(@Valid @RequestBody OrderPayReqVO reqVO) {
        return bzOrderPayService.doPay(reqVO);
    }

    /**
     * B扫C
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/bScanCdoPay")
    public CommonResult<JSONObject> bScanCdoPay(@Valid @RequestBody OrderPayReqVO reqVO) {
        return CommonResult.success(bzOrderPayService.bScanCdoPay(reqVO));
    }

    /**
     * 点餐机退款
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/refund")
    public CommonResult<String> refund(@Valid @RequestBody OrderPayReqVO reqVO) {
        bzOrderPayService.refund(reqVO);
        return CommonResult.success("OK");
    }

    /**
     * App代取订单整单退款
     *
     * @param reqVO
     * @return
     */
    @PostMapping("/errand/full-refund")
    public CommonResult<String> errandFullRefund(@Valid @RequestBody OrderPayReqVO reqVO) {
        bzOrderPayService.errandFullRefund(reqVO);
        return CommonResult.success("OK");
    }

    /**
     * 回调
     *
     * @param body
     * @return
     */
    @PermitAll
    @PostMapping("/notify")
    @DataPermission(enable = false)
    public Map<String, String> notify(@RequestBody Map<String, Object> body) {
        try {
            return bzOrderPayService.notify(body);
        } catch (Exception e) {

            return new HashMap<>() {
                @Serial
                private static final long serialVersionUID = 4865811853983561188L;

                {
                    put("rspCod", "");
                    put("rspMsg", "error");
                }
            };
        }
    }

    @Resource
    private BzOrderService bzOrderService;

    /**
     * 小票打印测试
     */
    //@PreAuthorize("@ss.hasPermi('printer:printer:list')")
    @GetMapping("/ssss")
    @PermitAll
    public void ssss(@Param("orderSn") String orderSn) {
        BzOrderDO bzOrderDO = bzOrderService.getBzOrderDO(orderSn);
        OrderDetailDTO detail = bzOrderService.getDetail(orderSn);
        bzOrderPayService.sendPrinter(bzOrderDO, detail.getProductDOList(),detail.getProductSonDOList(), detail.getPurchaseDOList());
    }
    /**
     * 小票打印测试
     */
    //@PreAuthorize("@ss.hasPermi('printer:printer:list')")
    @GetMapping("/sssss")
    @PermitAll
    public void sssss(@Param("orderSn") String orderSn) {
        BzOrderDO bzOrderDO = bzOrderService.getBzOrderDO(orderSn);
        OrderDetailDTO detail = bzOrderService.getDetail(orderSn);
        bzOrderPayService.sendErrandPrinter(bzOrderDO, detail.getProductDOList(),detail.getProductSonDOList(), detail.getPurchaseDOList());
    }

    @GetMapping("/simulate-print")
    @PermitAll
    public CommonResult<Integer> simulatePrint(@RequestParam Long storeId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                @RequestParam String printerSn,
                                                @RequestParam String printerBrand) {
        return CommonResult.success(bzOrderPayService.startSimulatedPrint(storeId, date, printerSn, printerBrand));
    }

    @GetMapping("/simulate-print/stop")
    @PermitAll
    public CommonResult<Integer> stopSimulatePrint() {
        return CommonResult.success(bzOrderPayService.stopAllSimulatedPrints());
    }

    @GetMapping("/log/test")
    @LogExecutionTime(value = "日志测试")
    @PermitAll
    public void logTest() {
        log.info("==> 日志测试");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.htyoudao.youdao.module.order.service.pay;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandFullRefundReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandRefundReqVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.OrderPayReqVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付 服务实现类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */
public interface BzOrderPayService extends IService<BzOrderPayDO> {

    /**
     * 支付
     *
     * @param reqVO
     * @return
     */
    CommonResult<JSONObject> doPay(OrderPayReqVO reqVO);

    /**
     * 扫码支付
     *
     * @param reqVO
     * @return
     */
    JSONObject bScanCdoPay(OrderPayReqVO reqVO);

    /**
     * 退款
     *
     * @param reqVO
     */
    BzOrderDO refund(OrderPayReqVO reqVO);

    /**
     * 已支付待接单代取订单退款，仅处理支付侧退款，不更新订单状态。
     *
     * @param order 订单
     */
    void refundWaitingAcceptErrandPayment(BzOrderDO order);

    /**
     * 代取订单整单退款，包含跑腿赏金分账撤销/退回。
     *
     * @param reqVO 退款请求
     * @return 订单
     */
    BzOrderDO errandFullRefund(ErrandFullRefundReqVO reqVO);

    /**
     * App 代取订单整单退款，包含跑腿赏金分账撤销/退回。
     *
     * @param reqVO 退款请求
     * @return 订单
     */
    BzOrderDO errandFullRefund(OrderPayReqVO reqVO);

    /**
     * 代取订单整单退款，包含跑腿赏金分账撤销/退回。
     *
     * @param orderSn 订单号
     * @return 订单
     */
    BzOrderDO errandFullRefund(String orderSn);

    /**
     * 代取订单分项退款，支持整单、餐费、赏金。
     *
     * @param reqVO 退款请求
     * @return 订单
     */
    BzOrderDO errandRefund(ErrandRefundReqVO reqVO);

    /**
     * 支付回调
     *
     * @param body
     * @return
     * @throws Exception
     */
    Map<String, String> notify(Map<String, Object> body) throws Exception;

    /**
     * 打印
     *
     * @param bzOrder
     * @param orderProducts
     * @param sons
     */
    void sendPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases);

    void sendErrandPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases);

    int startSimulatedPrint(Long storeId, LocalDate date, String printerSn, String printerBrand);

    int stopAllSimulatedPrints();
}

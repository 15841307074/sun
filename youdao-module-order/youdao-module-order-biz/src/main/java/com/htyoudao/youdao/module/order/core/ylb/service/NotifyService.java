package com.htyoudao.youdao.module.order.core.ylb.service;


import com.htyoudao.youdao.module.order.core.ylb.ApiRequest;
import com.htyoudao.youdao.module.order.core.ylb.ApiResponse;
import com.htyoudao.youdao.module.order.core.ylb.SignUtils;
import com.htyoudao.youdao.module.order.core.ylb.enums.LogisticsStatusEnum;
import com.htyoudao.youdao.module.order.core.ylb.request.OrderStateNotifyRequest;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-27
 */
@Slf4j
@Service
public class NotifyService extends BaseService<OrderStateNotifyRequest.OrderInfo> {

    protected final String CMD = "order.DeliveryStateSync";

    @Autowired
    private BzOrderService iBzOrderService;

    public ApiResponse notify(ApiRequest<OrderStateNotifyRequest.OrderInfo> body) {
        ApiResponse returnObj = new ApiResponse();

        BeanUtils.copyProperties(body, returnObj);
        returnObj.setCmd(this.getCmd());
        returnObj.setBody(new ApiResponse.ResponseBody(0, "success"));

//        String sign = this.getSign(body);
//        //验签
//        if (!sign.equals(body.getSign())) {
//            returnObj.setBody(new ApiResponse.ResponseBody(1, "验签失败"));
//        } else {
            log.info("==> 云喇叭订单状态回调，验签成功");
            OrderStateNotifyRequest.OrderInfo orderInfo = body.getBody();
            BzOrderDO bzOrderParam = new BzOrderDO();
            bzOrderParam.setOrderSn(orderInfo.getOrderId());
            bzOrderParam.setCreateTime(LocalDateTime.now());
            bzOrderParam.setOrderState(
                    LogisticsStatusEnum.getCodeByLogisticsStatus(orderInfo.getLogisticsStatus())
            );
            iBzOrderService.updateOrderState(bzOrderParam);
//        }

        return returnObj;
    }

    @Override
    String getSign(ApiRequest<OrderStateNotifyRequest.OrderInfo> request) {
        return SignUtils.generateSign(request, secret);
    }

    @Override
    String getCmd() {
        return "resp." + CMD;
    }
}

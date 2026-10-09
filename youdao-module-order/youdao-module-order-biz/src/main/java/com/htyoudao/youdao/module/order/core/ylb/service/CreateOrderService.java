package com.htyoudao.youdao.module.order.core.ylb.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.controller.orderProduct.VO.BzOrderProductDTO;
import com.htyoudao.youdao.module.order.core.ylb.ApiRequest;
import com.htyoudao.youdao.module.order.core.ylb.ApiResponse;
import com.htyoudao.youdao.module.order.core.ylb.SignUtils;
import com.htyoudao.youdao.module.order.core.ylb.request.OrderRequest;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.YlbOrderDO;
import com.htyoudao.youdao.module.order.service.order.BzOrderProductService;
import com.htyoudao.youdao.module.order.service.order.YlbOrderService;
import com.htyoudao.youdao.module.order.util.SubTableUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Slf4j
@Service
public class CreateOrderService extends BaseService<OrderRequest> {
    protected final String CMD = "order.create";
    protected final String NOTIFY_PATH = "app-api/order/ylb/state/notify";

    @Value("${hbgc.platformName}")
    private String PLATFORM_NAME;
    @Value("${hbgc.host}")
    private String HBGC_HOST;

    @Autowired
    private BzOrderProductService iBzOrderProductService;
    @Autowired
    private YlbOrderService ylbOrderService;

    public ApiRequest<OrderRequest> getApiRequest(BzOrderDO bzOrder) {

        BzOrderProductDO bzOrderProductParam = new BzOrderProductDO();
        bzOrderProductParam.setOrderSn(bzOrder.getOrderSn());
        // 查询订单货品明细列表  查询该订单有哪些商品
        LocalDateTime[] createTimes = {LocalDateTime.now().minusHours(1), LocalDateTime.now()};
        List<BzOrderProductVO> bzOrderProductList = iBzOrderProductService.getOrderProductList(Collections.singletonList(bzOrder.getOrderSn()), createTimes);

        if (ObjectUtil.isEmpty(bzOrderProductList)) {
            return null;
        }

        int count = 0;
        List<OrderRequest.Food> foods = new ArrayList<>();
        for (BzOrderProductVO i : bzOrderProductList) {
            foods.add(OrderRequest.Food.builder()
                    .name(i.getGoodsName())
                    .price(i.getGoodsShowPrice().toString())
                    .quantity(i.getGoodsNum())
                    .box_num(0)
                    .box_price(0)
                    .build());
            count += i.getGoodsNum();
        }


        long epochSecond = bzOrder.getCreateTime().atZone(ZoneId.systemDefault()).toEpochSecond();

        OrderRequest build = OrderRequest.builder()
                .source(PLATFORM_NAME)
                .shop_id(bzOrder.getStoreId().toString())
                .shop_name(bzOrder.getStoreName())
                .shop_phone(bzOrder.getStorePhone())
                .recipient_name(bzOrder.getReceiverName())
                .recipient_phone(ObjectUtil.isEmpty(bzOrder.getReceiverMobile()) ? bzOrder.getTakeAwayTel() : bzOrder.getReceiverMobile())
                .recipient_address(bzOrder.getReceiverAddress())
                //经度
                .recipient_latitude(bzOrder.getRefuseRemark())
                //纬度
                .recipient_longitude(bzOrder.getRefuseReason())
                .order_id(bzOrder.getOrderSn())
                .created_time(epochSecond)
                .updated_time(epochSecond)
                .day_seq(ThreadLocalRandom.current().nextInt(10000, 100000))
                .original_price(bzOrder.getOrderAmount().toString())
                .quantity(count)
                .paid_price(bzOrder.getPayAmount().toString())
                .remarks(bzOrder.getOrderRemark())
                .notify_url(HBGC_HOST + NOTIFY_PATH)
                .foods(foods)
                .build();
        return super.getApiRequest(build);
    }

    public ApiResponse sendOrderRequest(BzOrderDO bzOrder) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ApiRequest<OrderRequest> apiRequest = this.getApiRequest(bzOrder);

        if (ObjectUtil.isEmpty(apiRequest)) {
            log.error("==> 未查询到订单明细");
            return null;
        }

        // 设置签名
        apiRequest.setSign(this.getSign(apiRequest));

        //记录推送记录，包括入参和出参
        YlbOrderDO ylbOrder = YlbOrderDO.builder().orderSn(bzOrder.getOrderSn()).req(JSON.toJSONString(apiRequest)).build();

//        HttpEntity<ApiRequest> entity = new HttpEntity<>(apiRequest, headers);

        ApiResponse apiResponse = null;
        try {
//            apiResponse = restTemplate.postForObject(OPEN_URL, entity, ApiResponse.class);
            String response = HttpUtil.post(OPEN_URL, JSON.toJSONString(apiRequest));
            apiResponse = JSON.parseObject(response, ApiResponse.class);

            ylbOrder.setRsp(JSON.toJSONString(apiResponse));
        } catch (RestClientException e) {
            ylbOrder.setRsp(JSON.toJSONString(Collections.singletonList(e.getMessage())));
        }

        ylbOrderService.save(ylbOrder);
        return apiResponse;
    }

    @Override
    public String getSign(ApiRequest<OrderRequest> request) {
        return SignUtils.generateSign(request, secret);
    }

    @Override
    String getCmd() {
        return CMD;
    }
}
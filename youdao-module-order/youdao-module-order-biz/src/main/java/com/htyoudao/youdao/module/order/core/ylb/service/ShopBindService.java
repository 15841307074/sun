package com.htyoudao.youdao.module.order.core.ylb.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.core.ylb.ApiRequest;
import com.htyoudao.youdao.module.order.core.ylb.ApiResponse;
import com.htyoudao.youdao.module.order.core.ylb.SignUtils;
import com.htyoudao.youdao.module.order.core.ylb.request.ShopBindParam;
import com.htyoudao.youdao.module.order.core.ylb.request.ShopBindRequest;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.REQ_HASH_KEY;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Slf4j
@RefreshScope
@Service
public class ShopBindService extends BaseService<ShopBindRequest> {
    protected final String CMD = "shop.bind.msg";

    @Resource
    protected StringRedisTemplate stringRedisTemplate;
    @DubboReference
    private StoreApi storeApi;

    public ApiRequest<ShopBindRequest> getApiRequest(ShopBindParam body) {

        CommonResult<StoreDTO> commonResult = storeApi.getStoreByStoreId(Long.valueOf(body.getShopId()));
        StoreDTO storeDTO = commonResult.getCheckedData();
        if (ObjectUtils.isEmpty(storeDTO)) {
            throw exception(ORDER_GET_STORE_FAIL);
        }

        ShopBindRequest build = ShopBindRequest.builder()
                .address(storeDTO.getStoreAddress())
                .latitude(storeDTO.getStoreLatitude())
                .longitude(storeDTO.getStoreLongitude())
                .name(storeDTO.getStoreName())
                .phone(storeDTO.getStorePhone())
                .shopId(body.getShopId())
                .state(body.getState())
                .build();
        return super.getApiRequest(build);
    }

    public ApiResponse sendShopBindRequest(ShopBindParam body) {

        Object req = stringRedisTemplate.opsForHash().get(REQ_HASH_KEY, body.getShopId());
        if (ObjectUtils.isEmpty(req)) {
            throw exception(ORDER_YLB_CANNOT_USE);
        }

        if (!body.getSource().equals(source)) {
            throw exception(ORDER_YLB_ID_NOT_OWN);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ApiRequest<ShopBindRequest> apiRequest = this.getApiRequest(body);

        // 设置签名
        apiRequest.setSign(this.getSign(apiRequest));

        String response = HttpUtil.post(OPEN_URL, JSON.toJSONString(apiRequest));
        ApiResponse apiResponse = JSON.parseObject(response, ApiResponse.class);

        log.info("==> 店铺注册云喇叭响应: {}", apiResponse);
        return apiResponse;
    }

    @Override
    public String getSign(ApiRequest<ShopBindRequest> request) {
        return SignUtils.generateSign(request, secret);
    }

    @Override
    String getCmd() {
        return CMD;
    }
}
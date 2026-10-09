package com.htyoudao.youdao.module.order.controller.app.order;

import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.core.ylb.ApiRequest;
import com.htyoudao.youdao.module.order.core.ylb.ApiResponse;
import com.htyoudao.youdao.module.order.core.ylb.request.OrderStateNotifyRequest;
import com.htyoudao.youdao.module.order.core.ylb.request.ShopBindParam;
import com.htyoudao.youdao.module.order.core.ylb.service.NotifyService;
import com.htyoudao.youdao.module.order.core.ylb.service.ShopBindService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_YLB_STORE_REGISTER_FAIL;

/**
 * <p>
 * 云喇叭相关接口
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-27
 */
@RestController
@RequestMapping("order/ylb")
@Slf4j
public class YlbController {

    @Autowired
    private NotifyService notifyService;
    @Autowired
    private ShopBindService shopBindService;

    /**
     * 订单状态回调
     */
    @PermitAll
    @PostMapping("/state/notify")
    public ApiResponse notify(@ModelAttribute OrderStateNotifyRequest body) {

        ApiRequest<OrderStateNotifyRequest.OrderInfo> apiRequest = new ApiRequest<>();
        BeanUtils.copyProperties(body, apiRequest);

        return notifyService.notify(apiRequest);
    }

    /**
     * 门店映射
     */
    @PostMapping("/shop/bind")
    public CommonResult<String> notify(@Valid @RequestBody ShopBindParam body) {
        ApiResponse apiResponse = shopBindService.sendShopBindRequest(body);

        if (apiResponse == null || apiResponse.getBody() == null) {
            return CommonResult.error(ORDER_YLB_STORE_REGISTER_FAIL);
        }

        if (apiResponse.getBody().getCode() != 0) {
            return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), apiResponse.getBody().getErrMsg());
        }

        return CommonResult.success("OK");
    }

}

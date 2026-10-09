package com.htyoudao.youdao.module.commodity.controller.app.afterorder;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderAppVO;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderReqVO;
import com.htyoudao.youdao.module.commodity.service.afterorder.AfterOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "app - 订单生成后加购商品")
@RestController
@RequestMapping("/commodity/after-order/app")
@Validated
@Slf4j
public class AfterOrderAppController {

    @Resource
    private AfterOrderService afterOrderService;

    @PostMapping("/getAfterOrderList")
    @Operation(summary = "选完商品,提交订单之前获得加购商品集合")
    public CommonResult<List<AfterOrderAppVO>> getAfterOrderList(@Valid @RequestBody AfterOrderReqVO reqVO) {
        log.info("app访问加购商品接口,getAfterOrderList:{}", reqVO);
        return success(afterOrderService.getAfterOrderList(reqVO));
    }
}

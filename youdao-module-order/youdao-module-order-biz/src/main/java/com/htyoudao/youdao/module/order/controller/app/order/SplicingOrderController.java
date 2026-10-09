package com.htyoudao.youdao.module.order.controller.app.order;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SplicingOrderMemberAddReqVO;
import com.htyoudao.youdao.module.order.service.order.IBzSplicingOrderServcie;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-11-15
 */
@RestController
@RequestMapping("/order/splicing-order")
public class SplicingOrderController {

    @Resource
    private IBzSplicingOrderServcie iBzSplicingOrderServcie;

    /**
     * 拼单主体查询 (新版)
     *
     * @return
     */
//    @RepeatSubmit
    @Operation(summary = "拼单主体查询 (新版)")
    @GetMapping("/main/select")
    public CommonResult<Map<String, Object>> mainSelect(@RequestParam(value = "mainId", required = false) String mainId, @RequestParam("openId") String openId, @RequestParam("storeId") Long storeId) {
        return CommonResult.success(iBzSplicingOrderServcie.mainSelect(mainId, openId, storeId));
    }

    /**
     * 拼单选择去继续点餐 (新版)
     *
     * @return
     */
//    @RepeatSubmit
    @Operation(summary = "拼单选择去继续点餐 (新版)")
    @GetMapping("/main/continue")
    public CommonResult<String> mainContinue(@RequestParam(value = "mainId", required = false) String mainId, @RequestParam("openId") String openId, @RequestParam("storeId") Long storeId) {
        iBzSplicingOrderServcie.mainContinue(mainId, openId, storeId);
        return CommonResult.success("OK");
    }

    /**
     * 去结算返回全部商品信息 (新版)
     *
     * @return
     */
//    @RepeatSubmit
    @Operation(summary = "去结算返回全部商品信息 (新版)")
    @GetMapping("/main/getCommodity")
    public CommonResult<String> getCommodity(@RequestParam(value = "mainId", required = false) String mainId, @RequestParam(value = "openId", required = false) String openId, @RequestParam(value = "storeId", required = false) Long storeId) {
        return CommonResult.success(iBzSplicingOrderServcie.getCommodity(mainId, openId, storeId));
    }

    /**
     * 结算锁定拼单 (新版)
     *
     * @return
     */
    @Operation(summary = "结算锁定拼单 (新版)")
    @PutMapping("/main/lock")
    public CommonResult<String> mainLock(@RequestBody SplicingOrderMemberAddReqVO reqVO) {
        iBzSplicingOrderServcie.mainLock(reqVO.getMainId(), reqVO.getOpenId());
        return CommonResult.success("OK");
    }

    /**
     * 取消拼单 (新版)
     *
     * @return
     */
    @Operation(summary = "取消拼单 (新版)")
    @PutMapping("/main/cancel")
    public CommonResult<String> mainCancel(@RequestBody SplicingOrderMemberAddReqVO reqVO) {
        iBzSplicingOrderServcie.mainCancel(reqVO.getMainId(), reqVO.getOpenId());
        return CommonResult.success("OK");
    }

    /**
     * 选好商品 (新版)
     *
     * @return
     */
//    @RepeatSubmit
    @Operation(summary = "选好商品 (新版)")
    @PutMapping("/commodity/change")
    public CommonResult<String> commodityChange(@RequestBody SplicingOrderMemberAddReqVO reqVO) {
        iBzSplicingOrderServcie.commodityChange(reqVO);
        return CommonResult.success("OK");
    }

    @PermitAll
    @Operation(summary = "测试feign")
    @GetMapping("/feign/test")
    public CommonResult<String> feignTest() {
        iBzSplicingOrderServcie.feignTest();
        return CommonResult.success("OK");
    }

}

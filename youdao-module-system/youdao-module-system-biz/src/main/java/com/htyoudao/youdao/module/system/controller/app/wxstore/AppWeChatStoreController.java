package com.htyoudao.youdao.module.system.controller.app.wxstore;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreImgRespVO;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreRespVO;
import com.htyoudao.youdao.module.system.service.wxstore.StoreWecomConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "后台pc - 企业微信")
@RestController
@RequestMapping("/system/wxstore")
public class AppWeChatStoreController {

    @Resource
    private StoreWecomConfigService storeWecomConfigService;

    @GetMapping("/getQrCodeByStoreId")
    public CommonResult<AppWeChatStoreRespVO> getQrCodeByStoreId(@RequestParam(name = "storeId")  Long storeId) {
        return success(storeWecomConfigService.getQrCodeByStoreId(storeId));
    }
    @GetMapping("/getQrCodeBgByStoreId")
    @PermitAll
    public CommonResult<AppWeChatStoreImgRespVO> getQrCodeBgByStoreId(@RequestParam(name = "storeId")  Long storeId,
                                                                      @RequestParam(name = "couponId", required = false)  Long couponId,
                                                                      @RequestParam(name = "activityId", required = false)  Long activityId,
                                                                      @RequestParam(name = "packageId", required = false)  Long packageId) {
        return success(storeWecomConfigService.getQrCodeBgByStoreId(storeId, couponId, activityId, packageId));
    }
}

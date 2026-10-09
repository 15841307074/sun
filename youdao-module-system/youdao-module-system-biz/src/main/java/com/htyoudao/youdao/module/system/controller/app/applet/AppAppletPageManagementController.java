package com.htyoudao.youdao.module.system.controller.app.applet;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppAppletPageManagementReqVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementReqVO;
import com.htyoudao.youdao.module.system.service.applet.AppletPageManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.APPLET_PAGE_FALL_BACK;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.APPLET_PAGE_TIMEOUT;

/**
 * 小程序配置页面
 */
@Tag(name = "app - 小程序管理配置信息")
@RestController
@Slf4j
@RequestMapping("/system/appletPageManagement")
public class AppAppletPageManagementController {
    @Autowired
    private AppletPageManagementService appletPageManagementService;

    /**
     * 小程序获取页面
     * @param appletPageManagement AppletPageManagementReqVO
     * @return CommonResult
     */
//    @PermitAll
//    @SentinelResource(value = "getAppletPageManagementForApplet", fallback = "getAppletPageManagementForAppletFallback", blockHandler = "getAppletPageManagementForAppletExceptionHandler")
//    @PostMapping("/getAppletPageManagementForApplet")
//    @Operation(summary = "获取页面装修信息")
//    public CommonResult<Object> getAppletPageManagementForApplet(@RequestBody AppletPageManagementReqVO appletPageManagement) throws InterruptedException {
//        Object appletPageInfo = appletPageManagementService.getAppletPageManagementForApplet(appletPageManagement);
//        return success(appletPageInfo);
//    }

    @PermitAll
    @SentinelResource(value = "getAppletPageManagementForApplet")
    @PostMapping("/getAppletPageManagementForApplet")
    @Operation(summary = "获取页面装修信息新")
    public CommonResult<String> getAppletPageManagementForAppletNew(@RequestBody AppAppletPageManagementReqVO appletPageManagement) throws InterruptedException {
        String appletPageInfo = appletPageManagementService.getAppletPageManagementForAppletNew(appletPageManagement);
        return success(appletPageInfo);
    }

}

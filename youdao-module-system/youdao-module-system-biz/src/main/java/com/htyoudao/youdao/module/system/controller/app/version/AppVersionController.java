package com.htyoudao.youdao.module.system.controller.app.version;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.controller.app.version.vo.AppVersionTerminalEnum;
import com.htyoudao.youdao.module.system.controller.app.version.vo.VersionDataRequestVo;
import com.htyoudao.youdao.module.system.service.version.config.AppVersionProperties;
import com.htyoudao.youdao.module.system.service.version.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 点餐机门店操作
 * @author  lbw
 */
@Slf4j
@Tag(name = "小程序 - 获取版本号")
@RestController
@RequestMapping("/system/app-version-config")
@Validated
public class AppVersionController {

    private static final String ALIPAY_PLATFORM = "Alipay";
    private static final String ALIPAY_APPLET_VERSION = "v3.1.6";

    @Resource
    private VersionService versionService;

    @Resource
    private AppVersionProperties appVersionProperties;

    @GetMapping("/getVersion")
    @Operation(summary = "小程序版本号")
    @PermitAll
    public CommonResult<String> getVersion() {
        String version = versionService.getVersion();
        return success(version);
    }

    @GetMapping("/getSwitch")
    @Operation(summary = "小程序维护开关")
    @PermitAll
    public CommonResult<Integer> getSwitch(
            @RequestParam(value = "clientType", required = false) String clientType) {
        Integer switch0 = versionService.getSwitch0(clientType);
        return success(switch0);
    }


    @GetMapping("/selectActive")
    @Operation(summary = "0090版本号")
    @PermitAll
    public CommonResult<VersionDataRequestVo> selectActive() {
        VersionDataRequestVo versionCode = versionService.selectActive();
        return success(versionCode);
    }

    @GetMapping("/queryVersion")
    @Operation(summary = "按端枚举查询版本号（businessId后端自动获取）")
    @PermitAll
    public CommonResult<String> queryVersion(@RequestParam("terminal") AppVersionTerminalEnum terminal,
                                             @RequestHeader(value = "platform", required = false) String platform) {
        log.info("[queryVersion] platform = {}", platform);
        if (terminal == AppVersionTerminalEnum.APPLET && ALIPAY_PLATFORM.equalsIgnoreCase(platform)) {
            return success(ALIPAY_APPLET_VERSION);
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        AppVersionProperties.VersionNode versionNode = appVersionProperties.getBusiness().get(businessId);
        if (versionNode == null) {
            versionNode = appVersionProperties.getDefaults();
        }
        if (versionNode == null) {
            return success("");
        }
        if (terminal == AppVersionTerminalEnum.ORDER_MACHINE) {
            return success(versionNode.getOrderMachine());
        }
        return success(versionNode.getApplet());
    }

}

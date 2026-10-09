package com.htyoudao.youdao.module.system.controller.admin.appversion;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.appversion.vo.AppVersionConfigUpdateReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.appversion.AppVersionConfigDO;
import com.htyoudao.youdao.module.system.service.appversion.AppVersionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - APP版本配置")
@RestController
@RequestMapping("/system/app-version-config")
@Validated
public class AppVersionConfigController {
    private final AppVersionConfigService service;

    public AppVersionConfigController(AppVersionConfigService service) {
        this.service = service;
    }

    @GetMapping("/list")
    @Operation(summary = "查询六个指定项目的版本配置列表")
    public CommonResult<List<AppVersionConfigDO>> list() {
        return success(service.list());
    }

    @PutMapping("/update")
    @Operation(summary = "修改版本配置")
    public CommonResult<Boolean> update(@Valid @RequestBody AppVersionConfigUpdateReqVO req) {
        service.update(req);
        return success(true);
    }
}

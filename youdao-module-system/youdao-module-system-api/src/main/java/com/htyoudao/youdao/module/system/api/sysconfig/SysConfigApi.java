package com.htyoudao.youdao.module.system.api.sysconfig;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.sysconfig.dto.ClientSysConfigDTO;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Tag(name = "RPC 服务 - 系统配置")
public interface SysConfigApi {

    CommonResult<Map<String, String>> getBykeys(@RequestParam(value = "keyList", required = false) List<String> keyList);

    CommonResult<Boolean> editConfig(@RequestBody ClientSysConfigDTO sysConfig);
}

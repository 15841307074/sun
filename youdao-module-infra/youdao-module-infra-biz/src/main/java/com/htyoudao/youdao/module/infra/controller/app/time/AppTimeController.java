package com.htyoudao.youdao.module.infra.controller.app.time;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.controller.app.time.vo.AppTimeRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 系统时间")
@RestController
@RequestMapping("/infra/time")
@Slf4j
public class AppTimeController {

    @GetMapping("/current-time")
    @Operation(summary = "获取系统当前时间")
    @PermitAll
    public CommonResult<AppTimeRespVO> getCurrentTime() {
        return success(new AppTimeRespVO().setCurrentTime(System.currentTimeMillis()));
    }

}

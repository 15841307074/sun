package com.htyoudao.youdao.module.system.controller.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * details
 *
 * @author liuzhaowang
 */
@Tag(name = "健康检查")
@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    @Operation(summary = "健康检查")
    @PermitAll
    public String healthCheck() {
        return "ok";
    }
}

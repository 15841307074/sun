package com.alibaba.csp.sentinel.dashboard.controller.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * details
 *
 * @author liuzhaowang
 */
@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    public String healthCheck() {
        return "ok";
    }
}

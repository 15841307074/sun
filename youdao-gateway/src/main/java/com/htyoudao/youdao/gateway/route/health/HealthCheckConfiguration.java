package com.htyoudao.youdao.gateway.route.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * details
 *
 * @author liuzhaowang
 */
@Configuration
public class HealthCheckConfiguration {
    @Bean
    public RouterFunction<ServerResponse> healthCheckRoute() {
        return route(GET("/health-check"),
                request -> ServerResponse.ok().bodyValue("ok")
        );
    }
}

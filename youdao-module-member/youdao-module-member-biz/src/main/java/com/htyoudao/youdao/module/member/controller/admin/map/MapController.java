package com.htyoudao.youdao.module.member.controller.admin.map;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.service.appmap.AppMapService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "我的 - 地图")
@RestController
@RequestMapping("/member/map")
@Validated
public class MapController {

    private final AppMapService appMapService;

    public MapController(AppMapService appMapService) {
        this.appMapService = appMapService;
    }

    @GetMapping("/v3/geocoder")
    public CommonResult<String> geocoderV3(@RequestParam String lon, @RequestParam String lat) {
        return CommonResult.success(appMapService.geocoderV3(lon, lat));
    }

    @GetMapping("/v3/suggestion")
    public CommonResult<String> suggestionV3(@RequestParam String keyword,
                                             @RequestParam(required = false) String region) {
        return CommonResult.success(appMapService.suggestionV3(keyword, region));
    }
}

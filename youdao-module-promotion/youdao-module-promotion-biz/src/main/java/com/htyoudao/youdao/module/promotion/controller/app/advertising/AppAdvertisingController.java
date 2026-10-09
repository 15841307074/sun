package com.htyoudao.youdao.module.promotion.controller.app.advertising;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.AdvertisingRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.AdvertisingVO;
import com.htyoudao.youdao.module.promotion.controller.app.advertising.VO.AdvertisingConfigReqVo;
import com.htyoudao.youdao.module.promotion.service.advertising.AdvertisingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Tag(name = "小程序 - 广告配置页面")
@RestController
@RequestMapping("/promotion/advertising")
public class AppAdvertisingController {

    @Autowired
    private AdvertisingService advertisingService;


    @Operation(summary = "广告配置查询")
    @PermitAll
    @PostMapping("/appletGETAdvertising")
    public CommonResult<List<AdvertisingConfigReqVo> > appletGetAdvertisingNew(@Valid @RequestBody AdvertisingVO advertisingVO){
        List<AdvertisingConfigReqVo>  integerListMap = advertisingService.appletGetAdvertisingNew(advertisingVO);
        return CommonResult.success(integerListMap);
    }


}

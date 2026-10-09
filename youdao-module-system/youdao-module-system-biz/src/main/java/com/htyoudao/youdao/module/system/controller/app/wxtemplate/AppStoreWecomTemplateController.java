package com.htyoudao.youdao.module.system.controller.app.wxtemplate;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateDetailReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplatePageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateReqVO;
import com.htyoudao.youdao.module.system.service.wxtemplate.StoreWecomTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 企业微信模版")
@RestController
@RequestMapping("/system/wxstore/template")
public class AppStoreWecomTemplateController {



    @Resource
    private StoreWecomTemplateService storeWecomTemplateService;
    @Schema(description = "获取符合条件的模版")
    @GetMapping("/getStoreIdByTemplate")
    public CommonResult<String> getStoreIdByTemplate(@RequestParam(value = "storeId", required = true) Long storeId)
    {
        return success(storeWecomTemplateService.getStoreIdByTemplate(storeId));
    }




}

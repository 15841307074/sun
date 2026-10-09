package com.htyoudao.youdao.module.system.controller.admin.wxtemplate;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.StoreWecomConfigReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateDetailReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplatePageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateReqVO;
import com.htyoudao.youdao.module.system.service.wxstore.StoreWecomConfigService;
import com.htyoudao.youdao.module.system.service.wxtemplate.StoreWecomTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 企业微信模版")
@RestController
@RequestMapping("/system/wxstore-template")
public class StoreWecomTemplateController {



    @Resource
    private StoreWecomTemplateService storeWecomTemplateService;
    @Schema(description = "新增企业微信模版")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody StoreWecomTemplateReqVO storeWecomTemplateReqVO)
    {
        return success(storeWecomTemplateService.insertstoreWecomTemplate(storeWecomTemplateReqVO));
    }

    @Schema(description = "更新企业微信模版")
    @PostMapping("/update")
    public CommonResult<Integer> update(@RequestBody StoreWecomTemplateReqVO storeWecomTemplateReqVO)
    {
        return success(storeWecomTemplateService.updatestoreWecomTemplate(storeWecomTemplateReqVO));
    }


    @GetMapping("/remove")
    @Operation(summary = "企业微信模版删除")
    public CommonResult<Integer> remove(@RequestParam(value = "id", required = false) long id) {
        return success(storeWecomTemplateService.remove(id));
    }

    @GetMapping("/getByDetail")
    @Operation(summary = "获取模版详情")
    public CommonResult<StoreWecomTemplateDetailReqVO> getByDetail(@RequestParam(value = "id", required = false) long id) {
        return success(storeWecomTemplateService.getByDetail(id));
    }


    @GetMapping("/getList")
    @Operation(summary = "获取模版列表")
    public CommonResult<List<StoreWecomTemplatePageRespVO>> getList() {
        return success(storeWecomTemplateService.getList());
    }

    @GetMapping("/isRelease")
    @Operation(summary = "发布")
    public CommonResult<Integer> isRelease(@RequestParam("id")Long id,@RequestParam("status")Integer status) {
        return success(storeWecomTemplateService.isRelease(id,status));
    }


}

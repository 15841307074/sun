package com.htyoudao.youdao.module.system.controller.admin.wxstore;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoresUpdateVO;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomImgDO;
import com.htyoudao.youdao.module.system.service.wxstore.StoreWecomConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 企业微信")
@RestController
@RequestMapping("/system/wxstore-config")
//@Validated
public class StoreWecomConfigController {

    @Resource
    private StoreWecomConfigService storeWecomConfigService;

    @GetMapping(value = "/{id}")
    public CommonResult<StoreWecomConfigRespVO> getInfo(@PathVariable("id") Long id)
    {
        return success(storeWecomConfigService.selectStoreWecomConfigById(id));
    }

    @Schema(description = "【店铺企业微信】")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:wxstore-config:addOrUpdate')")
    public CommonResult<Integer> add(@RequestBody StoreWecomConfigReqVO storeWecomConfig)
    {
        return success(storeWecomConfigService.insertStoreWecomConfig(storeWecomConfig));
    }

    @Schema(description = "【店铺企业微信】 删除二维码")
    @DeleteMapping("/deleteQR/{id}")
    @PreAuthorize("@ss.hasPermission('system:wxstore-config:addOrUpdate')")
    public CommonResult<Integer> delete(@PathVariable("id") Long id)
    {
        return success(storeWecomConfigService.deleteStoreWecomConfig(id));
    }

    @PostMapping(value = "/getStoreWecomPage")
    public CommonResult<PageResult<StoreWecomConfigRespVO>> getStoreWecomPage(@RequestBody StoreWecomPageReqVO storeWecomConfig)
    {
        PageResult<StoreWecomConfigRespVO> pageResult =storeWecomConfigService.getStoreWecomPage(storeWecomConfig);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }
    @PutMapping("/updateCodeType")
    @Operation(summary = "批量修改标签类型")
    public CommonResult<Boolean> updateCodeType(@RequestBody StoresCodeUpdateVO storeUpdateVO) {
        storeWecomConfigService.updateCodeType(storeUpdateVO);
        return success(true);
    }
    @Schema(description = "【社区领卷图片】")
    @PostMapping("/communityImgAdd")
    public CommonResult<Integer> communityImgAdd(@RequestBody StoreWecomImgReqVO storeWecomImgReqVO)
    {
        return success(storeWecomConfigService.communityImgAdd(storeWecomImgReqVO));
    }
    @Schema(description = "【社区领卷图片详情】")
    @PostMapping("/communityImgDetail")
    public CommonResult<StoreWecomImgDO> communityImgDetail()
    {
        return success(storeWecomConfigService.communityImgDetail());
    }
}

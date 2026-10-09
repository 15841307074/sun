package com.htyoudao.youdao.module.system.controller.app.store;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreDetailResVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSaveReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.DC.StoreInfoDCRespVo;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 点餐机门店操作
 * @author  Villky
 */
@Tag(name = "点餐机 - 门店")
@RestController
@RequestMapping("/system/DC/store")
@Validated
public class DCStoreInfoController {
    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @GetMapping("/getInfo")
    @Operation(summary = "点餐机获得门店详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<StoreInfoDCRespVo> getStoreDetailForDc(@RequestParam("id") Long id) {
        StoreInfoDCRespVo storeInfoDCRespVo = systemStoreInfoService.getStoreDetailForDc(id);
        return success(storeInfoDCRespVo);
    }


    @PutMapping("/updateStoreInfo")
    @Operation(summary = "点餐机修改门店")
    public CommonResult<Boolean> updateStore(@Valid @RequestBody StoreSaveReqVO reqVO) {

        systemStoreInfoService.updateStoreByDC(reqVO);
        return success(true);
    }
}
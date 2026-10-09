package com.htyoudao.youdao.module.system.controller.admin.store;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSelectVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendResVO;
import com.htyoudao.youdao.module.system.service.store.SysStoreExtendService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 店铺关系表
 * </p>
 *
 * @author zhangjihe
 * @since 2024-05-08
 */
@Tag(name = "商户号配置")
@RestController
@RequestMapping("/system/sys-store-extend")
public class SysStoreExtendController {

    @Resource
    private SysStoreExtendService sysStoreExtendService;
    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    /**
     * 查询商户号
     *
     * @param reqVO
     * @return
     */
    @Operation(summary = "商户号列表")
    @DataPermission(enable = false)
    @GetMapping("/list")
    public CommonResult<Page<SysStoreExtendResVO>> list(SysStoreExtendReqVO reqVO) {
        Page<SysStoreExtendResVO> sysStoreExtendPage = sysStoreExtendService.listPage(reqVO);
        return CommonResult.success(sysStoreExtendPage);
    }

    @Operation(summary = "商户号新增，修改")
    @DataPermission(enable = false)
    @PostMapping("/createOrUpdate")
    public CommonResult<String> createOrUpdate(@Valid @RequestBody SysStoreExtendReqVO reqVO) {
        sysStoreExtendService.createOrUpdate(reqVO);
        return CommonResult.success("OK");
    }

    /**
     * 门店下拉
     *
     * @return
     */
    @Operation(summary = "门店下拉")
    @DataPermission(enable = false)
    @GetMapping("/store/select")
    public CommonResult<List<StoreSelectVO>> list(@RequestParam(value = "storeName", required = false) String storeName) {
        return CommonResult.success(systemStoreInfoService.getAllStoreInfo(storeName));
    }

    @Deprecated
    @GetMapping("/matchingField")
    public CommonResult<String> matchingField() {
        sysStoreExtendService.matchingField();
        return CommonResult.success("OK");
    }

}

package com.htyoudao.youdao.module.commodity.controller.admin.sync;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityBaseToStoreSyncReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityCacheReloadReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityTemplateToStoreSyncReqVO;
import com.htyoudao.youdao.module.commodity.service.job.JobService;
import com.htyoudao.youdao.module.commodity.service.sync.CommoditySyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 同步接口
 */
@Tag(name = "管理后台 - 商品同步")
@RestController
@RequestMapping("/commodity/sync")
public class CommoditySyncController {

    @Resource
    private JobService jobService;

    @Resource
    private CommoditySyncTaskService commoditySyncTaskService;

    @GetMapping("/page/list")
    @Operation(summary = "同步记录")
    public CommonResult<PageResult<CommoditySyncListRespVO>> syncPageList(@Valid CommoditySyncPageReqVO reqVO) {
        return success(commoditySyncTaskService.syncPageList(reqVO));
    }

    @GetMapping("/store/list")
    @Operation(summary = "根据批次号查询门店记录列表")
    public CommonResult<List<CommoditySyncStoreReqVO>> listByBatchNo(String batchNo) {
        return success(commoditySyncTaskService.listByBatchNo(batchNo));
    }


    /**
     * 同步连锁商品到门店接口
     */
    @PostMapping("/base/store")
    @Operation(summary = "同步连锁商品到门店接口")
    @PreAuthorize("@ss.hasPermission('commodity:sync:base2store')")
    public CommonResult<String> baseToStore(@RequestBody @Valid CommodityBaseToStoreSyncReqVO syncVO) {
        String batchNo = commoditySyncTaskService.baseToStore(syncVO);
        return CommonResult.success(batchNo);
    }


    /**
     * 同步模板商品到门店
     */
    @PostMapping("/template/store")
    @Operation(summary = "同步模板商品到门店")
    @PreAuthorize("@ss.hasPermission('commodity:sync:template2store')")
    public CommonResult<String> templateToStore(@RequestBody @Valid CommodityTemplateToStoreSyncReqVO syncVO) {
        String batchNo = commoditySyncTaskService.templateToStore(syncVO);
        return CommonResult.success(batchNo);
    }


    @Operation(summary = "门店商品缓存重置")
    @PostMapping("/cache/reload")
    @PreAuthorize("@ss.hasPermission('commodity:sync:cacheReload')")
    public CommonResult<Boolean> commodityCacheReloadHandler(@RequestBody @Valid CommodityCacheReloadReqVO reqVO) {
        jobService.commodityCacheReloadHandler(reqVO.getStoreIds(), reqVO.getBusinessId());
        return success(true);
    }
}

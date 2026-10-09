package com.htyoudao.youdao.module.promotion.controller.admin.activityMz;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzInventoryChangeReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzSaveReqVO;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzCacheService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzInventoryReconcileService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MZ_IS_NOT_ID;

/**
 * 后台pc - 营销活动满赠
 */
@Tag(name = "后台pc - 营销活动满赠")
@RestController
@RequestMapping("/promotion/activityMz")
public class ActivityMzController {

    @Resource
    private ActivityMzService activityMzService;

    @Resource
    private ActivityMzCacheService activityMzCacheService;

    @Resource
    private ActivityMzInventoryReconcileService inventoryReconcileService;

    // ==================== 活动 CRUD ====================

    @PostMapping("/create")
    @Operation(summary = "创建满赠活动")
    public CommonResult<Boolean> createActivityMz(@Valid @RequestBody ActivityMzSaveReqVO activitySaveReqVO) {
        activityMzService.createActivityMz(activitySaveReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "修改满赠活动")
    public CommonResult<Boolean> updateActivityMz(@Valid @RequestBody ActivityMzSaveReqVO activitySaveReqVO) {
        if (activitySaveReqVO.getId() == null) {
            throw exception(MZ_IS_NOT_ID);
        }
        if (activitySaveReqVO.getActivityId() == null) {
            throw exception(MZ_IS_NOT_ID);
        }
        activityMzService.updateActivityMz(activitySaveReqVO);
        return success(true);
    }

    @GetMapping("/delete")
    @Operation(summary = "删除满赠活动")
    public CommonResult<Boolean> deleteActivityMz(@RequestParam(name = "id") Long id) {
        activityMzService.deleteActivityMz(id);
        return success(true);
    }

    @GetMapping("/selectInfo")
    @Operation(summary = "查询满赠活动详情")
    public CommonResult<ActivityMzInfoRespVO> selectInfo(@RequestParam("id") Long id) {
        ActivityMzInfoRespVO respVO = activityMzService.selectInfo(id);
        return success(respVO);
    }

    // ==================== 库存管理 ====================

    @PreAuthorize("@ss.hasPermission('promotion:activityMz:inventory')")
    @PostMapping("/inventory/change")
    @Operation(summary = "赠品库存增减（扣减/回滚）")
    public CommonResult<ActivityMzCacheService.InventoryChangeResult> changeInventory(
            @Valid @RequestBody ActivityMzInventoryChangeReqVO reqVO) {
        ActivityMzCacheService.InventoryChangeResult result = activityMzCacheService.changeGiftInventory(
                reqVO.getActivityId(), reqVO.getStoreId(), reqVO.getGiftCommodityId(), reqVO.getChange());
        return success(result);
    }

    @PreAuthorize("@ss.hasPermission('promotion:activityMz:query')")
    @GetMapping("/inventory/query")
    @Operation(summary = "查询赠品库存（共用模式）")
    @Parameters({
            @Parameter(name = "activityId", description = "活动ID", required = true),
            @Parameter(name = "giftCommodityId", description = "赠送商品ID（不传则返回所有赠品库存）")
    })
    public CommonResult<?> querySharedInventory(@RequestParam("activityId") Long activityId,
                                                @RequestParam(value = "giftCommodityId", required = false) Long giftCommodityId) {
        if (giftCommodityId != null) {
            Integer inventory = activityMzCacheService.querySharedInventory(activityId, giftCommodityId);
            return success(inventory);
        }
        Map<String, Integer> allInventory = activityMzCacheService.queryAllSharedInventory(activityId);
        return success(allInventory);
    }

    @PreAuthorize("@ss.hasPermission('promotion:activityMz:query')")
    @GetMapping("/inventory/queryStore")
    @Operation(summary = "查询赠品库存（独立门店模式）")
    @Parameters({
            @Parameter(name = "activityId", description = "活动ID", required = true),
            @Parameter(name = "storeId", description = "门店ID（不传则返回所有门店库存）"),
            @Parameter(name = "giftCommodityId", description = "赠送商品ID（不传则返回该门店所有赠品库存）")
    })
    public CommonResult<?> queryStoreInventory(@RequestParam("activityId") Long activityId,
                                               @RequestParam(value = "storeId", required = false) Long storeId,
                                               @RequestParam(value = "giftCommodityId", required = false) Long giftCommodityId) {
        if (storeId != null && giftCommodityId != null) {
            // 查指定门店指定赠品
            Integer inventory = activityMzCacheService.queryStoreInventory(activityId, storeId, giftCommodityId);
            return success(inventory);
        }
        if (storeId != null) {
            // 查指定门店所有赠品
            Map<String, Integer> storeInventory = activityMzCacheService.queryAllStoreInventory(activityId, storeId);
            return success(storeInventory);
        }
        // 查所有门店所有赠品
        Map<Long, Map<String, Integer>> allStoreInventory = activityMzCacheService.queryAllStoreInventoryBatch(activityId);
        return success(allStoreInventory);
    }

    /**
     * 供周期任务调用的无Token只读对账接口。activityId不传时对账全部有限库存活动。
     * 接口只返回差异，不自动修改Redis或MySQL。
     */
    @PermitAll
    @DataPermission(enable = false)
    @GetMapping("/inventory/reconcile")
    @Operation(summary = "满赠Redis/MySQL库存对账（无需Token，只读）")
    public CommonResult<ActivityMzInventoryReconcileService.ReconcileResult> reconcileInventory(
            @RequestParam(value = "activityId", required = false) Long activityId) {
        return success(inventoryReconcileService.reconcile(activityId));
    }
}

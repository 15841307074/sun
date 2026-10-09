package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.NoPurchaseDetailReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStoreStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.NoPurchaseDetailResp;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.StoreStatsResp;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.WarehouseStatsResp;
import com.htyoudao.youdao.module.analysis.service.PurchaseStatsService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Set;

@RestController("adminPurchaseStatsController")
@RequestMapping("/analysis/admin/purchase")
public class PurchaseStatsController {

    @Resource
    private PurchaseStatsService service;

    @DubboReference
    private StoreApi storeApi;

    /**
     * 1) 按仓库维度统计
     */
    @PostMapping("/warehouse/stats")
    public CommonResult<WarehouseStatsResp> warehouseStats(@RequestBody @Valid PurchaseStatsReq req) {
        this.putStoreIds(req);
        return CommonResult.success(service.warehouseStats(req));
    }

    @PostMapping("/warehouse/stats/export")
    public CommonResult<String> exportWarehouseStats(@RequestBody @Valid PurchaseStatsReq req) {
        this.putStoreIds(req);
        service.exportWarehouseStats(req);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 2) 按门店维度统计（简单分页）
     */
    @PostMapping("/store/stats")
    public CommonResult<StoreStatsResp> storeStats(@RequestBody @Valid PurchaseStoreStatsReq req) {
        this.putStoreIds(req);
        return CommonResult.success(service.storeStats(req));
    }

    @PostMapping("/store/stats/export")
    public CommonResult<String> exportStoreStats(@RequestBody @Valid PurchaseStoreStatsReq req) {
        this.putStoreIds(req);
        service.exportStoreStats(req);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    /**
     * 3) 十天内未进货门店详情
     */
    @PostMapping("/no-purchase-10d/detail")
    public CommonResult<NoPurchaseDetailResp> noPurchase10dDetail(@RequestBody @Valid NoPurchaseDetailReq req) {
        this.putStoreIds(req);
        return CommonResult.success(service.noPurchase10dDetail(req));
    }

    private void putStoreIds(PurchaseStatsReq req) {
        if (!CollectionUtils.isEmpty(req.getStoreIds())) {
            return;
        }
        Set<Long> storeIdSet = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId()).getCheckedData();
        req.setStoreIds(new ArrayList<>(storeIdSet));
    }
}

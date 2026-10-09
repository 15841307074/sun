package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.app.vo.req.NoPurchaseDetailReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.req.PurchaseStoreStatsReq;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.NoPurchaseDetailResp;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.StoreStatsResp;
import com.htyoudao.youdao.module.analysis.controller.app.vo.resp.WarehouseStatsResp;

public interface PurchaseStatsService {

    WarehouseStatsResp warehouseStats(PurchaseStatsReq req);

    StoreStatsResp storeStats(PurchaseStoreStatsReq req);

    void exportWarehouseStats(PurchaseStatsReq req);

    void exportStoreStats(PurchaseStoreStatsReq req);

    NoPurchaseDetailResp noPurchase10dDetail(NoPurchaseDetailReq req);
}

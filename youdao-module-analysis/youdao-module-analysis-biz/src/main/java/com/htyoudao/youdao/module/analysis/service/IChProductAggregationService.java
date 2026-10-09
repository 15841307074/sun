package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductPageDownloadExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;

import java.util.List;

/**
 * 商品分页聚合查询 —— ClickHouse 数据源
 */
public interface IChProductAggregationService {

    /**
     * 从 ClickHouse 查询商品分页数据（与 productPage 功能一致，数据来源改为 CH）
     */
    PageResult<ProductResult> queryProductPageFromCh(ProductPageRequest request);

    /** 商品明细下载（CH 数据源）：导出范围/表头/行 VO 与 productPageDownload 一致；
     *  差异：主查询走 CH 矩阵 SQL、事件 UV 批量化、isSingle=3 无售后单过滤、复购窄口径、含 90 天校验 */
    List<ProductPageDownloadExcelVO> productPageDownloadFromCh(ProductPageRequest request);
}

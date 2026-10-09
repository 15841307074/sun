package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductPageDownloadExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import java.util.List;
import java.util.Map;


public interface IProductAggerationService {

    PageResult<ProductResult> getProductPage(ProductPageRequest request);

    /**
     * 游标分页查询商品数据
     * 使用 Elasticsearch composite 聚合实现高效的深度分页
     * 适用于大数据量查询和数据导出场景，解决传统分页 from + size > 10000 的限制
     *
     * @param request 商品分页请求参数，需包含 afterKey 用于游标定位
     * @return 商品分页结果，包含 afterKey 用于下次查询
     */
    PageResult<ProductResult> getProductPageByCursor(ProductPageRequest request);

    PageResult<ProductResult> realTimeProductPage(ProductPageRequest request);

    /**
     * 经营分析-商品明细下载
     * 按商品+门店维度导出当前筛选结果
     */
    List<ProductPageDownloadExcelVO> productPageDownload(ProductPageRequest request);

    /**
     * 报表下载使用，按门店返回商品聚合结果
     */
    Map<Long, List<ProductResult>> getProductDownloadDataByStore(ProductPageRequest request);

    /**
     * 根据门店ID分组查询
     * @param request 产品页面请求参数
     * @return 门店聚合结果列表
     */
    PageResult<AnalysisTopVO> getStoreAggregation(ProductPageRequest request,String groupField);

}

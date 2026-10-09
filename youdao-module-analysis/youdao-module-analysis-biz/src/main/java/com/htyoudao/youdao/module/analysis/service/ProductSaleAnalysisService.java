package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleDataDTO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleQueryReq;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleStatVO;

import java.util.List;

public interface ProductSaleAnalysisService {

    /**
     * 查询单品销量排行
     */
    List<ProductSaleStatVO> querySingleProductSaleRank(ProductSaleQueryReq req);

    /**
     * 批量写入商品销售数据到ClickHouse
     *
     * @param dataList 商品销售数据列表
     */
    void batchInsertProductSaleData(List<ProductSaleDataDTO> dataList);
}

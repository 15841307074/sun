package com.htyoudao.youdao.module.promotion.service.analysis;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChoosedReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataAnalysisReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataDownloadReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;

import java.util.List;
import java.util.Set;

public interface CouponDataAnalysisService {

    CouponDataAnalysisRespVO getDataAnalysis(Long id);


    PageResult<CouponDataAnalysisRespVO> getDataByStoreAndOrg(CouponDataAnalysisReqVO couponDataAnalysisReqVO);

    /**
     * 导出
     * @param couponDataAnalysisReqVO couponDataAnalysisReqVO
     */
    void exportListByStoreAndOrg(CouponDataAnalysisReqVO couponDataAnalysisReqVO);

    List<CouponDataAnalysisRespVO> queryCouponData(CouponDataAnalysisReqVO couponDataAnalysisReqVO);

    PageResult<CouponDataAnalysisRespVO> getDataBySource(Long id);


    /**
     * 优惠券使用数据下载
     * @param couponDataDownloadReqVO couponDataDownloadReqVO
     */
    void couponDataDownload(CouponDataDownloadReqVO couponDataDownloadReqVO);

    /**
     * 按渠道分析汇总
     * @param couponDataDownloadReqVO
     */
    void couponDataDownloadBySource(CouponDataDownloadReqVO couponDataDownloadReqVO);
}

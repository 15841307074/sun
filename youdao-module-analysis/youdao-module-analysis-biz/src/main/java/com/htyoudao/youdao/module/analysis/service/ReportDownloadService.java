package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportOrderDownloadReqVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;

import java.util.List;

/**
 * @author dht
 */
public interface ReportDownloadService {

    /**
     * 订单下载
     * @param requestVO requestVO
     */
    void orderDownload(ReportOrderDownloadReqVO requestVO);
}

package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.StoreExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportProductDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportStoreDownloadReqVO;
import jakarta.validation.Valid;
import java.util.List;

public interface IAggDownService {

    List<StoreExcelRespVO> storeDownload(@Valid ReportStoreDownloadReqVO requestVO);

    List<ProductExcelRespVO> productDownload(@Valid ReportProductDownloadReqVO requestVO);

}

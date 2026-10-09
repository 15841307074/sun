package com.htyoudao.youdao.module.infra.service.download;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.download.FileDownloadDO;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-09
 */
public interface DownloadService {

    Long create(FileDownloadReqVO fileDownloadReqVO);

    void update(FileDownloadReqVO fileDownloadReqVO);

    PageResult<FileDownloadDO> getDownloadPage(FileDownloadPageReqVO pageReqVO);
}

package com.htyoudao.youdao.module.infra.service.download;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.download.FileDownloadDO;
import com.htyoudao.youdao.module.infra.dal.mysql.download.FileDownloadMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-09
 */
@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Resource
    private FileDownloadMapper fileDownloadMapper;

    @Override
    public Long create(FileDownloadReqVO fileDownloadReqVO) {
        FileDownloadDO fileDownloadDO = new FileDownloadDO();
        BeanUtils.copyProperties(fileDownloadReqVO, fileDownloadDO);
        fileDownloadMapper.insert(fileDownloadDO);
        return fileDownloadDO.getId();
    }

    @Override
    public void update(FileDownloadReqVO fileDownloadReqVO) {
        FileDownloadDO fileDownloadDO = new FileDownloadDO();
        BeanUtils.copyProperties(fileDownloadReqVO, fileDownloadDO);
        fileDownloadMapper.updateById(fileDownloadDO);
    }

    @Override
    public PageResult<FileDownloadDO> getDownloadPage(FileDownloadPageReqVO pageReqVO) {
        return fileDownloadMapper.selectPage(pageReqVO);
    }
}

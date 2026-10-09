package com.htyoudao.youdao.module.infra.dal.mysql.download;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.infra.controller.admin.download.vo.FileDownloadPageReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.download.FileDownloadDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-09
 */
@Mapper
public interface FileDownloadMapper extends BaseMapperX<FileDownloadDO> {

    default PageResult<FileDownloadDO> selectPage(FileDownloadPageReqVO reqVO) {
        LambdaQueryWrapperX<FileDownloadDO> query = new LambdaQueryWrapperX<FileDownloadDO>()
                .eq(FileDownloadDO::getCreator, SecurityFrameworkUtils.getLoginUsername())
                .betweenIfPresent(FileDownloadDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(FileDownloadDO::getCreateTime);
        return selectPage(reqVO, query);
    }
}

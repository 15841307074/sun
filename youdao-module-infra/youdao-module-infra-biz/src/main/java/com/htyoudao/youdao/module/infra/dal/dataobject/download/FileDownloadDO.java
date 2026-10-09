package com.htyoudao.youdao.module.infra.dal.dataobject.download;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serializable;

/**
 * <p>
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@TableName("system_file_download")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileDownloadDO extends BusinessBaseDO {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String fileName;

    private String fileUrl;

    private String failedReason;

    private Integer status;
}

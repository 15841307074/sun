package com.htyoudao.youdao.module.infra.controller.admin.download.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@Data
public class FileDownloadReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4956360336627701850L;
    private Long id;

    private String fileName;

    private String fileUrl;

    private String failedReason;

    private Integer status;

    private Long businessId;

    private String creator;
}

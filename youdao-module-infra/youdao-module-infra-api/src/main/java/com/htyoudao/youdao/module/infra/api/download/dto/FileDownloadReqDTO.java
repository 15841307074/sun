package com.htyoudao.youdao.module.infra.api.download.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String fileName;

    private String fileUrl;

    private String failedReason;

    private Integer status;

    private Long businessId;

    private String creator;
}

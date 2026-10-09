package com.htyoudao.youdao.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DownStatusEnum {

    /**
     * 下载中
     */
    DOWNLOADING(0),
    /**
     * 成功
     */
    SUCCESS(1),
    /**
     * 失败
     */
    FAILD(2);

    private final Integer status;

}

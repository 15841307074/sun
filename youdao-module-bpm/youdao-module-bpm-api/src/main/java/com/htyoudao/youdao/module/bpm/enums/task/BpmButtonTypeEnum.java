package com.htyoudao.youdao.module.bpm.enums.task;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import com.htyoudao.youdao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BpmButtonTypeEnum {

    REAPPROVE(1, "进行中"),
    APPROVE(0, "审批通过");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

    BpmButtonTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}

package com.htyoudao.youdao.module.system.controller.app.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
public class SystemUserPermissionsVO {
    /**
     * 1 单门店 2 多门店
     */
    private int storeType;
    /**
     * 跳转页面
     */
    private int type;
    private Long userId;
}

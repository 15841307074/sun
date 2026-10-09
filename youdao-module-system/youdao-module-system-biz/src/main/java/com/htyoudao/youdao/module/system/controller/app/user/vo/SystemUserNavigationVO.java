package com.htyoudao.youdao.module.system.controller.app.user.vo;

import lombok.Data;

@Data
public class SystemUserNavigationVO {
    /**
     * 是否多门店
     */
    private Boolean isManyStore = false;
    /**
     * 是否有管理权限
     */
    private Boolean isManagement = false;
    /**
     * 是否有数据权限
     */
    private Boolean isData = false;
    /**
     * 是否有订单权限
     */
    private Boolean isOrder = false;
    /**
     * 是否有进货权限
     */
    private Boolean isPurchase = false;
    /**
     * 是否有OA权限
     */
    private Boolean isOA = false;
    /**
     * 我的2栏还是4
     */
    private Boolean isMyPermissions = false;
}

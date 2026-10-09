package com.htyoudao.youdao.module.system.enums.org;

/**
 * @author dht
 * 用户组织类型
 */
public interface OrgUserTypeConstants {

    /**
     * 负责人
     */
    Integer ORG_USER_TYPE_PRINCIPAL = 1;

    /**
     * 普通关系
     */
    Integer ORG_USER_TYPE_NORMAL = 0;

    /**
     * 组织  用户不可见
     */
    Integer ORG_USER_NOT_VISIBLE = 0;

    /**
     * 组织  用户可见
     */
    Integer ORG_USER_VISIBLE = 1;
}

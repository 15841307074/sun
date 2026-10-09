package com.htyoudao.youdao.module.system.api.business.dto;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目 DO
 *
 * @author 零零玖零
 */

@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDTO implements Serializable {


    private Long id;
    /**
     * 项目编码;hk,ts
     */
    private String code;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 经营方式 0 自营 1 合作
     */
    private Integer manageType;
    /**
     * 状态;0-未启用 1-启用
     */
    private Integer status;
    /**
     * 有效期开始时间
     */
    private LocalDateTime validityStartTime;
    /**
     * 有效期结束时间
     */
    private LocalDateTime validityEndTime;
    /**
     * logo url
     */
    private String logoUrl;

    /**
     * 点餐登录页背景
     */
    private String dcUrl;

    /**
     * 描述
     */
    private String comment;
    /**
     * 菜单ID，多个逗号分隔
     */
    private String menuIds;
    /**
     * 项目负责人 id
     */
    private Long userId;

}
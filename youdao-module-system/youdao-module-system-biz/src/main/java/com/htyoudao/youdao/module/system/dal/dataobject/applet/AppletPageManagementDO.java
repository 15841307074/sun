package com.htyoudao.youdao.module.system.dal.dataobject.applet;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.time.LocalDateTime;

import static com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants.APPLET_PAGE_ALL_MANAGEMENT;
import static com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants.APPLET_PAGE_MANAGEMENT;

@TableName("applet_page_management")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppletPageManagementDO extends BusinessBaseDO {

    @TableId(value = "applet_page_id", type = IdType.ASSIGN_ID)
    private Long appletPageId;
    //模版名称

    private String appletPageName;
    //小程序页面位置

    private Integer appletPageLocation;
    //小程序发布状态

    private Integer appletPageStatus;
    //小程序页面内容

    private String appletPageInfo;

    public String appletMainKey(){

        return APPLET_PAGE_ALL_MANAGEMENT + this.getBusinessId() ;
    }

    public String appletKey(){

        return APPLET_PAGE_MANAGEMENT + this.getAppletPageLocation() ;
    }
    /**
     * 应用范围 1 按门店 2 按标签
     */
    private Integer appScope;

    /**
     * 1 全部门店 2 部分门店
     */
    private Integer storeScope;

    /**
     * 发布时间
     */
    private LocalDateTime releaseTime;

    /**
     * 兜底状态
     */
    private Integer guaranteeFlag;


}

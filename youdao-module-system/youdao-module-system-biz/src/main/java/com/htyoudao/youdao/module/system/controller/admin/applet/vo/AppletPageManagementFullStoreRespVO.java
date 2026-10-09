package com.htyoudao.youdao.module.system.controller.admin.applet.vo;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppletPageManagementFullStoreRespVO extends BusinessBaseDO {

    @Schema(description = "id")
    private Long appletPageId;
    //模版名称
    @Schema(description = "模版名称")
    private String appletPageName;
    //小程序页面位置
    @Schema(description = "小程序页面位置")
    private Integer appletPageLocation;
    //小程序发布状态
    @Schema(description = "小程序发布状态")
    private Integer appletPageStatus;
    //小程序页面内容
    @Schema(description = "小程序页面内容")
    private String appletPageInfo;

    @Schema(description = "兜底状态")
    private Integer guaranteeFlag;

    @Schema(description = "发布时间")
    private LocalDateTime releaseTime;

    @Schema(description = "应用范围 1 按门店 2 按标签")
    private Integer appScope;

    @Schema(description = "1 全部门店 2 部分门店")
    private Integer storeScope;

    @Schema(description = "部分门店集合")
    private List<FullStoreRespVO> storeList;

    @Schema(description = "标签集合")
    private List<Long> tagList;

    private int totalStore;

    private List<String> storeNameList;

}

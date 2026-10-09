package com.htyoudao.youdao.module.system.controller.admin.applet.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.system.enums.AppletPageLocationEnum;
import com.htyoudao.youdao.module.system.enums.applet.AppScopeEnum;
import com.htyoudao.youdao.module.system.enums.applet.StoreScopeEnum;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppletPageManagementReqVO  {

    @Schema( description = "模板ID")
    private Long appletPageId;

    @Schema( description = "模版名称")
    private String appletPageName;

    @Schema( description = "小程序页面位置")
    @InEnum(AppletPageLocationEnum.class)
    private Integer appletPageLocation;

    @Schema( description = "小程序发布状态")
    private Integer appletPageStatus;

    @Schema( description = "小程序页面内容")
    private String appletPageInfo;

    @Schema(name = "createTime")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(name = "updateTime")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime updateTime;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "修改人名称")
    private String updateUserName;

    @Schema(description = "应用范围 1 按门店 2 按标签")
    @InEnum(AppScopeEnum.class)
    private Integer appScope;

    @Schema(description = "1 全部门店 2 部分门店")
    @InEnum(StoreScopeEnum.class)
    private Integer storeScope;

    @Schema(description = "部分门店集合")
    private List<AppletPageManagementStoreReqVO> storeList;

    @Schema(description = "标签集合")
    private List<Long> tagList;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "门店ID")
    private Long storeId;

}

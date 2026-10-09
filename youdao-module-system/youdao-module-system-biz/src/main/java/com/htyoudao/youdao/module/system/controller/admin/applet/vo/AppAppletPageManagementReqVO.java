package com.htyoudao.youdao.module.system.controller.admin.applet.vo;

import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.system.enums.AppletPageLocationEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppAppletPageManagementReqVO {

    @Schema( description = "小程序页面位置")
    @InEnum(AppletPageLocationEnum.class)
    private Integer appletPageLocation;

    @Schema( description = "门店ID")
    private Long storeId;

}

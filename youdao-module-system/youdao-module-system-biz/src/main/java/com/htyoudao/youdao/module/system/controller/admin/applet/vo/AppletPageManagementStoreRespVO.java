package com.htyoudao.youdao.module.system.controller.admin.applet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppletPageManagementStoreRespVO {

    @Schema( description = "模板门店ID")
    private Long storeId;

    @Schema( description = "模版门店名称")
    private String storeName;

}

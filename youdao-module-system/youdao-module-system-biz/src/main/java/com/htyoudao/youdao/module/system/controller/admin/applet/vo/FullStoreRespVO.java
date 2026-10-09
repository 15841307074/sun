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
public class FullStoreRespVO {

    @Schema(description = "storeId")
    private Long storeId;

    //门店名称
    @Schema(description = "门店名称")
    private String storeName;

}

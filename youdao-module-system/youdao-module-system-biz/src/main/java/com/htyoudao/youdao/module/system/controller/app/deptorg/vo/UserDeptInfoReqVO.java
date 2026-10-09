package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 老板助手全部人员 + 部门 Response VO")
public class UserDeptInfoReqVO extends PageParam {

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "1")
    private String nameOrMobile;
}

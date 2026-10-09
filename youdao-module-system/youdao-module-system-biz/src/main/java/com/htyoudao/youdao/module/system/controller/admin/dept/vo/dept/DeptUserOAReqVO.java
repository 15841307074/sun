package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "管理后台 - oa的获取用户信息 Request VO")
public class DeptUserOAReqVO extends PageParam {

    @Schema(description = "部门id")
    private Long deptId;

    @Schema(description = "名字或手机号")
    private String nameOrMobile;

    @Schema(description = "是否是主管 0是 1不是")
    private Integer type;
}

package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RoleUserResVo {
    @Schema(description = "项目id", example = "项目id")
    private List<BusinessSimpleRespVO> businessList;
    @Schema(description = "角色id", example = "角色id")
    private Long roleId;
}

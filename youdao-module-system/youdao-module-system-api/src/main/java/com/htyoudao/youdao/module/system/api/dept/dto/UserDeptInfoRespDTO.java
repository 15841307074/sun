package com.htyoudao.youdao.module.system.api.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author dht
 */
@Data
public class UserDeptInfoRespDTO implements Serializable {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "1")
    private String username;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "1")
    private String nickname;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "1")
    private String mobile;

    /**
     * 部门列表
     */
    @Schema(description = "部门列表", example = "1")
    private List<DeptRespDTO> depts;

    /**
     * 门店列表
     */
    @Schema(description = "门店列表", example = "1")
    private List<StoreRespDTO> stores;
}

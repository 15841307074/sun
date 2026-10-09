package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(description = "管理后台 - oa的获取用户信息 Resp VO")
public class DeptUserOAPageRespVO {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "用户名")
    private String nickname;

    @Schema(description = "用户名")
    private String mobile;

    private List<DeptRespVO> depts;
}

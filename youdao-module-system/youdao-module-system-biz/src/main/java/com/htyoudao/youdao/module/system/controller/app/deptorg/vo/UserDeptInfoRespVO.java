package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 老板助手登录人的部门/门店信息 Response VO")
public class UserDeptInfoRespVO implements Serializable {

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
    private List<DeptRespVO> depts;

    /**
     * 门店列表
     */
    @Schema(description = "门店列表", example = "1")
    private List<StoreResVO> stores;
    @Schema(description = "对应项目List")
    private List<BusinessSimpleRespVO> businessList = new ArrayList<>();
}

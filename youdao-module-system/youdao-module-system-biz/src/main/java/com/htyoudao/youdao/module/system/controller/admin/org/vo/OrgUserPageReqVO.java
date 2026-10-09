package com.htyoudao.youdao.module.system.controller.admin.org.vo;


import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构/门店添加负责人弹窗分页 Request VO")
@Data
public class OrgUserPageReqVO extends PageParam {

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "组织id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotNull(message = "组织id不能为空")
    private Long orgId;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private Long storeId;

    private Long roleId;

    @Schema(description = "选中的组织id 0是查询无组织用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private Long checkedOrgId;

    @Schema(description = "电话")
    private String mobile;
}

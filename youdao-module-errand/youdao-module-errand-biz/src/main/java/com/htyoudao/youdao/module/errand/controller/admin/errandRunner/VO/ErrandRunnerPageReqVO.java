package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 跑腿员分页查询请求 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跑腿员分页查询请求参数")
public class ErrandRunnerPageReqVO extends PageParam {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "门店 ID")
    private Long storeId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "组织 ID")
    private Long orgId;

    @Schema(description = "审核状态：0待审核 1通过 2失败")
    private Integer auditStatus;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "封禁状态：0正常 1封禁")
    private Integer banStatus;
}

package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 骑手入驻申请资料变更记录 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入驻申请资料变更记录")
public class ErrandRunnerApplyChangeLogVO extends BusinessBaseDO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "审核状态 0待审核 1通过 2失败")
    private Integer auditStatus;

    @Schema(description = "修改批次号")
    private String batchNo;

    @Schema(description = "变化字段编码")
    private String changeField;

    @Schema(description = "变化字段中文名")
    private String changeFieldName;

    @Schema(description = "旧值")
    private String oldValue;

    @Schema(description = "新值")
    private String newValue;
}

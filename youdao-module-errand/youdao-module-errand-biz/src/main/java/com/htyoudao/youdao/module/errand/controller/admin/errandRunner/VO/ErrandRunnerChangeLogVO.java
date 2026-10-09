package com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
@Data
@Schema(description = "跑腿员变更记录 VO")
public class ErrandRunnerChangeLogVO extends BusinessBaseDO {

    @Schema(description = "主键 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "跑腿员 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long runnerId;

    @Schema(description = "会员 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

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

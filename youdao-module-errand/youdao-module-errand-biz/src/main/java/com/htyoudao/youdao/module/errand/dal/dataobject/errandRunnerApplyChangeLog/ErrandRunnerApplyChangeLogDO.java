package com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerApplyChangeLog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 骑手入驻申请资料变更记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "bz_errand_runner_apply_change_log", autoResultMap = true)
public class ErrandRunnerApplyChangeLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "跑腿员 ID")
    private Long runnerId;

    @Schema(description = "会员 ID")
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

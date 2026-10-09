package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店记录明细快照分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoreInspectionRecordItemPageReqVO extends PageParam {

    @Schema(description = "记录明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4595")
    private Long id;

    @Schema(description = "所属巡店记录ID", example = "15985")
    private Long recordId;

    @Schema(description = "关联原始点检项ID", example = "19631")
    private Long checklistId;

    @Schema(description = "检点项目大类id", example = "19631")
    private Long typeId;

    @Schema(description = "检点项目大类名称", example = "19631")
    private String typeName;

    @Schema(description = "标题快照")
    private String titleSnap;

    @Schema(description = "提示快照")
    private String promptSnap;

    @Schema(description = "点检标准快照")
    private String standardSnap;

    @Schema(description = "该项满分快照")
    private Integer maxScoreSnap;

    @Schema(description = "图片上传规则快照")
    private Boolean imgStateSnap;

    @Schema(description = "应传图片数量快照")
    private Integer imgCountSnap;

    @Schema(description = "检查结果：0 不合格, 1 合格, 2 不适用, 3 没批", example = "2")
    private Boolean actualStatus;

    @Schema(description = "实际得分")
    private Integer actualScore;

    @Schema(description = "奖惩金额")
    private BigDecimal rewardAmount;

    @Schema(description = "巡店人备注")
    private String actualComment;

    @Schema(description = "图片地址(多图用逗号或JSON数组存储)")
    private String actualImages;

    @Schema(description = "是否已整改：0 否, 1 是")
    private Boolean isRectified;

    @Schema(description = "整改完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime[] rectifyTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime[] createTime;


    @Schema(description = """
            图片规则
            {
            "imgFlag":"0 不允许上传 1 允许上传",
            "imgState":"0 不必传 1 合格时必传 2 不合格时必传 3 不适用时必传",
            "imgCount":"1",
            "localImgFlag":"0 不允许本地上传 1 允许本地上传"
            }
            """)
    private String imgRule;

    @Schema(description = """
            描述规则
            {
            "descriptionFlag":"0 不允许填写 1 允许填写",
            "qualified":"0 选填 1 必填",
            "unqualified":"0 选填 1 必填",
            "inapplicability":"0 选填 1 必填"
            }
            """)
    private String descriptionRule;

    @Schema(description = """
            不适用规则
            {
            "showFlag":"0 不显示 1显示"
            }
            """)
    private String inapplicabilityRule;

    @Schema(description = """
            奖惩规则
            {
            "rewardPunishmentFlag":"0 不填 1填写",
            "qualified":"0 选填 1 必填",
            "unqualified":"0 选填 1 必填"
            }
            """)
    private String rewardPunishmentRule;

}
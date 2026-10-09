package com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 巡店记录明细快照 DO
 *
 * @author 超级管理员
 */
@TableName("system_store_inspection_record_item")
@KeySequence("system_store_inspection_record_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionRecordItemDO extends BusinessBaseDO {

    /**
     * 记录明细ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属巡店记录ID
     */
    private Long recordId;

    /**
     * 检点项目大类id
     */
    private Long typeId;

    /**
     * 检点项目大类名称
     */
    private String typeName;

    /**
     * 关联原始点检项ID
     */
    private Long checklistId;
    /**
     * 标题快照
     */
    private String titleSnap;
    /**
     * 提示快照
     */
    private String promptSnap;
    /**
     * 点检标准快照
     */
    private String standardSnap;
    /**
     * 该项满分快照
     */
    private Integer maxScoreSnap;

    /**
     * 检查结果：0 不合格, 1 合格, 2 不适用, 3 没批
     */
    private Integer actualStatus;
    /**
     * 实际得分
     */
    private Integer actualScore;
    /**
     * 奖惩金额
     */
    private BigDecimal rewardAmount;
    /**
     * 巡店人备注
     */
    private String actualComment;
    /**
     * 图片地址(多图用逗号或JSON数组存储)
     */
    private String actualImages;
    /**
     * 是否已整改：0 否, 1 是
     */
    private Boolean isRectified;
    /**
     * 整改完成时间
     */
    private LocalDateTime rectifyTime;


    /**
     * 图片规则
     * {
     * "imgFlag":"0 不允许上传 1 允许上传",
     * "imgState":"0 不必传 1 合格时必传 2 不合格时必传 3 不适用时必传",
     * "imgCount":"1",
     * "localImgFlag":"0 不允许本地上传 1 允许本地上传"
     * }
     */
    private String imgRule;

    /**
     * 描述规则
     * {
     * "descriptionFlag":"0 不允许填写 1 允许填写",
     * "qualified":"0 选填 1 必填",
     * "unqualified":"0 选填 1 必填",
     * "inapplicability":"0 选填 1 必填"
     * }
     */
    private String descriptionRule;

    /**
     * 不适用规则
     * {
     * "showFlag":"0 不能选择不适用 1可以选择不适用"
     * }
     */
    private String inapplicabilityRule;

    /**
     * 奖惩规则
     * {
     * "rewardPunishmentFlag":"0 不填 1填写",
     * "qualified":"0 选填 1 必填",
     * "unqualified":"0 选填 1 必填"
     * }
     */
    private String rewardPunishmentRule;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 大类排序
     */
    private Integer typeSort;
}
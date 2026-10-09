package com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 巡店记录主 DO
 *
 * @author 超级管理员
 */
@TableName("system_store_inspection_record")
@KeySequence("system_store_inspection_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionRecordDO extends BusinessBaseDO {

    /**
     * 巡店记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 门店名称(快照)
     */
    private String storeName;
    /**
     * 使用的模板ID
     */
    private Long templateId;
    /**
     * 模板名称(快照)
     */
    private String templateName;
    /**
     * 状态：0 进行中, 1 已完成, 2 已失效
     */
    private Integer status;
    /**
     * 巡店人ID
     */
    private Long inspectorId;
    /**
     * 巡店人姓名
     */
    private String inspectorName;
    /**
     * 巡店人电话
     */
    private String inspectorPhone;
    /**
     * 是否在范围内 0在 1不在
     */
    private Boolean inScope;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 理论结束时间 = 开始时间+1天
     */
    private LocalDateTime endTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime overTime;
    /**
     * 模板预设满分
     */
    private Integer totalScore;
    /**
     * 实际得分
     */
    private Integer actualScore;
    /**
     * 得分率
     */
    private BigDecimal scoreRate;
    /**
     * 点检项目总数
     */
    private Integer itemCount;
    /**
     * 合格数
     */
    private Integer qualifiedCount;
    /**
     * 不合格数
     */
    private Integer unqualifiedCount;
    /**
     * 不适用数
     */
    private Integer notApplicableCount;
    /**
     * 奖惩金额
     */
    private BigDecimal rewardAmount;
    /**
     * 门店巡店位置
     */
    private String storePatrolPosition;

    /**
     * 照片
     */
    private String storePic;

    /**
     * 整改意见
     */
    private String rectificationOpinion;
}
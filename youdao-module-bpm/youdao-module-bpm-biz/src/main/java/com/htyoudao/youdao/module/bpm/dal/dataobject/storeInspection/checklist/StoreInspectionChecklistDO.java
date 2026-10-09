package com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 点检项 DO
 *
 * @author 超级管理员
 */
@TableName("system_store_inspection_checklist")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionChecklistDO extends BusinessBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long checklistId;
    /**
     * 大类ID
     */
    private Long typeId;
    /**
     * 大类名称
     */
    private String typeName;
    /**
     * 标题
     */
    private String title;
    /**
     * 提示
     */
    private String prompt;
    /**
     * 总分
     */
    private Integer score;
    /**
     * 0 不允许上传 1 允许上传
     */
    private Boolean imgFlag;
    /**
     * 0 不必传 1 合格时必传 2 不合格时必传 3 不适用时必传
     */
    private String imgState;
    /**
     * 上传图片个数
     */
    private Integer imgCount;
    /**
     * 0 不需要整改人 1 需要整改人
     */
    private Boolean rectifierFlag;
    /**
     * 整改人
     */
    private String rectifier;
    /**
     * 0 不需要审核人 1 需要审核人
     */
    private Boolean approverFlag;
    /**
     * 审核人
     */
    private String approver;
    /**
     * 抄送人
     */
    private String ccPerson;
    /**
     * 整改有效期
     */
    private Integer deadline;
    /**
     * 申述有效期
     */
    private Integer validityPeriod;
    /**
     * 0 不需要上传整改图片 1 需要上传整改图片
     */
    private Boolean rectifyImgFlag;
    /**
     * 0 不允许实时拍照 1允许实时拍照
     */
    private Boolean realFlag;
    /**
     * 点检标准
     */
    private String inspectionStandard;
    /**
     * 排序
     */
    private Integer sort;

}
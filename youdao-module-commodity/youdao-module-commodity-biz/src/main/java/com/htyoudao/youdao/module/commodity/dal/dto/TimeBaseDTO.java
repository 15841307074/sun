package com.htyoudao.youdao.module.commodity.dal.dto;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;
import java.util.Date;
@Data
public class TimeBaseDTO implements Serializable {

    /**
     * 是否满足分时条件
     */
    @Schema(description = "是否满足分时条件")
    private Boolean isUp;

    /**
     * 是否开启分时置顶 1是 0否
     */
    @Schema(description = "是否开启分时置顶 1是 0否")
    private Integer timeSharingTopping;


    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "开始日期")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "结束日期")
    private Date endDate;

    /**
     * 指定日期逗号分割
     */
    @Schema(description = "指定日期逗号分割")
    private String dayNumbers;

    /**
     * 指定周几逗号分割
     */
    @Schema(description = "指定周几逗号分割")
    private String weekNumbers;

    /**
     * 是否全天时段 1是 0否
     */
    @Schema(description = "是否全天时段 1是 0否")
    private Integer isAllDay;

    /**
     * 指定时间段
     */
    @Schema(description = "指定时间段")
    private String timeRange;

}

package com.htyoudao.youdao.module.order.controller.admin.order.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
public class BzOrderAppListReqVO extends PageParam {

    @Serial
    private static final long serialVersionUID = -8384948804635255123L;

    @Schema(description = "是否查看历史订单", example = "0 进行中， 1 历史")
    private Integer orderStateHistory;

    @JsonIgnore
    private String openId;

    /**
     * 搜索关键字 订单号/手机号/三方订单号
     */
    private String searchStr;

    @JsonIgnore
    private LocalDateTime[] createTime;

    @JsonIgnore
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDateTime startTime;

    @JsonIgnore
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDateTime endTime;

    public void setStartTime(LocalDate startTime) {
        if (!ObjectUtils.isEmpty(startTime)) {
            this.startTime = DateUtils.localDateToLocalDateTime(startTime, DateUtils.T_00_00_00);
        }
    }

    public void setEndTime(LocalDate endTime) {
        if (!ObjectUtils.isEmpty(endTime)) {
            this.endTime = DateUtils.localDateToLocalDateTime(endTime, DateUtils.T_23_59_59);
        }
    }
}

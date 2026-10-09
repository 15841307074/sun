package com.htyoudao.youdao.module.order.controller.app.report.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ReportReqVO extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 1:环比 2:同比
     */
    @NotNull
    private Integer hbOrTb;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String searchStr;

    private String searchStr1;

    private String searchStr2;

    @NotNull
    private Integer numberType;

    public void setPageNum(Integer pageNum) {
        this.setPageNo(pageNum);
    }

    public void setCurrent(Integer current) {
        this.setPageNo(current);
    }

    public void setSize(Integer size) {
        this.setPageSize(size);
    }

    public Pair<LocalDate, LocalDate> getOtherTime() {
        return calculateCompareDates(this.numberType, this.hbOrTb, this.startDate, this.endDate);
    }

    /**
     * 返回对比周期的开始和结束日期
     *
     * @param numberType 1=周维度，2=月维度
     * @param hbOrTb     1=环比，2=同比
     * @param startDate  入参开始时间（自然周周一 / 自然月第一天）
     * @param endDate    入参结束时间（自然周周日 / 自然月最后一天）
     * @return 对比周期的开始/结束日期
     */
    public static Pair<LocalDate, LocalDate> calculateCompareDates(Integer numberType,
                                                                   Integer hbOrTb,
                                                                   LocalDate startDate,
                                                                   LocalDate endDate) {
        if (hbOrTb == null) {
            throw new IllegalArgumentException("hbOrTb不能为空");
        }
        if (numberType == null) {
            throw new IllegalArgumentException("numberType不能为空");
        }

        LocalDate compareStart;
        LocalDate compareEnd;

        switch (numberType) {
            // ======================
            // 1. 周维度
            // ======================
            case 1:
                switch (hbOrTb) {
                    case 1: // 环比，上周
                        compareStart = startDate.minusWeeks(1);
                        compareEnd = endDate.minusWeeks(1);
                        break;

                    case 2: // 同比，去年
                        compareStart = startDate.minusYears(1);
                        compareEnd = endDate.minusYears(1);
                        break;

                    default:
                        throw new IllegalArgumentException("hbOrTb只能是1（环比）或2（同比）");
                }
                break;

            // ======================
            // 2. 月维度
            // ======================
            case 2:
                switch (hbOrTb) {
                    case 1: // 环比 上个月
                        LocalDate lastMonth = startDate.minusMonths(1);
                        compareStart = lastMonth.withDayOfMonth(1);
                        compareEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                        break;

                    case 2: // 同比 去年同月
                        LocalDate lastYearMonth = startDate.minusYears(1);
                        compareStart = lastYearMonth.withDayOfMonth(1);
                        compareEnd = lastYearMonth.withDayOfMonth(lastYearMonth.lengthOfMonth());
                        break;

                    default:
                        throw new IllegalArgumentException("hbOrTb只能是1（环比）或2（同比）");
                }
                break;
            default:
                throw new IllegalArgumentException("numberType只能是1（周维度）或2（月维度）");
        }
        return Pair.of(compareStart, compareEnd);
    }


}

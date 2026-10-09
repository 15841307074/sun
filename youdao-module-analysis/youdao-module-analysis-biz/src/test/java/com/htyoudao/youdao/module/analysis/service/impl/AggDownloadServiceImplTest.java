package com.htyoudao.youdao.module.analysis.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.TimeRangeRow;
import com.htyoudao.youdao.module.analysis.enums.DownloadModeEnum;
import java.util.List;
import org.junit.jupiter.api.Test;

class AggDownloadServiceImplTest {

    private AggDownloadServiceImpl  aggDownloadService = new AggDownloadServiceImpl();

    @Test
    void formatByRange() {

        List<TimeRangeRow> dayRows = aggDownloadService.formatByRange("2005-06-01", "2005-06-30",
            DownloadModeEnum.DAY);
        System.out.println(dayRows.size());

        List<TimeRangeRow> monthRows = aggDownloadService.formatByRange("2005-06", "2005-12",
            DownloadModeEnum.MONTH);
        System.out.println(monthRows.size());

        List<TimeRangeRow> summaryRows = aggDownloadService.formatByRange("2005-06-01", "2005-06-30",
            DownloadModeEnum.SUMMARY);
        System.out.println(summaryRows.size());

    }
}
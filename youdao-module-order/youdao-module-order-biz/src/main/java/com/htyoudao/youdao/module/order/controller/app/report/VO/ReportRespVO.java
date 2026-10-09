package com.htyoudao.youdao.module.order.controller.app.report.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ReportRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    List<ReportListRespVO> list;

    private List<String> x;

    private List<Map<String, BigDecimal>> y;

    private List<Integer> salesNums = new ArrayList<>();

    private String startDate0;

    private String endDate0;

    private String startDate1;

    private String endDate1;



    private Long total;
}

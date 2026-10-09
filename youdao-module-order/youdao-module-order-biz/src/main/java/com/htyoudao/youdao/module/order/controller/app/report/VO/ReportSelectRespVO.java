package com.htyoudao.youdao.module.order.controller.app.report.VO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportSelectRespVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7418930538037003579L;

    private Long k;

    private String v;
}

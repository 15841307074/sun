package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 有奖问答动态导出数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAnswerExportDataVO {

    /**
     * EasyExcel 动态表头。
     */
    private List<List<String>> head;

    /**
     * EasyExcel 动态行数据。
     */
    private List<List<String>> data;
}

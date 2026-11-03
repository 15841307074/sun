package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 门店导出专用数据，保留现有导出列，不向分页接口增加字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class StoreExportVO extends StoreResVO {
    @ExcelProperty("上级组织及负责人")
    @ColumnWidth(80)
    private String parentOrgNamesAndLeaders;
}

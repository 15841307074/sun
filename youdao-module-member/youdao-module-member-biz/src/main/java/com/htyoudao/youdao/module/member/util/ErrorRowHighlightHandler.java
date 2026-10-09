package com.htyoudao.youdao.module.member.util;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.Set;

/**
 * 错误行高亮处理器 - 将指定行号的数据标记为浅红色背景
 */
public class ErrorRowHighlightHandler implements CellWriteHandler {

    // 需要高亮的行号集合（Excel中的实际行号，从1开始）
    private final Set<Integer> errorRowNumbers;

    // 浅红色背景样式缓存
    private CellStyle errorCellStyle;

    public ErrorRowHighlightHandler(Set<Integer> errorRowNumbers) {
        this.errorRowNumbers = errorRowNumbers;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
    }


    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<Cell> cells, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 获取当前行号（Excel实际行号，从1开始）
        int currentRowNum = cells.get(0).getRowIndex() + 1;

        // 如果不是表头且当前行需要高亮
        if (!isHead && errorRowNumbers.contains(currentRowNum)) {
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
            if (errorCellStyle == null) {
                errorCellStyle = createErrorCellStyle(workbook);
            }
            // 为当前行的所有单元格设置样式
            for (Cell cell : cells) {
                cell.setCellStyle(errorCellStyle);
            }
        }
    }

    /**
     * 创建浅红色背景样式
     */
    private CellStyle createErrorCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 设置浅红色背景
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框（可选）
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}

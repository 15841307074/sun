package com.htyoudao.youdao.module.member.util;

import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.PointsUpdateErrorDTO;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义行样式处理器 - 根据错误信息设置行背景色为红色
 */
/**
 * 基于行数据的样式处理器
 */
public class ErrorRowStyleHandler implements RowWriteHandler {

    // 存储每一行的错误信息
    private final Map<Integer, Boolean> rowHasErrorMap = new HashMap<>();

    public ErrorRowStyleHandler(List<PointsUpdateErrorDTO> errorDataList) {
        // 预先计算哪些行有错误信息
        for (int i = 0; i < errorDataList.size(); i++) {
            PointsUpdateErrorDTO dto = errorDataList.get(i);
            if (dto != null && StringUtils.isNotBlank(dto.getErrorMessage())) {
                rowHasErrorMap.put(i + 1, true); // +1 是因为第0行是表头
            }
        }
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                Row row, Integer rowIndex, Boolean isHead) {
        if (isHead || rowIndex == null) {
            return;
        }

        // 检查当前行是否有错误
        if (rowHasErrorMap.containsKey(rowIndex)) {
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();

            // 为整行设置红色背景
            for (Cell cell : row) {
                CellStyle style = workbook.createCellStyle();
                style.cloneStyleFrom(cell.getCellStyle());
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // 设置字体颜色为白色
                Font font = workbook.createFont();
                font.setColor(IndexedColors.WHITE.getIndex());
                style.setFont(font);

                cell.setCellStyle(style);
            }
        }
    }
}
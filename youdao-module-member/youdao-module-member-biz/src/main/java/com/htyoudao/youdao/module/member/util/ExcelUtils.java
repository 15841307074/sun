package com.htyoudao.youdao.module.member.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
public class ExcelUtils {

    /**
     * 获取Excel文件的数据行数（不含表头）
     */
    public static int getRowCount(byte[] fileBytes, String filename) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();  // 返回数据行数（不含表头）
            return Math.max(0, rowCount);
        } catch (Exception e) {
            log.error("读取Excel行数失败", e);
            throw new RuntimeException("读取Excel文件失败：" + e.getMessage());
        }
    }
}

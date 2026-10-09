package com.htyoudao.youdao.framework.excel.core.service;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.enums.DownStatusEnum;
import com.htyoudao.youdao.framework.excel.core.context.RequestContext;
import com.htyoudao.youdao.framework.excel.core.context.RequestContextHolder;
import com.htyoudao.youdao.framework.excel.core.handler.Custemhandler;
import com.htyoudao.youdao.framework.excel.core.pojo.SearchAfterSupport;
import com.htyoudao.youdao.framework.excel.core.service.listenner.GenericExcelListener;
import com.htyoudao.youdao.framework.excel.core.service.vo.ActivityLogStatisticsVO;
import com.htyoudao.youdao.framework.excel.core.service.vo.LotteryLogStatisticsVO;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.module.infra.api.download.DownloadApi;
import com.htyoudao.youdao.module.infra.api.download.dto.FileDownloadReqDTO;
import com.htyoudao.youdao.module.infra.api.file.FileApi;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.io.InputStream;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.apache.ibatis.cursor.Cursor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@Slf4j
@Component
public class ExcelActionService<T> {

    private static final String UPLOAD_TMP_DIR= "/tmpfile/";

    @Resource
    private Validator validator;

    //@DubboReference(methods = {@Method(name = "multiPartCreateFile", timeout = 300000)})
    @DubboReference(
            methods = {
                    @Method(name = "multiPartCreateFile", parameters = {"payload", "83886080"}, timeout = 500000),
                    @Method(name = "createFile", parameters = {"payload", "83886080"}, timeout = 500000)
            },
            timeout = 50000
    )
    private FileApi fileApi;
    @DubboReference
    private DownloadApi downloadApi;

    public List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        GenericExcelListener<T> listener = new GenericExcelListener<>(validator);
        try {
            EasyExcel.read(inputStream, clazz, listener)
                .charset(Charset.forName("GBK"))
                .sheet().doRead();
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("Excel解析失败", e);
        }

        //强校验 必填项是否填写
        if (CollectionUtils.isNotEmpty(listener.getErrors())) {
            String errorMsg = Joiner.on("\n\r").join(listener.getErrors());
            throw new ServiceException(EXCEL_IMPORT_FILE_FAILED.getCode(), errorMsg);
        }

        //导入内容
        return listener.getSuccessList();
    }


    /**
     * 异步分页分片导出+查询  一次性并且携带头
     * @param clazz        数据实体类
     * @param queryFunction     数据
     * @param fileName     导出文件名
     */
    public <T> void exportAsyncExcel(Class<T> clazz,Object requestVO,Function<Object, List<T>> queryFunction, String fileName,Set<String> header) throws ServerException {

        log.info("==> 开始执行异步下载任务");

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder().fileName(fileName).creator(context.getCreator()).businessId(context.getBusinessId()).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
                //FileDownloadReqDTO.builder().fileName(fileName).creator("admin").businessId(3L).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os, clazz)
                        .registerWriteHandler(new Custemhandler())
                        .includeColumnFieldNames(header)
                        //.registerWriteHandler(new FirstRowWriteHandler(firstRow))
                        .registerWriteHandler(this.getStyleStrategy())
                        .build();
                WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").head(clazz).build();

                List<T> data = queryFunction.apply(requestVO);
                excelWriter.write(data, writeSheet);
            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);

            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }


    /**
     * 异步分页分片导出（双Sheet：抽奖记录 + 门店统计）
     *
     * @param clazz              数据实体类
     * @param page               分页对象
     * @param dataSupplier       数据提供者函数
     * @param fileName           导出文件名
     * @param statisticsData     门店统计数据（第二个Sheet的内容）
     */
    public <T> void exportAsyncExcel(Class<T> clazz,
                                              Page<T> page,
                                              Function<Page<T>, List<T>> dataSupplier,
                                              String fileName,
                                              List<LotteryLogStatisticsVO> statisticsData) throws ServerException {

        log.info("==> 开始执行异步下载任务（双Sheet版）");

        RequestContext context = RequestContextHolder.getContext();
        if(context!=null){
            BusinessContextHolder.setBusinessId(context.getBusinessId());
        }


        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder().fileName(fileName).creator(context.getCreator()).businessId(context.getBusinessId()).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                // 关键修改：使用空构造器，支持多个Sheet
                excelWriter = EasyExcel.write(os)
                        .inMemory(false)
                        .build();

                // ================== 第一步：写主数据Sheet（抽奖记录，Sheet1） ==================
                int currentSheetIndex = 1;
                WriteSheet writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex, "抽奖记录");

                long current = 1;
                int dynamicBatchSize = 50000;
                // 总处理行数计数器
                int totalCount = 0;
                // 页码计数器
                int pageCount = 0;
                // 当前Sheet行数计数器
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;

                log.info("开始导出抽奖记录数据...");
                while (true) {
                    page.setCurrent(current);
                    List<T> data = this.fetchDataWithRetry(dataSupplier, page);
                    log.info("当前页: {}, 数据量: {}", current, data.size());
                    pageCount++;
                    if (data == null || data.isEmpty()) {
                        break;
                    }

                    int fromIndex = 0;
                    while (fromIndex < data.size()) {
                        int toIndex = Math.min(fromIndex + dynamicBatchSize, data.size());
                        List<T> batch = data.subList(fromIndex, toIndex);

                        // 检查是否需要切换Sheet（只对主数据分Sheet，统计数据不分）
                        if (sheetRowCount + batch.size() > maxSheetRows) {
                            int remainingInSheet = maxSheetRows - sheetRowCount;

                            if (remainingInSheet > 0) {
                                List<T> partialBatch = batch.subList(0, remainingInSheet);
                                excelWriter.write(partialBatch, writeSheet);
                                totalCount += partialBatch.size();
                                sheetRowCount += partialBatch.size();
                            }

                            // 创建新Sheet（还是抽奖记录）
                            currentSheetIndex++;
                            writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex, "抽奖记录_" + (currentSheetIndex - 1));
                            sheetRowCount = 0;

                            if (remainingInSheet > 0) {
                                batch = batch.subList(remainingInSheet, batch.size());
                            }
                        }

                        // 写入批次数据
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        sheetRowCount += batch.size();
                        fromIndex = toIndex;

                        // 动态调整批次大小
                        dynamicBatchSize = adjustBatchSize(totalCount);

                        // 每10万行打印进度
                        if (totalCount % 100000 == 0) {
                            log.info("已导出抽奖记录: {} 行, 当前Sheet: {}, Sheet行数: {}",
                                    totalCount, writeSheet.getSheetName(), sheetRowCount);
                        }
                    }
                    current++;

                    //减少一次空数据请求
                    if (data.size() < page.getSize()) {
                        break;
                    }
                }

                log.info("抽奖记录导出完成，总共处理{}页，{}条数据", pageCount, totalCount);

                // ================== 第二步：写统计数据Sheet（门店统计，Sheet2） ==================
                if (statisticsData != null && !statisticsData.isEmpty()) {
                    try {
                        WriteSheet statsSheet = EasyExcel.writerSheet("门店统计")
                                .head(LotteryLogStatisticsVO.class)
                                .registerWriteHandler(getStatisticsStyleStrategy())
                                .build();

                        // 写入统计数据
                        excelWriter.write(statisticsData, statsSheet);

                        log.info("已写入门店统计Sheet，共{}条记录", statisticsData.size());

                    } catch (Exception e) {
                        log.warn("写入统计Sheet失败，将继续完成导出", e);
                    }
                } else {
                    log.info("无统计数据，不生成统计Sheet");
                }

            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);
            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }


    /**
     * 异步分页分片导出（双Sheet：抽卡记录 + 门店统计）
     *
     * @param clazz              数据实体类
     * @param page               分页对象
     * @param dataSupplier       数据提供者函数
     * @param fileName           导出文件名
     * @param statisticsData     门店统计数据（第二个Sheet的内容）
     */
    public <T> void exportAsyncExcel(Class<T> clazz,
                                     Page<T> page,
                                     Function<Page<T>, List<T>> dataSupplier,
                                     String fileName,
                                     List<ActivityLogStatisticsVO> statisticsData, Boolean flag) throws ServerException {

        log.info("==> 开始执行异步下载任务（双Sheet版）");

        RequestContext context = RequestContextHolder.getContext();
        if(context!=null){
            BusinessContextHolder.setBusinessId(context.getBusinessId());
        }


        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder().fileName(fileName).creator(context.getCreator()).businessId(context.getBusinessId()).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                // 关键修改：使用空构造器，支持多个Sheet
                excelWriter = EasyExcel.write(os)
                        .inMemory(false)
                        .build();

                // ================== 第一步：写主数据Sheet（抽奖记录，Sheet1） ==================
                int currentSheetIndex = 1;
                WriteSheet writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex, "抽奖记录");

                long current = 1;
                int dynamicBatchSize = 50000;
                // 总处理行数计数器
                int totalCount = 0;
                // 页码计数器
                int pageCount = 0;
                // 当前Sheet行数计数器
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;

                log.info("开始导出抽奖记录数据...");
                while (true) {
                    page.setCurrent(current);
                    List<T> data = this.fetchDataWithRetry(dataSupplier, page);
                    log.info("当前页: {}, 数据量: {}", current, data.size());
                    pageCount++;
                    if (data == null || data.isEmpty()) {
                        break;
                    }

                    int fromIndex = 0;
                    while (fromIndex < data.size()) {
                        int toIndex = Math.min(fromIndex + dynamicBatchSize, data.size());
                        List<T> batch = data.subList(fromIndex, toIndex);

                        // 检查是否需要切换Sheet（只对主数据分Sheet，统计数据不分）
                        if (sheetRowCount + batch.size() > maxSheetRows) {
                            int remainingInSheet = maxSheetRows - sheetRowCount;

                            if (remainingInSheet > 0) {
                                List<T> partialBatch = batch.subList(0, remainingInSheet);
                                excelWriter.write(partialBatch, writeSheet);
                                totalCount += partialBatch.size();
                                sheetRowCount += partialBatch.size();
                            }

                            currentSheetIndex++;
                            writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex, "抽卡记录_" + (currentSheetIndex - 1));
                            sheetRowCount = 0;

                            if (remainingInSheet > 0) {
                                batch = batch.subList(remainingInSheet, batch.size());
                            }
                        }

                        // 写入批次数据
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        sheetRowCount += batch.size();
                        fromIndex = toIndex;

                        // 动态调整批次大小
                        dynamicBatchSize = adjustBatchSize(totalCount);

                        // 每10万行打印进度
                        if (totalCount % 100000 == 0) {
                            log.info("已导出抽卡记录: {} 行, 当前Sheet: {}, Sheet行数: {}",
                                    totalCount, writeSheet.getSheetName(), sheetRowCount);
                        }
                    }
                    current++;

                    //减少一次空数据请求
                    if (data.size() < page.getSize()) {
                        break;
                    }
                }

                log.info("抽卡记录导出完成，总共处理{}页，{}条数据", pageCount, totalCount);

                // ================== 第二步：写统计数据Sheet（门店统计，Sheet2） ==================
                if (statisticsData != null && !statisticsData.isEmpty()) {
                    try {
                        WriteSheet statsSheet = EasyExcel.writerSheet("门店统计")
                                .head(LotteryLogStatisticsVO.class)
                                .registerWriteHandler(getStatisticsStyleStrategy())
                                .build();

                        // 写入统计数据
                        excelWriter.write(statisticsData, statsSheet);

                        log.info("已写入门店统计Sheet，共{}条记录", statisticsData.size());

                    } catch (Exception e) {
                        log.warn("写入统计Sheet失败，将继续完成导出", e);
                    }
                } else {
                    log.info("无统计数据，不生成统计Sheet");
                }

            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);
            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }




    /**
     * 创建新的Sheet（重载，支持自定义Sheet名称）
     */
    private WriteSheet createNewSheet(ExcelWriter excelWriter, Class<?> clazz, int sheetIndex, String sheetName) {
        if (sheetName == null || sheetName.isEmpty()) {
            sheetName = "Sheet" + sheetIndex;
        }

        return EasyExcel.writerSheet(sheetName)
                .head(clazz)
                .registerWriteHandler(new Custemhandler())
                .registerWriteHandler(this.getStyleStrategy())
                .build();
    }

    /**
     * 统计表样式策略
     */
    private WriteHandler getStatisticsStyleStrategy() {
        // 表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headWriteCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);

        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setBold(true);
        headWriteFont.setFontHeightInPoints((short) 12);
        headWriteCellStyle.setWriteFont(headWriteFont);

        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);

        // 内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 异步分页分片导出  一次性并且携带头
     * @param clazz        数据实体类
     * @param dataList     数据
     * @param fileName     导出文件名
     */
    public <T> void exportAsyncExcel(Class<T> clazz,List<T> dataList , String fileName,Set<String> header) throws ServerException {

        log.info("==> 开始执行异步下载任务");

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder().fileName(fileName).creator(context.getCreator()).businessId(context.getBusinessId()).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os, clazz)
                        .registerWriteHandler(new Custemhandler())
                        .includeColumnFieldNames(header)
                        //.registerWriteHandler(new FirstRowWriteHandler(firstRow))
                        .registerWriteHandler(this.getStyleStrategy())
                        .build();
                WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").head(clazz).build();

                List<T> data = dataList;
                excelWriter.write(data, writeSheet);
            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);

            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }


    /**
     * 异步分页分片导出
     *
     * @param clazz        数据实体类
     * @param page         分页对象
     * @param dataSupplier 数据提供者函数
     * @param fileName     导出文件名
     */
    public <T> void exportAsyncExcel(Class<T> clazz, Page<T> page, Function<Page<T>, List<T>> dataSupplier, String fileName) throws ServerException {

        log.info("==> 开始执行异步下载任务");

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder()
                        .fileName(fileName)
                        .creator(context.getCreator())
                        .businessId(context.getBusinessId())
                        .status(DownStatusEnum.DOWNLOADING.getStatus())
                        .build()
        );

//        CommonResult<Long> downloadResult = downloadApi.createDownload(
//                FileDownloadReqDTO.builder().fileName(fileName).creator("dht").businessId(10L).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
//        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os, clazz)
                        .registerWriteHandler(new Custemhandler())
                        .registerWriteHandler(this.getStyleStrategy())
                        .inMemory(false)
                        .build();
                int currentSheetIndex = 1;
                //WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").head(clazz).build();
                WriteSheet writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);

                long current = 1;
                int dynamicBatchSize = 50000;
                // 总处理行数计数器
                int totalCount = 0;
                // 页码计数器
                int pageCount = 0;
                // 当前Sheet行数计数器
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;


                int i = 0;
                while (true) {
                    page.setCurrent(current);
                    List<T> data = this.fetchDataWithRetry(dataSupplier, page);
                    log.info("当前页: {}, 数据量: {}", current, data.size());
                    pageCount++;
                    if (data == null || data.isEmpty()) {
                        break;
                    }


                    i++;
                    int fromIndex = 0;
                    while (fromIndex < data.size()) {
                        int toIndex = Math.min(fromIndex + dynamicBatchSize, data.size());
                        List<T> batch = data.subList(fromIndex, toIndex);

                        // 检查是否需要切换Sheet
                        if (sheetRowCount + batch.size() > maxSheetRows) {
                            // 计算当前Sheet还能写入的行数
                            int remainingInSheet = maxSheetRows - sheetRowCount;

                            // 写入当前Sheet剩余容量
                            if (remainingInSheet > 0) {
                                List<T> partialBatch = batch.subList(0, remainingInSheet);
                                excelWriter.write(partialBatch, writeSheet);
                                totalCount += partialBatch.size();
                                sheetRowCount += partialBatch.size();
                            }

                            // 创建新Sheet
                            currentSheetIndex++;
                            writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);
                            sheetRowCount = 0;

                            // 处理剩余数据（如果有）
                            if (remainingInSheet > 0) {
                                batch = batch.subList(remainingInSheet, batch.size());
                            }
                        }

                        // 写入批次数据
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        sheetRowCount += batch.size();
                        fromIndex = toIndex;

                        // 动态调整批次大小
                        dynamicBatchSize = adjustBatchSize(totalCount);

                        // 每10万行打印进度
                        if (totalCount % 100000 == 0) {
                            log.info("已导出: {} 行, 当前Sheet: {}, Sheet行数: {}",
                                    totalCount, writeSheet.getSheetName(), sheetRowCount);
                        }
                    }
                    current++;

                    //减少一次空数据请求
                    if (data.size() < page.getSize()) {
                        break;
                    }
                }

            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);

            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }


    /**
     * 异步分页分片导出
     *
     * @param clazz        数据实体类
     * @param dataSupplier 数据提供者函数
     * @param fileName     导出文件名
     */
    public <T> void exportAsyncExcel(Class<T> clazz, Function<Page<T>, List<T>> dataSupplier, String fileName) throws ServerException {

        log.info("==> 开始执行异步下载任务");

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        // 确保文件名有效并过滤非法字符
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder().fileName(fileName).creator(context.getCreator()).businessId(context.getBusinessId()).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
        );

//        CommonResult<Long> downloadResult = downloadApi.createDownload(
//                FileDownloadReqDTO.builder().fileName(fileName).creator("dht").businessId(10L).status(DownStatusEnum.DOWNLOADING.getStatus()).build()
//        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os, clazz)
                        .registerWriteHandler(new Custemhandler())
                        .registerWriteHandler(this.getStyleStrategy())
                        .inMemory(false)
                        .build();
                int currentSheetIndex = 1;
                WriteSheet writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);

                long current = 1;
                int dynamicBatchSize = 50000;
                // 总处理行数计数器
                int totalCount = 0;
                // 当前Sheet行数计数器
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;


                Page<T> page = Page.of(current, dynamicBatchSize);
                List<T> data = this.fetchDataWithRetry(dataSupplier, page);
                log.info("无分页导出查询页: {}, 数据量: {}", current, data.size());


                int fromIndex = 0;
                while (fromIndex < data.size()) {
                    int toIndex = Math.min(fromIndex + dynamicBatchSize, data.size());
                    List<T> batch = data.subList(fromIndex, toIndex);

                    // 检查是否需要切换Sheet
                    if (sheetRowCount + batch.size() > maxSheetRows) {
                        // 计算当前Sheet还能写入的行数
                        int remainingInSheet = maxSheetRows - sheetRowCount;

                        // 写入当前Sheet剩余容量
                        if (remainingInSheet > 0) {
                            List<T> partialBatch = batch.subList(0, remainingInSheet);
                            excelWriter.write(partialBatch, writeSheet);
                            totalCount += partialBatch.size();
                            sheetRowCount += partialBatch.size();
                        }

                        // 创建新Sheet
                        currentSheetIndex++;
                        writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);
                        sheetRowCount = 0;

                        // 处理剩余数据（如果有）
                        if (remainingInSheet > 0) {
                            batch = batch.subList(remainingInSheet, batch.size());
                        }
                    }

                    // 写入批次数据
                    excelWriter.write(batch, writeSheet);
                    totalCount += batch.size();
                    sheetRowCount += batch.size();
                    fromIndex = toIndex;

                    // 动态调整批次大小
                    dynamicBatchSize = adjustBatchSize(totalCount);

                    // 每10万行打印进度
                    if (totalCount % 100000 == 0) {
                        log.info("已导出: {} 行, 当前Sheet: {}, Sheet行数: {}", totalCount, writeSheet.getSheetName(), sheetRowCount);
                    }
                }



            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);

            // 分片上传OSS
            File file = new File(safeFileName);

            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }

    /**
     * 异步分页分片导出（动态表头）
     *
     * @param page              分页对象
     * @param dataSupplier      数据提供者
     * @param fileName          导出文件名
     * @param head              动态表头
     * @param extraWriteHandlers 额外写处理器
     */
    public void exportAsyncExcel(Page<List<Object>> page,
                                 Function<Page<List<Object>>, List<List<Object>>> dataSupplier,
                                 String fileName,
                                 List<List<String>> head,
                                 List<WriteHandler> extraWriteHandlers) throws ServerException {

        log.info("==> 开始执行异步下载任务（动态表头版）");

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder()
                        .fileName(fileName)
                        .creator(context.getCreator())
                        .businessId(context.getBusinessId())
                        .status(DownStatusEnum.DOWNLOADING.getStatus())
                        .build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os)
                        .registerWriteHandler(new Custemhandler())
                        .registerWriteHandler(this.getStyleStrategy())
                        .inMemory(false)
                        .build();

                int currentSheetIndex = 1;
                WriteSheet writeSheet = createNewDynamicSheet(currentSheetIndex, head, extraWriteHandlers);

                long current = 1;
                int dynamicBatchSize = 50000;
                int totalCount = 0;
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;

                while (true) {
                    page.setCurrent(current);
                    List<List<Object>> data = dataSupplier.apply(page);
                    log.info("动态表头导出当前页: {}, 数据量: {}", current, data == null ? 0 : data.size());

                    if (data == null || data.isEmpty()) {
                        break;
                    }

                    int fromIndex = 0;
                    while (fromIndex < data.size()) {
                        int toIndex = Math.min(fromIndex + dynamicBatchSize, data.size());
                        List<List<Object>> batch = data.subList(fromIndex, toIndex);

                        if (sheetRowCount + batch.size() > maxSheetRows) {
                            int remainingInSheet = maxSheetRows - sheetRowCount;
                            if (remainingInSheet > 0) {
                                List<List<Object>> partialBatch = batch.subList(0, remainingInSheet);
                                excelWriter.write(partialBatch, writeSheet);
                                totalCount += partialBatch.size();
                                sheetRowCount += partialBatch.size();
                            }

                            currentSheetIndex++;
                            writeSheet = createNewDynamicSheet(currentSheetIndex, head, extraWriteHandlers);
                            sheetRowCount = 0;

                            if (remainingInSheet > 0) {
                                batch = batch.subList(remainingInSheet, batch.size());
                            }
                        }

                        if (!batch.isEmpty()) {
                            excelWriter.write(batch, writeSheet);
                            totalCount += batch.size();
                            sheetRowCount += batch.size();
                        }
                        fromIndex = toIndex;
                        dynamicBatchSize = adjustBatchSize(totalCount);
                    }
                    current++;

                    if (data.size() < page.getSize()) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason(e.getMessage()).build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            log.info(">>> 输出本地文件 {}", safeFileName);
            File file = new File(safeFileName);
            log.info(">>> 文件上传存储器开始，文件大小: {}", file.length());

            try {
                String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
                if (url == null || url.isEmpty()) {
                    log.warn("文件上传存储器失败，文件名: {}", safeFileName);
                    downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("文件上传存储器失败").build());
                    return;
                }

                log.info(">>> 文件上传存储器成功，文件名: {}", safeFileName);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.SUCCESS.getStatus()).fileUrl(url).build()
                );
            } finally {
                this.deleteFileIfExist(file);
            }
        } catch (Exception e) {
            log.error("资源关闭失败", e);
            downloadApi.updateDownload(FileDownloadReqDTO.builder().id(downloadId).status(DownStatusEnum.FAILD.getStatus()).failedReason("资源关闭失败").build());
        }
    }

    /**
     * 异步分页分片导出（支持 SearchAfter 模式）TODO 目前指给PC订单列表使用
     *
     * @param clazz        导出实体类
     * @param page         分页对象
     * @param dataSupplier 数据提供函数（BiFunction：reqVO + page -> SearchAfterPage<T>）
     * @param reqVO        请求参数（包含搜索条件、searchAfter 游标）
     * @param fileName     导出文件名
     * @param useSearchAfter 是否启用 SearchAfter 模式
     */
    public <T, P> void exportAsyncExcel(Class<T> clazz, Page<T> page, BiFunction<P, Page<T>, SearchAfterPage<T>> dataSupplier, P reqVO, String fileName, boolean useSearchAfter) throws ServerException {

        log.info("==> 开始执行异步下载任务（useSearchAfter={}）", useSearchAfter);

        RequestContext context = RequestContextHolder.getContext();
        BusinessContextHolder.setBusinessId(context.getBusinessId());

        // 安全文件名
        String safeFileName = this.getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;

        // 创建下载记录
        CommonResult<Long> downloadResult = downloadApi.createDownload(
                FileDownloadReqDTO.builder()
                        .fileName(fileName)
                        .creator(context.getCreator())
                        .businessId(context.getBusinessId())
                        .status(DownStatusEnum.DOWNLOADING.getStatus())
                        .build()
        );
        Long downloadId = downloadResult.getCheckedData();

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(os, clazz)
                        .registerWriteHandler(new Custemhandler())
                        .registerWriteHandler(this.getStyleStrategy())
                        .inMemory(false)
                        .build();

                int currentSheetIndex = 1;
                long current = 1;
                WriteSheet writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);

                // 分页游标
                List<FieldValue> searchAfter = null;
                long totalCount = 0;
                int sheetRowCount = 0;
                final int maxSheetRows = 1_048_570;

                while (true) {
                    page.setCurrent(current);
                    if (useSearchAfter && reqVO instanceof SearchAfterSupport) {
                        ((SearchAfterSupport) reqVO).setSearchAfter(searchAfter);
                    }

                    // 调用数据提供函数
                    SearchAfterPage<T> resultPage = dataSupplier.apply(reqVO, page);
                    List<T> data = resultPage.getList();
                    if (CollUtil.isEmpty(data)) {
                        break;
                    }

                    // 写入数据
                    excelWriter.write(data, writeSheet);
                    totalCount += data.size();
                    sheetRowCount += data.size();

                    // 保存下一个 search_after 游标
                    if (useSearchAfter) {
                        searchAfter = resultPage.getSearchAfter();
                    }

                    // 判断是否结束
                    if (!useSearchAfter || searchAfter == null) {
                        break;
                    }

                    // 超过单个 sheet 上限，创建新 sheet
                    if (sheetRowCount >= maxSheetRows) {
                        currentSheetIndex++;
                        writeSheet = createNewSheet(excelWriter, clazz, currentSheetIndex);
                        sheetRowCount = 0;
                    }

                    current++;

                    log.info("==> 导出进度: {} 条 (模式: {})", totalCount, "SearchAfter");
                }

            } catch (Exception e) {
                log.error("==> Excel导出失败", e);
                downloadApi.updateDownload(
                        FileDownloadReqDTO.builder()
                                .id(downloadId)
                                .status(DownStatusEnum.FAILD.getStatus())
                                .failedReason(e.getMessage())
                                .build()
                );
                return;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭 ExcelWriter 时发生异常", e);
                    }
                }
            }

            // 上传文件到 OSS
            File file = new File(safeFileName);
            String url = fileApi.multiPartCreateFile(FileMultiPartCreateReqDTO.builder().file(file).build()).getCheckedData();
            downloadApi.updateDownload(
                    FileDownloadReqDTO.builder()
                            .id(downloadId)
                            .status(DownStatusEnum.SUCCESS.getStatus())
                            .fileUrl(url)
                            .build()
            );

            this.deleteFileIfExist(file);

        } catch (Exception e) {
            log.error("SearchAfter 导出失败", e);
            downloadApi.updateDownload(
                    FileDownloadReqDTO.builder()
                            .id(downloadId)
                            .status(DownStatusEnum.FAILD.getStatus())
                            .failedReason("资源关闭失败")
                            .build()
            );
        }
    }



    /**
     * 创建新Sheet的方法
     * @param excelWriter excelWriter
     * @param clazz clazz
     * @param  sheetIndex sheetIndex
     * @return WriteSheet
     */
    private WriteSheet createNewSheet(ExcelWriter excelWriter, Class<?> clazz, int sheetIndex) {
        String sheetName = "Sheet" + sheetIndex;
        log.info("创建新Sheet: {}", sheetName);

        return EasyExcel.writerSheet(sheetIndex, sheetName)
                .head(clazz)
                .build();
    }

    private WriteSheet createNewDynamicSheet(int sheetIndex, List<List<String>> head, List<WriteHandler> extraWriteHandlers) {
        String sheetName = "Sheet" + sheetIndex;
        log.info("创建新动态Sheet: {}", sheetName);
        com.alibaba.excel.write.builder.ExcelWriterSheetBuilder builder = EasyExcel.writerSheet(sheetIndex, sheetName).head(head);
        if (extraWriteHandlers != null) {
            for (WriteHandler handler : extraWriteHandlers) {
                builder.registerWriteHandler(handler);
            }
        }
        return builder.build();
    }

    /**
     * 动态调整批次大小的方法
     * @param totalProcessed
     * @return
     */
    private int adjustBatchSize(int totalProcessed) {
        if (totalProcessed < 500000) {
            // 前50万行：5万/批
            return 50000;
        } else if (totalProcessed < 1000000) {
            // 50-100万行：10万/批
            return 100000;
        } else if (totalProcessed < 2000000) {
            // 100-200万行：20万/批
            return 200000;
        } else {
            // 200万行以上：30万/批
            return 300000;
        }
    }

    /**
     * Excel导出方法
     *
     * @param response
     * @param fileName
     * @param dataList
     * @param clazz
     * @param <T>
     */
    public <T> void exportExcel(HttpServletResponse response, HttpServletRequest request, String fileName, List<T> dataList, Class<T> clazz) {
        this.validateExportParams(response, fileName, dataList, clazz);

        try {
            //必要的参数校验
            this.setupResponse(response, fileName, request);

            ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream(), clazz)
                    .autoCloseStream(true)
                    .excludeColumnFieldNames(Collections.emptyList())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());

            writerBuilder.sheet("Sheet1")
                    .doWrite(dataList);
        } catch (IOException e) {
            throw exception(new ErrorCode(EXCEL_EXPORT_FILE_FAILED.getCode(), String.format(EXCEL_EXPORT_FILE_FAILED.getMsg(), fileName, e.getMessage())));
        }
    }

    /**
     * 删除文件并处理异常
     *
     * @param file
     */
    private void deleteFileIfExist(File file) {
        if (file != null && file.exists()) {
            try {
                if (!file.delete()) {
                    log.warn("文件删除失败，文件名: {}", file.getName());
                }
            } catch (Exception e) {
                log.warn("文件删除时发生异常", e);
            }
        }
    }

    @NotNull
    private String getSafeFileName(String fileName) {
        String safeFileName = this.sanitizeFileName(fileName);
        if (safeFileName == null || safeFileName.isEmpty()) {
            safeFileName = "default_export";
        }
        safeFileName = safeFileName + "_" + DateUtils.dateTimeNow() + ".xlsx";
        return safeFileName;
    }

    /**
     * 过滤非法字符
     *
     * @param fileName
     * @return
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // 替换非法字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 获取数据并增加重试机制
     *
     * @param dataSupplier
     * @param page
     * @return
     */
    private <T> List<T> fetchDataWithRetry(Function<Page<T>, List<T>> dataSupplier, Page<T> page) {
        // 最大重试次数
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                return dataSupplier.apply(page);
            } catch (Exception e) {
                log.warn("数据获取失败，正在进行第{}次重试", i + 1, e);
            }
        }
        log.warn("数据获取失败，已达到最大重试次数");
        return Collections.emptyList();
    }

    public HorizontalCellStyleStrategy getStyleStrategy() {
        try {
            // 定义常量
            final short FONT_SIZE = 12;
            final String HEAD_FONT_NAME = "宋体";
            final String CONTENT_FONT_NAME = "Calibri";
            final short GREY_COLOR_INDEX = IndexedColors.GREY_25_PERCENT.getIndex();
            final short WHITE_COLOR_INDEX = IndexedColors.WHITE.getIndex();
            final BorderStyle BORDER_STYLE = BorderStyle.THIN;  // 细线边框
            final short BORDER_COLOR = IndexedColors.BLACK.getIndex();  // 黑色

            // 创建头样式
            WriteCellStyle headWriteCellStyle = this.createCellStyle(GREY_COLOR_INDEX, true);
            WriteFont headWriteFont = this.createFont(FONT_SIZE, HEAD_FONT_NAME, true, null);
            headWriteCellStyle.setWriteFont(headWriteFont);
            // 自动换行
            headWriteCellStyle.setWrapped(true);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 创建内容样式
            WriteCellStyle contentWriteCellStyle = this.createCellStyle(WHITE_COLOR_INDEX, false);
            WriteFont contentWriteFont = this.createFont(FONT_SIZE, CONTENT_FONT_NAME, false, null);
            // 显式设置填充模式
            contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
            contentWriteCellStyle.setWriteFont(contentWriteFont);

            // 设置内容边框（与表头一致）
            contentWriteCellStyle.setBorderLeft(BORDER_STYLE);
            contentWriteCellStyle.setLeftBorderColor(BORDER_COLOR);
            contentWriteCellStyle.setBorderRight(BORDER_STYLE);
            contentWriteCellStyle.setRightBorderColor(BORDER_COLOR);
            contentWriteCellStyle.setBorderTop(BORDER_STYLE);
            contentWriteCellStyle.setTopBorderColor(BORDER_COLOR);
            contentWriteCellStyle.setBorderBottom(BORDER_STYLE);
            contentWriteCellStyle.setBottomBorderColor(BORDER_COLOR);

            return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create style strategy", e);
        }
    }

    /**
     * 创建样式
     *
     * @param fillColor
     * @param isBold
     * @return
     */
    private WriteCellStyle createCellStyle(short fillColor, boolean isBold) {
        WriteCellStyle cellStyle = new WriteCellStyle();
        cellStyle.setFillForegroundColor(fillColor);
        return cellStyle;
    }

    /**
     * 创建字体
     *
     * @param fontSize
     * @param fontName
     * @param isBold
     * @param colorIndex
     * @return
     */
    private WriteFont createFont(short fontSize, String fontName, boolean isBold, Short colorIndex) {
        WriteFont font = new WriteFont();
        font.setFontHeightInPoints(fontSize);
        font.setFontName(fontName);
        font.setBold(isBold);
        if (colorIndex != null) {
            font.setColor(colorIndex);
        }
        return font;
    }

    private void validateExportParams(HttpServletResponse response, String fileName, List<?> dataList, Class<?> clazz) {
        if (response == null) {
            throw exception(EXCEL_RSP_CAN_NOT_BE_NULL);
        }

        if (!StringUtils.hasText(fileName)) {
            throw exception(EXCEL_FILENAME_CAN_NOT_BE_EMPTY);
        }

        if (clazz == null) {
            throw exception(EXCEL_CLASS_CAN_NOT_BE_NULL);
        }

        if (CollectionUtils.isEmpty(dataList)) {
            throw exception(EXCEL_DATA_CAN_NOT_BE_EMPTY);
        }
    }

    private void setupResponse(HttpServletResponse response, String fileName, HttpServletRequest request) throws IOException {
        String contentDisposition = "attachment; " + this.encodeFileName(fileName, request);
        response.setHeader("Content-Disposition", contentDisposition);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // 禁用缓存
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private String encodeFileName(String fileName, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String finalFileName = this.getSafeFileName(fileName);

        // IE浏览器
        if (isLegacyBrowser(userAgent)) {
            return URLEncoder.encode(finalFileName, StandardCharsets.UTF_8);
        } else { // 现代浏览器
            return "filename*=UTF-8''" + URLEncoder.encode(finalFileName, StandardCharsets.UTF_8)
                    // 空格特殊处理
                    .replaceAll("\\+", "%20");
        }
    }

    private static boolean isLegacyBrowser(String userAgent) {
        if (userAgent == null) return false;
        String lowerAgent = userAgent.toLowerCase();
        return lowerAgent.contains("msie") || lowerAgent.contains("trident/");
    }


}




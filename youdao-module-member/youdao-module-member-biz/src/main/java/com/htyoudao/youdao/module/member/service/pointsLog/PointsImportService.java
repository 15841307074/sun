package com.htyoudao.youdao.module.member.service.pointsLog;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.framework.excel.core.handler.Custemhandler;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.infra.api.download.DownloadApi;
import com.htyoudao.youdao.module.infra.api.file.FileApi;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.PointsUpdateErrorDTO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.PointsUpdateImportDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTaskError;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsImportTaskErrorMapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PointsImportService {

    @Autowired
    private PointsImportTaskService importTaskService;

    @Resource
    private IPointsLogService pointsLogService;

    @Resource
    private ExcelActionService excelActionService;

    @Autowired
    private PointsImportTaskErrorMapper pointsImportTaskErrorMapper;

    private static final String UPLOAD_TMP_DIR= "/tmpfile/";

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

    /**
     * 异步导入更新收货地址、快递单号和快递公司
     */
    @Async
    @DS(DsNameConstants.SHARDING)
    public void processUpdateImportAsync(byte[] fileBytes, String filename, String taskId) {
        List<PointsImportTaskService.ErrorRecord> errorRecords = new ArrayList<>();

        List<PointsUpdateImportDTO> updateList;
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            // 复用原有的 Excel 导入逻辑，进行强校验
            updateList = excelActionService.importExcel(inputStream, PointsUpdateImportDTO.class);
        } catch (Exception e) {
            log.error("异步导入任务解析Excel失败", e);
            importTaskService.updateTaskFailed(taskId, "文件解析异常：" + e.getMessage());
            return;
        }

        if (updateList == null || updateList.isEmpty()) {
            importTaskService.updateTaskFailed(taskId, "文件中没有数据");
            return;
        }

        // 批量处理更新（如果有任何错误，整批都不更新）
        boolean hasError = processUpdateBatch(updateList, errorRecords);

        // 保存错误记录到PointsImportTaskError表
        importTaskService.saveErrorRecordsToPoints(taskId, errorRecords);

        // 更新任务状态
        if (hasError) {
            // 有错误，任务失败，生成错误Excel文件
            String errorFileUrl = generateErrorExcel(updateList, errorRecords, taskId);

            // 更新任务状态为失败，并记录错误文件URL
            String errorMessage = buildErrorMessage(errorRecords);
            importTaskService.updateTaskFailedWithErrorFile(taskId, "导入失败：" + errorMessage, errorFileUrl);

            // 同时更新成功和失败条数（失败时成功数为0，失败数为总条数）
            importTaskService.updateTaskCounts(taskId, updateList.size(), 0, updateList.size());

            log.info("导入任务失败，taskId: {}, 总数: {}, 成功: 0, 失败: {}, 错误文件: {}, 原因: {}",
                    taskId, updateList.size(), errorRecords.size(), errorFileUrl, errorMessage);
        } else {
            // 无错误，任务成功，成功总条数，失败0条
            importTaskService.updateTaskSuccess(taskId, updateList.size(), updateList.size(), 0);
            log.info("导入任务完成，taskId: {}, 总数: {}, 成功: {}, 失败: 0",
                    taskId, updateList.size(), updateList.size());
        }
    }

    /**
     * 生成错误Excel文件（借用导出架构）
     * @param originalList 原始数据列表
     * @param errorRecords 错误记录列表
     * @param taskId 任务ID
     * @return 错误文件的URL
     */
    private String generateErrorExcel(List<PointsUpdateImportDTO> originalList,
                                      List<PointsImportTaskService.ErrorRecord> errorRecords,
                                      String taskId) {
        // 构建错误数据列表（包含失败原因）
        List<PointsUpdateErrorDTO> errorDataList = buildErrorDataList(originalList, errorRecords);

        if (errorDataList.isEmpty()) {
            log.warn("没有错误数据需要生成Excel");
            return null;
        }

        // 生成文件名
        String fileName = "导入错误数据" + taskId + "_" + System.currentTimeMillis() + ".xlsx";
        String safeFileName = getSafeFileName(fileName);
        safeFileName = UPLOAD_TMP_DIR + safeFileName;
        log.info("==> 开始生成错误Excel文件: {}", safeFileName);

        try (FileOutputStream os = new FileOutputStream(safeFileName)) {
            ExcelWriter excelWriter = null;
            try {
                // 创建ExcelWriter，添加样式（复用导出样式）
                excelWriter = EasyExcel.write(os, PointsUpdateErrorDTO.class)
                        .registerWriteHandler(new Custemhandler())
                        .registerWriteHandler(getStyleStrategy())
                        .inMemory(false)
                        .build();

                // 创建Sheet
                WriteSheet writeSheet = EasyExcel.writerSheet("错误数据").build();

                // 分批写入数据，避免内存溢出（参考导出逻辑）
                int dynamicBatchSize = 50000;
                int totalCount = errorDataList.size();
                int sheetRowCount = 0;
                final int maxSheetRows = 1048570;

                log.info("错误数据总量: {} 条", totalCount);

                for (int i = 0; i < totalCount; i += dynamicBatchSize) {
                    int endIndex = Math.min(i + dynamicBatchSize, totalCount);
                    List<PointsUpdateErrorDTO> batch = errorDataList.subList(i, endIndex);

                    // 检查是否需要切换Sheet（防止单Sheet超过104万行）
                    if (sheetRowCount + batch.size() > maxSheetRows) {
                        // 创建新Sheet
                        writeSheet = EasyExcel.writerSheet("错误数据_" + (writeSheet.getSheetNo() + 1)).build();
                        sheetRowCount = 0;
                        log.info("创建新Sheet: {}", writeSheet.getSheetName());
                    }

                    // 写入批次数据
                    excelWriter.write(batch, writeSheet);
                    sheetRowCount += batch.size();

                    log.info("写入错误数据进度: {}/{}", endIndex, totalCount);
                }

                log.info("错误Excel文件生成成功: {}", safeFileName);

            } catch (Exception e) {
                log.error("生成错误Excel文件失败", e);
                return null;
            } finally {
                if (excelWriter != null) {
                    try {
                        excelWriter.finish();
                    } catch (Exception e) {
                        log.warn("关闭ExcelWriter时发生异常", e);
                    }
                }
            }

            // 上传到OSS（复用导出逻辑）
            File file = new File(safeFileName);
            log.info("上传错误文件到OSS开始，文件大小: {} bytes", file.length());

            try {
                String url = fileApi.multiPartCreateFile(
                        FileMultiPartCreateReqDTO.builder()
                                .file(file)
                                .build()
                ).getCheckedData();

                if (url == null || url.isEmpty()) {
                    log.warn("上传错误文件到OSS失败，文件名: {}", safeFileName);
                    return null;
                }

                log.info("上传错误文件到OSS成功，URL: {}", url);
                return url;
            } finally {
                // 删除临时文件（复用导出逻辑）
                deleteFileIfExist(file);
            }

        } catch (Exception e) {
            log.error("生成错误Excel文件异常", e);
            return null;
        }
    }

    /**
     * 构建错误数据列表（包含失败原因）
     */
    private List<PointsUpdateErrorDTO> buildErrorDataList(List<PointsUpdateImportDTO> originalList,
                                                          List<PointsImportTaskService.ErrorRecord> errorRecords) {
        List<PointsUpdateErrorDTO> errorDataList = new ArrayList<>();

        // 将错误原因映射到对应行索引
        Map<Integer, String> errorMessageMap = new HashMap<>();
        for (PointsImportTaskService.ErrorRecord record : errorRecords) {
            if (record.getOriginalData() instanceof PointsUpdateImportDTO) {
                PointsUpdateImportDTO errorDTO = (PointsUpdateImportDTO) record.getOriginalData();
                // 使用 pointsLogId 匹配找到对应的行索引
                for (int i = 0; i < originalList.size(); i++) {
                    PointsUpdateImportDTO dto = originalList.get(i);
                    if (dto.getPointsLogId() != null &&
                            dto.getPointsLogId().equals(errorDTO.getPointsLogId())) {
                        errorMessageMap.put(i, record.getErrorMessage());
                        break;
                    }
                }
            }
        }

        // 构建包含失败原因的数据列表
        for (int i = 0; i < originalList.size(); i++) {
            PointsUpdateImportDTO dto = originalList.get(i);
            PointsUpdateErrorDTO errorDTO = new PointsUpdateErrorDTO();

            // 复制原始数据
            errorDTO.setPointsLogId(dto.getPointsLogId());
            errorDTO.setMemberId(dto.getMemberId());
            errorDTO.setProductTypeName(dto.getProductTypeName());
            errorDTO.setLogCode(dto.getLogCode());
            errorDTO.setProductName(dto.getProductName());
            errorDTO.setCreateTime(dto.getCreateTime());
            errorDTO.setProductPrice(dto.getProductPrice());
            errorDTO.setMemberNickName(dto.getMemberNickName());
            errorDTO.setMemberMobile(dto.getMemberMobile());
            errorDTO.setReceiveAddress(dto.getReceiveAddress());
            errorDTO.setTrackingNumber(dto.getTrackingNumber());
            errorDTO.setExpressCompany(dto.getExpressCompany());

            String errorMessage = errorMessageMap.get(i);
            errorDTO.setErrorMessage(StringUtils.isNotBlank(errorMessage) ? errorMessage : "");

            errorDataList.add(errorDTO);
        }

        return errorDataList;
    }

    /**
     * 构建错误信息
     */
    private String buildErrorMessage(List<PointsImportTaskService.ErrorRecord> errorRecords) {
        if (CollectionUtils.isEmpty(errorRecords)) {
            return "未知错误";
        }

        // 最多显示前3条错误信息
        int maxShow = Math.min(3, errorRecords.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxShow; i++) {
            PointsImportTaskService.ErrorRecord record = errorRecords.get(i);
            sb.append(record.getErrorMessage());
            if (i < maxShow - 1) {
                sb.append("；");
            }
        }

        if (errorRecords.size() > maxShow) {
            sb.append("等").append(errorRecords.size()).append("条错误");
        }

        return sb.toString();
    }

    /**
     * 批量处理更新（只要有一行失败，整批都不更新）
     * @return true: 有错误，整批失败; false: 无错误，可以更新
     */
    private boolean processUpdateBatch(List<PointsUpdateImportDTO> updateList,
                                       List<PointsImportTaskService.ErrorRecord> errorRecords) {
        if (updateList.isEmpty()) {
            return false;
        }

        BatchUpdateContext context = prepareBatchUpdateContext(updateList);
        Map<Integer, String> checkResults = doBatchUpdateCheck(context);

        boolean hasError = false;
        for (int i = 0; i < updateList.size(); i++) {
            PointsUpdateImportDTO dto = updateList.get(i);
            String errorMessage = checkResults.get(i);

            if (StringUtils.isNotBlank(errorMessage)) {
                hasError = true;
                errorRecords.add(new PointsImportTaskService.ErrorRecord(dto, errorMessage));
            }
        }

        if (hasError) {
            log.warn("检测到导入数据存在错误，整批放弃更新。总数据量：{}，错误数量：{}",
                    updateList.size(), errorRecords.size());
            return true;
        }

        if (!context.successList.isEmpty()) {
            boolean updateSuccess = batchUpdate(context.successList, errorRecords);
            if (!updateSuccess) {
                log.error("批量更新失败，整批数据导入失败");
                return true;
            }
        }

        return false;
    }

    /**
     * 批量更新数据库
     * @return true: 更新成功, false: 更新失败
     */
    private boolean batchUpdate(List<PointsUpdateImportDTO> successList,
                                List<PointsImportTaskService.ErrorRecord> errorRecords) {
        if (CollectionUtils.isEmpty(successList)) {
            return true;
        }

        // 收集所有需要更新的 pointsLogId (String 类型)
        List<String> pointsLogIds = successList.stream()
                .map(PointsUpdateImportDTO::getPointsLogId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        // 批量查询出完整的 PointsLogDO 对象（使用 pointsLogId）
        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PointsLogDO::getPointsLogId, pointsLogIds);
        // 如果还需要过滤积分类型
        // queryWrapper.notIn(PointsLogDO::getPointsType, Arrays.asList(1, 4));
        List<PointsLogDO> existLogs = pointsLogService.list(queryWrapper);

        Map<String, PointsLogDO> pointsLogIdMap = existLogs.stream()
                .collect(Collectors.toMap(
                        pointsLogDO -> String.valueOf(pointsLogDO.getPointsLogId()),  // Long 转 String
                        Function.identity()
                ));

        // 准备批量更新的数据
        List<PointsLogDO> updateList = new ArrayList<>();
        for (PointsUpdateImportDTO dto : successList) {
            PointsLogDO existingLog = pointsLogIdMap.get(dto.getPointsLogId());
            if (existingLog != null) {
                PointsLogDO updateDO = new PointsLogDO();
                updateDO.setPointsLogId(existingLog.getPointsLogId());
                updateDO.setTrackingNumber(dto.getTrackingNumber());
                updateDO.setExpressCompany(dto.getExpressCompany());
                updateList.add(updateDO);
            } else {
                errorRecords.add(new PointsImportTaskService.ErrorRecord(dto,
                        String.format("兑换编码[%s]在系统中不存在", dto.getPointsLogId())));
                return false;
            }
        }

        // 批量更新
        if (!updateList.isEmpty()) {
            try {
                boolean success = pointsLogService.updateBatchById(updateList);
                if (success) {
                    log.info("批量更新成功，更新数量：{}", updateList.size());
                    return true;
                } else {
                    errorRecords.add(new PointsImportTaskService.ErrorRecord(null,
                            "批量更新数据库失败"));
                    return false;
                }
            } catch (Exception e) {
                log.error("批量更新失败", e);
                errorRecords.add(new PointsImportTaskService.ErrorRecord(null,
                        "批量更新数据库失败：" + e.getMessage()));
                return false;
            }
        }

        return true;
    }

    /**
     * 准备批量更新上下文
     */
    private BatchUpdateContext prepareBatchUpdateContext(List<PointsUpdateImportDTO> updateList) {
        BatchUpdateContext context = new BatchUpdateContext();
        context.allList = new ArrayList<>(updateList);
        context.successList = new ArrayList<>();

        // 收集所有 pointsLogId (String 类型)
        context.allPointsLogIds = updateList.stream()
                .map(PointsUpdateImportDTO::getPointsLogId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        return context;
    }

    private Map<Integer, String> doBatchUpdateCheck(BatchUpdateContext context) {
        Map<Integer, String> checkResults = new HashMap<>();

        // 用于记录每个 pointsLogId 出现的所有行号
        Map<String, List<Integer>> pointsLogIdRowsMap = new HashMap<>();

        // 查询系统中存在的 pointsLogId
        Set<String> existPointsLogIdSet = getExistPointsLogIds(context.allPointsLogIds);

        // 第一遍遍历：收集每个 pointsLogId 出现的所有行号
        for (int i = 0; i < context.allList.size(); i++) {
            PointsUpdateImportDTO dto = context.allList.get(i);
            if (StringUtils.isNotBlank(dto.getPointsLogId())) {
                String pointsLogId = dto.getPointsLogId();
                int rowNum = i + 2;
                pointsLogIdRowsMap.computeIfAbsent(pointsLogId, k -> new ArrayList<>()).add(rowNum);
            }
        }

        // 第二遍遍历：进行校验并生成错误信息
        for (int i = 0; i < context.allList.size(); i++) {
            PointsUpdateImportDTO dto = context.allList.get(i);
            int rowNum = i + 2;
            StringBuilder errorMsg = new StringBuilder();

            // 检查兑换编码是否为空
            if (StringUtils.isBlank(dto.getPointsLogId())) {
                errorMsg.append(String.format("第%d行：兑换编码不能为空；", rowNum));
            } else {
                String pointsLogId = dto.getPointsLogId();
                List<Integer> rows = pointsLogIdRowsMap.get(pointsLogId);

                // 检查同一表格内兑换编码是否重复
                if (rows != null && rows.size() > 1) {
                    String repeatRows = rows.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining("、"));
                    errorMsg.append(String.format("第%d行：兑换编码[%s]在表格中存在重复（重复行：第%s行）；",
                            rowNum, pointsLogId, repeatRows));
                } else {
                    // 检查系统中是否存在
                    if (!existPointsLogIdSet.contains(pointsLogId)) {
                        errorMsg.append(String.format("兑换编码[%s]在系统中不存在；", pointsLogId));
                    }
                }
            }

            // 检查快递单号是否为空
            if (StringUtils.isBlank(dto.getTrackingNumber())) {
                errorMsg.append(String.format("第%d行：快递单号不能为空；", rowNum));
            }

            // 检查快递公司是否为空
            if (StringUtils.isBlank(dto.getExpressCompany())) {
                errorMsg.append(String.format("第%d行：快递公司不能为空；", rowNum));
            }

            if (errorMsg.length() > 0) {
                checkResults.put(i, errorMsg.toString());
            } else {
                context.successList.add(dto);
            }
        }

        return checkResults;
    }

    /**
     * 查询系统中存在的兑换编码 (String 类型)
     */
    private Set<String> getExistPointsLogIds(List<String> allPointsLogIds) {
        if (CollectionUtils.isEmpty(allPointsLogIds)) {
            return new HashSet<>();
        }

        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();
        // 如果需要过滤积分类型
        // queryWrapper.notIn(PointsLogDO::getPointsType, Arrays.asList(1, 4));
        queryWrapper.in(PointsLogDO::getPointsLogId, allPointsLogIds);
        queryWrapper.select(PointsLogDO::getPointsLogId);

        List<PointsLogDO> list = pointsLogService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            // 将 Long 类型的 pointsLogId 转换为 String 类型
            return list.stream()
                    .map(PointsLogDO::getPointsLogId)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * 获取安全的文件名
     */
    private String getSafeFileName(String fileName) {
        if (fileName == null) {
            fileName = "temp_" + System.currentTimeMillis();
        }
        // 过滤非法字符
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return fileName;
    }

    /**
     * 删除临时文件
     */
    private void deleteFileIfExist(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("删除临时文件失败: {}", file.getAbsolutePath());
            } else {
                log.debug("删除临时文件成功: {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 获取样式策略（复用导出样式）
     */
    private HorizontalCellStyleStrategy getStyleStrategy() {
        // 表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);

        // 内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 批量更新上下文内部类
     */
    @Data
    private static class BatchUpdateContext {
        private List<PointsUpdateImportDTO> allList;
        private List<PointsUpdateImportDTO> successList;
        private List<String> allPointsLogIds;
    }


}

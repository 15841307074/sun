package com.htyoudao.youdao.module.commodity.service.inventory;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportTask;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportTaskError;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.ImportTaskErrorMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.ImportTaskMapper;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class ImportTaskService {
    
    @Autowired
    private ImportTaskMapper importTaskMapper;
    
    @Autowired
    private ImportTaskErrorMapper importTaskErrorMapper;
    
    /**
     * 创建导入任务
     */
    public String createTask(String fileName, String channelType) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        
        ImportTask task = new ImportTask();
        task.setTaskId(taskId);
        task.setFileName(fileName);
        task.setChannelType(channelType);
        task.setStatus(ImportTask.ImportStatus.PROCESSING.name());
        importTaskMapper.insert(task);
        return taskId;
    }
    
    /**
     * 更新任务成功状态
     */
    public void updateTaskSuccess(String taskId, int totalCount, int successCount, int errorCount) {
        ImportTask task = importTaskMapper.selectOne(ImportTask::getTaskId, taskId);
        
        if (task != null) {
            task.setStatus(ImportTask.ImportStatus.COMPLETED.name());
            task.setTotalCount(totalCount);
            task.setSuccessCount(successCount);
            task.setErrorCount(errorCount);
            importTaskMapper.updateById(task);
        }
    }
    
    /**
     * 更新任务失败状态
     */
    public void updateTaskFailed(String taskId, String errorMessage) {
        ImportTask task = importTaskMapper.selectOne(
            new QueryWrapper<ImportTask>().eq("task_id", taskId)
        );

        if (task != null) {
            task.setStatus(ImportTask.ImportStatus.FAILED.name());
            task.setRemark(errorMessage);
            importTaskMapper.updateById(task);
        }
    }
    
    /**
     * 保存错误信息
     */
    public void saveErrorRecords(String taskId, List<ErrorRecord> errorRecords) {
        if (errorRecords == null || errorRecords.isEmpty()) {
            return;
        }

        for (ErrorRecord error : errorRecords) {
            ImportTaskError taskError = new ImportTaskError();
            taskError.setTaskId(taskId);

            // 将原始数据转换为JSON字符串
            try {
                String rowData = JSONUtil.toJsonStr(error.getOriginalData());
                taskError.setRowData(rowData);
            } catch (Exception e) {
                log.error("", e);
                taskError.setRowData("{}");
            }

            taskError.setErrorMessage(error.getErrorMessage());
            importTaskErrorMapper.insert(taskError);
        }
    }

    /**
     * 获取任务详情
     */
    public ImportTask getTask(String taskId) {
        return importTaskMapper.selectOne(
            new QueryWrapper<ImportTask>().eq("task_id", taskId)
        );
    }
    
    /**
     * 获取所有任务列表
     */
    public PageResult<ImportTask> getAllTasks(PageParam pageParam) {
        LambdaQueryWrapperX<ImportTask> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(ImportTask::getCreator, WebFrameworkUtils.getLoginUserId().toString());
        queryWrapperX.orderByDesc(ImportTask::getCreateTime);
        return importTaskMapper.selectPage(pageParam ,queryWrapperX);
    }
    
    /**
     * 获取任务的错误记录
     */
    public List<ImportTaskError> getTaskErrors(String taskId) {
        return importTaskErrorMapper.selectList(ImportTaskError::getTaskId,taskId);
    }
    
    /**
     * 设置错误文件URL
     */
    public void setErrorFileUrl(String taskId, String fileUrl) {
        ImportTask task = importTaskMapper.selectOne(
            new QueryWrapper<ImportTask>().eq("task_id", taskId)
        );
        
        if (task != null) {
            task.setErrorFileUrl(fileUrl);
            importTaskMapper.updateById(task);
        }
    }
    
    /**
     * 错误记录包装类
     */
    @Data
    @AllArgsConstructor
    public static class ErrorRecord {
        private Object originalData;
        private String errorMessage;
    }
}
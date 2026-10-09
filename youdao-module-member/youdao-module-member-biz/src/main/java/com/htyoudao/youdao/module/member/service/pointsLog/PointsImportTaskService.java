package com.htyoudao.youdao.module.member.service.pointsLog;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTask;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTaskError;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsImportTaskErrorMapper;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsImportTaskMapper;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class PointsImportTaskService {
    
    @Autowired
    private PointsImportTaskMapper importTaskMapper;
    
    @Autowired
    private PointsImportTaskErrorMapper importTaskErrorMapper;

//    @DubboReference
//    private AdminUserApi adminUserApi;

    /**
     * 创建导入任务
     */
    public String createTask(String fileName, String channelType) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        PointsImportTask task = new PointsImportTask();
        task.setTaskId(taskId);
        task.setFileName(fileName);
        task.setStatus(PointsImportTask.ImportStatus.PROCESSING.name());
        importTaskMapper.insert(task);
        return taskId;
    }

    /**
     * 更新任务成功状态
     */
    public void updateTaskSuccess(String taskId, int totalCount, int successCount, int errorCount) {
        PointsImportTask task = importTaskMapper.selectOne(PointsImportTask::getTaskId, taskId);

        if (task != null) {
            task.setStatus(PointsImportTask.ImportStatus.COMPLETED.name());
            task.setTotalCount(totalCount);
            task.setSuccessCount(successCount);
            task.setErrorCount(errorCount);
            importTaskMapper.updateById(task);
        }
    }
    /**
     * 更新任务的统计数据（用于失败场景）
     */
    public void updateTaskCounts(String taskId, int totalCount, int successCount, int errorCount) {
        PointsImportTask task = importTaskMapper.selectOne(
                new LambdaQueryWrapper<PointsImportTask>()
                        .eq(PointsImportTask::getTaskId, taskId)
        );

        if (task != null) {
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
        PointsImportTask task = importTaskMapper.selectOne(
            new QueryWrapper<PointsImportTask>().eq("task_id", taskId)
        );

        if (task != null) {
            task.setStatus(PointsImportTask.ImportStatus.FAILED.name());
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
            PointsImportTaskError taskError = new PointsImportTaskError();
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
    public PointsImportTask getTask(String taskId) {
        return importTaskMapper.selectOne(
            new QueryWrapper<PointsImportTask>().eq("task_id", taskId)
        );
    }

    /**
     * 获取所有任务列表
     */
    public PageResult<PointsImportTask> getAllTasks(PageParam pageParam) {
        LambdaQueryWrapperX<PointsImportTask> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(PointsImportTask::getCreator, WebFrameworkUtils.getLoginUserId().toString());
        queryWrapperX.orderByDesc(PointsImportTask::getCreateTime);
        PageResult<PointsImportTask> pointsImportTaskPageResult = importTaskMapper.selectPage(pageParam, queryWrapperX);
        List<PointsImportTask> list = pointsImportTaskPageResult.getList();
        if(ObjectUtil.isNotEmpty(list)){
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            Map<String, String> info = loginUser.getInfo();
            String nickname = info.get("nickname");
            for (PointsImportTask pointsImportTask : list) {
                pointsImportTask.setUserName(nickname);
//                CommonResult<AdminUserRespDTO> user = adminUserApi.getUser(pointsImportTask.getId());
//                if(user!=null){
//                    AdminUserRespDTO data = user.getData();
//                    if(data!=null){
//                        pointsImportTask.setUserName(data.getNickname());
//                    }
//
//                }

            }

        }else {
            if(ObjectUtil.isNotEmpty(pointsImportTaskPageResult)){
                pointsImportTaskPageResult.setList(new ArrayList<>());
            }

        }
        return pointsImportTaskPageResult;
    }

    /**
     * 获取任务的错误记录
     */
    public List<PointsImportTaskError> getTaskErrors(String taskId) {
        return importTaskErrorMapper.selectList(PointsImportTaskError::getTaskId,taskId);
    }

    /**
     * 设置错误文件URL
     */
    public void setErrorFileUrl(String taskId, String fileUrl) {
        PointsImportTask task = importTaskMapper.selectOne(
            new QueryWrapper<PointsImportTask>().eq("task_id", taskId)
        );

        if (task != null) {
            task.setErrorFileUrl(fileUrl);
            importTaskMapper.updateById(task);
        }
    }

    /**
     * 创建更新导入任务
     */
    public String createUpdateTask(String fileName) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        PointsImportTask task = new PointsImportTask();
        task.setTaskId(taskId);
        task.setFileName(fileName);
        task.setChannelType("UPDATE_ADDRESS");  // 新增任务类型：更新地址和快递单号
        task.setStatus(PointsImportTask.ImportStatus.PROCESSING.name());
        importTaskMapper.insert(task);
        return taskId;
    }

    /**
     * 检查是否有进行中的更新任务
     */
    public String getProcessingUpdateTask() {
        PointsImportTask task = importTaskMapper.selectOne(
                new LambdaQueryWrapperX<PointsImportTask>()
                        .eq(PointsImportTask::getChannelType, "UPDATE_ADDRESS")
                        .eq(PointsImportTask::getStatus, PointsImportTask.ImportStatus.PROCESSING.name())
                        .orderByDesc(PointsImportTask::getCreateTime)
                        .last("limit 1")
        );
        return task != null ? task.getTaskId() : null;
    }

    /**
     * 保存错误信息到PointsImportTaskError表
     */
    public void saveErrorRecordsToPoints(String taskId, List<ErrorRecord> errorRecords) {
        if (errorRecords == null || errorRecords.isEmpty()) {
            return;
        }

        for (ErrorRecord error : errorRecords) {
            PointsImportTaskError taskError = new PointsImportTaskError();
            taskError.setTaskId(taskId);

            // 将原始数据转换为JSON字符串
            try {
                String rowData = JSONUtil.toJsonStr(error.getOriginalData());
                taskError.setRowData(rowData);
            } catch (Exception e) {
                log.error("转换JSON失败", e);
                taskError.setRowData("{}");
            }

            taskError.setErrorMessage(error.getErrorMessage());
            importTaskErrorMapper.insert(taskError);
        }
    }

    /**
     * 更新任务失败状态，并记录错误文件URL
     */
    public void updateTaskFailedWithErrorFile(String taskId, String errorMessage, String errorFileUrl) {
        PointsImportTask task = importTaskMapper.selectOne(
                new LambdaQueryWrapper<PointsImportTask>()
                        .eq(PointsImportTask::getTaskId, taskId)
        );

        if (task != null) {
            task.setStatus(PointsImportTask.ImportStatus.FAILED.name());
            task.setRemark(errorMessage);
            task.setErrorFileUrl(errorFileUrl);
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
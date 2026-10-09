package com.htyoudao.youdao.module.bpm.service.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceQueryReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceQueryTaskCountReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.task.BpmProcessInstanceCopyMapper;
import com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.bpm.service.definition.BpmProcessDefinitionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 流程抄送 Service 实现类
 *
 * @author kyle
 */
@Service
@Validated
@Slf4j
public class BpmProcessInstanceCopyServiceImpl implements BpmProcessInstanceCopyService {

    @Resource
    private BpmProcessInstanceCopyMapper processInstanceCopyMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmTaskService taskService;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessInstanceService processInstanceService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public void createProcessInstanceCopy(Collection<Long> userIds, String reason, String taskId) {
        Task task = taskService.getTask(taskId);
        if (ObjectUtil.isNull(task)) {
            throw exception(ErrorCodeConstants.TASK_NOT_EXISTS);
        }
        // 执行抄送
        createProcessInstanceCopy(userIds, reason,
                task.getProcessInstanceId(), task.getTaskDefinitionKey(), task.getId(), task.getName());
    }

    @Override
    public void createProcessInstanceCopy(Collection<Long> userIds, String reason, String processInstanceId,
                                          String activityId, String activityName, String taskId) {
        // 1.1 校验流程实例存在
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 1.2 校验流程定义存在
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                processInstance.getProcessDefinitionId());
        if (processDefinition == null) {
            throw exception(ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS);
        }

        // 2. 创建抄送流程
        List<BpmProcessInstanceCopyDO> copyList = convertList(userIds, userId -> new BpmProcessInstanceCopyDO()
                .setUserId(userId).setReason(reason).setStartUserId(Long.valueOf(processInstance.getStartUserId()))
                .setProcessInstanceId(processInstanceId).setProcessInstanceName(processInstance.getName())
                .setCategory(processDefinition.getCategory()).setTaskId(taskId)
                .setActivityId(activityId).setActivityName(activityName)
                .setProcessDefinitionId(processInstance.getProcessDefinitionId()));
        processInstanceCopyMapper.insertBatch(copyList);
    }

    @Override
    public PageResult<BpmProcessInstanceCopyDO> getProcessInstanceCopyPage(Long userId,
                                                                           BpmProcessInstanceCopyPageReqVO pageReqVO) {
        return processInstanceCopyMapper.selectPage(userId, pageReqVO);
    }


    @Override
    public PageResult<BpmBusinessQueryDO> getBpmProcessInstanceCopyPage(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        LambdaQueryWrapper<BpmProcessInstanceCopyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BpmProcessInstanceCopyDO::getUserId, loginUserId);

        if (CollectionUtil.isNotEmpty(pIds)){
            wrapper.in(BpmProcessInstanceCopyDO::getProcessInstanceId,pIds);
        }

        List<BpmProcessInstanceCopyDO> list = processInstanceCopyMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(list)){
            return PageResult.empty();
        }
        // 取出所有的流程唯一标识
        List<String> procInstIds = list.stream()
                // 提取每个 Task 的 processInstanceId
                .map(BpmProcessInstanceCopyDO::getProcessInstanceId)
                // 过滤可能的 null 值（可选，根据业务场景）
                .filter(StrUtil::isNotEmpty)
                // 收集为 List<String>
                .toList();
        bpmProcessInstanceQueryReqVO.setIsAll(5);
        bpmProcessInstanceQueryReqVO.setProInstIdList(procInstIds);
        return processInstanceService.queryBusinessTask(bpmProcessInstanceQueryReqVO);
    }

    @Override
    public List<String> getBpmProcessInstanceCopyList(List<String> userIds, List<String> pIds) {
        LambdaQueryWrapper<BpmProcessInstanceCopyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BpmProcessInstanceCopyDO::getUserId, userIds);

        if (CollectionUtil.isNotEmpty(pIds)){
            wrapper.in(BpmProcessInstanceCopyDO::getProcessInstanceId, pIds);
        }
        List<BpmProcessInstanceCopyDO> list = processInstanceCopyMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(list)){
            return null;
        }
        // 取出所有的流程唯一标识
        return list.stream()
                // 提取每个 Task 的 processInstanceId
                .map(BpmProcessInstanceCopyDO::getProcessInstanceId)
                // 过滤可能的 null 值（可选，根据业务场景）
                .filter(StrUtil::isNotEmpty)
                // 收集为 List<String>
                .toList();
    }

    @Override
    public void updateBpmProcessInstanceCopyPageFlag(List<BpmBusinessQueryDO> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            List<BpmBusinessQueryDO> filteredList = list.stream()
                    // 过滤条件：copyFlag 不为 null 且等于 0
                    .filter(item -> item.getCopyFlag() == null || item.getCopyFlag() == 0)
                    // 收集为新的 List
                    .toList();
            processInstanceService.batchBpmProcessInstanceCopyPageFlag(filteredList);
        }

    }

    @Override
    public Object getBpmProcessInstanceCountCopyPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, List<String> pIds) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        LambdaQueryWrapper<BpmProcessInstanceCopyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BpmProcessInstanceCopyDO::getUserId, loginUserId);

        if (CollectionUtil.isNotEmpty(pIds)) {
            wrapper.in(BpmProcessInstanceCopyDO::getProcessInstanceId, pIds);
        }

        List<BpmProcessInstanceCopyDO> list = processInstanceCopyMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(list)){
            return 0;
        }
        // 取出所有的流程唯一标识
        List<String> procInstIds = list.stream()
                // 提取每个 Task 的 processInstanceId
                .map(BpmProcessInstanceCopyDO::getProcessInstanceId)
                // 过滤可能的 null 值（可选，根据业务场景）
                .filter(StrUtil::isNotEmpty)
                // 收集为 List<String>
                .toList();
        bpmProcessInstanceQueryReqVO.setProInstIdList(procInstIds);
        return processInstanceService.queryBusinessTaskCopyCount(bpmProcessInstanceQueryReqVO);
    }

    @Override
    public void deleteProcessInstanceCopy(String processInstanceId) {
        processInstanceCopyMapper.deleteByProcessInstanceId(processInstanceId);
    }

}

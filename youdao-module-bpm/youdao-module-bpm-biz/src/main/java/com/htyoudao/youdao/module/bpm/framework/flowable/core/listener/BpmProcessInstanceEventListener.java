package com.htyoudao.youdao.module.bpm.framework.flowable.core.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.BpmProcessBusinessMapper;
import com.htyoudao.youdao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.util.FlowableUtils;
import com.htyoudao.youdao.module.bpm.service.task.BpmProcessInstanceService;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Resource;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 监听 {@link ProcessInstance} 的状态变更，更新其对应的 status 状态
 *
 * @author jason
 */
@Component
public class BpmProcessInstanceEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    private BpmProcessBusinessMapper bpmProcessBusinessMapper;

    public static final Set<FlowableEngineEventType> PROCESS_INSTANCE_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.PROCESS_CREATED)
            .add(FlowableEngineEventType.PROCESS_COMPLETED)
            .add(FlowableEngineEventType.PROCESS_CANCELLED)
            .build();

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    public BpmProcessInstanceEventListener(){
        super(PROCESS_INSTANCE_EVENTS);
    }

    @Override
    protected void processCreated(FlowableEngineEntityEvent event) {
        ProcessInstance processInstance = (ProcessInstance) event.getEntity();
        FlowableUtils.execute(processInstance.getTenantId(),
                () -> processInstanceService.processProcessInstanceCreated(processInstance));
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {

        ProcessInstance processInstance = (ProcessInstance) event.getEntity();

        LambdaQueryWrapper<BpmBusinessDO> query = new LambdaQueryWrapper<>();
        query.eq(BpmBusinessDO::getProcInstId, processInstance.getProcessInstanceId());
        BpmBusinessDO businessDO = bpmProcessBusinessMapper.selectOne(query);
        Integer taskState = businessDO.getTaskState();
        if(!ObjectUtil.equal(taskState, BpmProcessInstanceStatusEnum.RUNNING.getStatus())
                && !ObjectUtil.equal(taskState, BpmProcessInstanceStatusEnum.REJECT.getStatus())
                && !ObjectUtil.equal(taskState, BpmProcessInstanceStatusEnum.OVERDUE.getStatus())
                && !ObjectUtil.equal(taskState, BpmProcessInstanceStatusEnum.CANCEL.getStatus())){
            LambdaUpdateWrapper<BpmBusinessDO> update = new LambdaUpdateWrapper<>();
            update.eq(BpmBusinessDO::getProcInstId, processInstance.getProcessInstanceId());
            update.set(BpmBusinessDO::getTaskState, BpmProcessInstanceStatusEnum.COMPLETED.getStatus());
            bpmProcessBusinessMapper.update(update);
        }

        if(ObjectUtil.isNotEmpty(businessDO.getParentProcInstId())){
            LambdaQueryWrapper<BpmBusinessDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BpmBusinessDO::getParentProcInstId, businessDO.getParentProcInstId());
            queryWrapper.ne(BpmBusinessDO::getTaskState, BpmProcessInstanceStatusEnum.COMPLETED.getStatus());
            List<BpmBusinessDO> list = bpmProcessBusinessMapper.selectList(queryWrapper);
            //子任务全部完事 主任务也完事
            if(CollectionUtil.isEmpty(list)){
                LambdaUpdateWrapper<BpmBusinessDO> update2 = new LambdaUpdateWrapper<>();
                update2.eq(BpmBusinessDO::getProcInstId, businessDO.getParentProcInstId());
                update2.set(BpmBusinessDO::getTaskState, BpmProcessInstanceStatusEnum.COMPLETED.getStatus());
                update2.set(BpmBusinessDO::getTaskCompletionTime, LocalDateTime.now());
                bpmProcessBusinessMapper.update(update2);
            }
        }
        FlowableUtils.execute(processInstance.getTenantId(),
                () -> processInstanceService.processProcessInstanceCompleted(processInstance));
    }

    @Override
    protected void processCancelled(FlowableCancelledEvent event) {
        // 特殊情况：当跳转到 EndEvent 流程实例未结束, 会执行 deleteProcessInstance 方法
        ProcessInstance processInstance = processInstanceService.getProcessInstance(event.getProcessInstanceId());
        if (processInstance != null) {
            FlowableUtils.execute(processInstance.getTenantId(),
                    () -> processInstanceService.processProcessInstanceCompleted(processInstance));
        }
    }

}

package com.htyoudao.youdao.module.bpm.service.task;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceQueryReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceQueryTaskCountReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import jakarta.validation.constraints.NotEmpty;
import org.flowable.bpmn.model.FlowNode;

import java.util.Collection;
import java.util.List;

/**
 * 流程抄送 Service 接口
 *
 * 现在是在审批的时候进行流程抄送
 */
public interface BpmProcessInstanceCopyService {

    /**
     * 【管理员】流程实例的抄送
     *
     * @param userIds 抄送的用户编号
     * @param reason 抄送意见
     * @param taskId 流程任务编号
     */
    void createProcessInstanceCopy(Collection<Long> userIds, String reason, String taskId);

    /**
     * 【自动抄送】流程实例的抄送
     *
     * @param userIds 抄送的用户编号
     * @param reason 抄送意见
     * @param processInstanceId 流程编号
     * @param activityId 流程活动编号（对应 {@link FlowNode#getId()}）
     * @param activityName 任务编号（对应 {@link FlowNode#getName()}）
     * @param taskId 任务编号，允许空
     */
    void createProcessInstanceCopy(Collection<Long> userIds, String reason,
                                   @NotEmpty(message = "流程实例编号不能为空") String processInstanceId,
                                   @NotEmpty(message = "流程活动编号不能为空") String activityId,
                                   @NotEmpty(message = "流程活动名字不能为空") String activityName,
                                   String taskId);

    /**
     * 获得抄送的流程的分页
     *
     * @param userId 当前登录用户
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCopyDO> getProcessInstanceCopyPage(Long userId,
                                                                    BpmProcessInstanceCopyPageReqVO pageReqVO);

    /**
     * 删除抄送流程
     *
     * @param processInstanceId 流程实例 ID
     */
    void deleteProcessInstanceCopy(String processInstanceId);

    PageResult<BpmBusinessQueryDO> getBpmProcessInstanceCopyPage(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, List<String> pIds);

    List<String> getBpmProcessInstanceCopyList(List<String> userIds, List<String> pIds);


    Object getBpmProcessInstanceCountCopyPage(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, List<String> pIds);

    void updateBpmProcessInstanceCopyPageFlag(List<BpmBusinessQueryDO> bpmProcessInstanceQueryReqVO);
}

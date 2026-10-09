package com.htyoudao.youdao.module.bpm.service.definition;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.*;
import com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept.BpmAppGetSumNumByDeptRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmAllStoreInfoDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BussinessTaskStoreDO;

import java.util.List;

/**
 * 流程定义接口
 *
 * @author yunlong.li
 * @author ZJQ
 * @author 0090
 */
public interface BpmProcessBusinessService {

    /**
     * 基于流程，创建业务流程
     *
     * @param bpmBusinessDO 流程模型
     */
    void createProcessBusiness(BpmBusinessDO bpmBusinessDO);

    /**
     * 基于流程，创建业务流程
     *
     * @param list 流程模型
     */
    void createProcessBusinessBatch(List<BpmBusinessDO> list);

    void createProcessStoreInfo(List<BussinessTaskStoreDO> bpmStoreInfoDOS);
    
    /**
     * 根据业务流程ID获取业务流程信息
     * @param procInstId
     * @return
     */
    BpmBusinessDO getBusinessTask(String procInstId);

    List<BussinessTaskStoreDO> getStoreList(String procInstId, Long businessId);

    List<BpmAllStoreInfoDO> getAllStoreList(Long businessId);

    void updateProcInstId(Long taskId, String bProcessInstanceId);

    PageResult<BpmBusinessQueryDO> getBusinessTaskList(BpmProcessInstanceQueryReqVO createReqVO, PageParam pageParam);

    PageResult<BpmBusinessQueryDO> getBusinessTaskListNotAll(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam);

    Object getBusinessTaskListCountNotAll(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam);

    BpmAppGetSumNumRespVO getSumNum(BpmProcessInstanceQueryNumReqVO bpmProcessInstanceQueryReqVO);

    PageResult<BpmBusinessQueryDO> getBusinessTaskListCopy(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam);

    void extractedAllList(List<String> userIdList, List<String> proInstIdList, Integer oaProjectFlag, Long oaProjectId);

    Object getBusinessTaskListCountCopy(BpmProcessInstanceQueryTaskCountReqVO bpmProcessInstanceQueryReqVO, PageParam pageParam);

    PageResult<BpmBusinessQueryDO> queryNotRelatedBusinessTask(BpmNotRelatedReqVO bpmNotRelatedReqVO);

    PageResult<BpmBusinessQueryDO> queryOABusinessTask(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);



    /**
     * 查询任务列表并统计（不带分页）
     * 参考 queryBusinessTask 的逻辑，但不分页，最后统计数据并返回 BpmAppGetSumNumRespVO
     *
     * @param bpmProcessInstanceQueryReqVO 查询条件
     * @return 任务统计信息
     */
    BpmAppGetSumNumRespVO getBusinessTaskStatistics(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);

    List<BpmBusinessDO> selectListByOaProjectId(Long oaProjectId);


    void calculateTaskStatistics(BpmAppGetSumNumRespVO bpmAppGetSumNumRespVO, List<BpmBusinessQueryDO> bpmBusinessQueryDOS);

    List<BpmAppGetSumNumByDeptRespVO> getBusinessTaskStatisticsByDept(BpmProcessInstanceQueryReqVO queryReqVO);
}

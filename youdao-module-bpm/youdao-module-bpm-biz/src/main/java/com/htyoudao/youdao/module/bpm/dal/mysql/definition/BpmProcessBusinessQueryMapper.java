package com.htyoudao.youdao.module.bpm.dal.mysql.definition;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceQueryReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BpmProcessBusinessQueryMapper extends BaseMapperX<BpmBusinessQueryDO> {
    List<BpmBusinessQueryDO> selectTaskListByPage(@Param("queryVO") BpmProcessInstanceQueryReqVO createReqVO, @Param("page") PageParam pageParam);

    Long selectTaskListTotal(@Param("queryVO") BpmProcessInstanceQueryReqVO createReqVO, @Param("page") PageParam pageParam);

    List<BpmBusinessQueryDO> selectTaskListAll(BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);

    List<BpmBusinessQueryDO> selectTaskListAllForNum(@Param("queryVO")  BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);

    List<BpmBusinessQueryDO> updateBpmProcessInstanceCopyPageFlag(@Param("queryVO") BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);

    List<BpmBusinessQueryDO> selectTaskListCopyByPage(@Param("queryVO") BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO,
                                                      @Param("page") PageParam pageParam,
                                                      @Param("userId") Long userId);

    Long selectTaskListCopyByPageTotal(@Param("queryVO")BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO,
                                       @Param("page") PageParam pageParam,
                                       @Param("userId") Long userId);

    Long selectTaskListCopyReadTotal(@Param("queryVO")BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO,
                                       @Param("userId") Long userId);

    List<BpmBusinessQueryDO> selectMainTaskListByPage(@Param("queryVO") BpmProcessInstanceQueryReqVO createReqVO,
                                                      @Param("page") PageParam pageParam);

    Long selectMainTaskListTotal(@Param("queryVO") BpmProcessInstanceQueryReqVO createReqVO,
                                 @Param("page") PageParam pageParam);

    List<BpmBusinessQueryDO> selectMainTaskList(@Param("queryVO") BpmProcessInstanceQueryReqVO bpmProcessInstanceQueryReqVO);
}

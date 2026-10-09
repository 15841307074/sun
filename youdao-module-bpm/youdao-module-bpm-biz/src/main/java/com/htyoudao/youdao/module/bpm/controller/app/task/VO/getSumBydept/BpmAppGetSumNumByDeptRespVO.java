package com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept;

import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmAppGetSumNumRespVO;
import lombok.Data;

@Data
public class BpmAppGetSumNumByDeptRespVO extends BpmAppGetSumNumRespVO {

    private Long secondDeptId;

    private String deptName;

}

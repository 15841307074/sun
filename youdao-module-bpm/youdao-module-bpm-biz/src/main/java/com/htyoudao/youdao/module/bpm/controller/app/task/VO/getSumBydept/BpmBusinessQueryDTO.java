package com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSumBydept;

import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import lombok.Data;

import java.io.Serializable;
@Data
public class BpmBusinessQueryDTO extends BpmBusinessQueryDO implements Serializable {

    private Long secondDeptId;

}

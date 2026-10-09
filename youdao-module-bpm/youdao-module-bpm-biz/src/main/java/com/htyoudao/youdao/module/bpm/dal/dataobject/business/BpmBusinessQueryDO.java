package com.htyoudao.youdao.module.bpm.dal.dataobject.business;

import lombok.*;


/**
 * OA 请假申请 DO
 *
 * @author jason
 * @author 0090
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BpmBusinessQueryDO extends BpmBusinessDO {

    /**
     * 执行人ID
     */
    private String executorUserId;
    /**
     * 执行门店ID
     */
    private String executorStoreId;
    /**
     * 执行部门ID
     */
    private String executorDeptId;


}

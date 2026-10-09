package com.htyoudao.youdao.module.bpm.enums;

/**
 * BPM 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author 0090
 */
public interface LogRecordConstants {


    // ======================= 营销OA项目 =======================
    String BPM_OA_BUSINESS ="OA项目";

    String BPM_OA_BUSINESS_CREATE_TYPE = "新增OA项目";
    String BPM_OA_BUSINESS_CREATE_SUCCESS = "新增OA项目【{{#oaProject.projectName}}】";

    String BPM_OA_BUSINESS_UPDATE_TYPE = "修改OA项目";
    String BPM_OA_BUSINESS_UPDATE_SUCCESS = "修改OA项目【{{#oaProject.projectName}}】";

    String BPM_OA_BUSINESS_DELETE_TYPE = "删除OA项目";
    String BPM_OA_BUSINESS_DELETE_SUCCESS = "删除OA项目";


    String BPM_OA_BUSINESS_CREATE_RELATIONSHIP_TYPE = "新增OA项目关系";
    String BPM_OA_BUSINESS_CREATE_RELATIONSHIP_SUCCESS = "新增OA项目关系";

    String BPM_OA_BUSINESS_DELETE_RELATIONSHIP_TYPE = "删除OA项目关系";
    String BPM_OA_BUSINESS_DELETE_RELATIONSHIP_SUCCESS = "删除OA项目关系";

}

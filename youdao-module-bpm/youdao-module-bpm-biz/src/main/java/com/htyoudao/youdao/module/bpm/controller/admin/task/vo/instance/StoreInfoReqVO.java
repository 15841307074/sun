package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class StoreInfoReqVO {

    private Long executorStoreId;

    /**
     * 门店名称
     */
    private String executorStoreName;

    /**
     * 门店负责人 id
     */
    private Long userId;

    /**
     * 门店负责人姓名
     */
    private String storeLeader;

    /**
     * 门店负责人电话
     */
    private String storeLeaderPhone;
}

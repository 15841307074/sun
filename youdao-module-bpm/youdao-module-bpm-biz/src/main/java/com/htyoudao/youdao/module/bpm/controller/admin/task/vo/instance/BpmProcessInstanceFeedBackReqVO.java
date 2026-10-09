package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmProcessInstanceFeedBackReqVO extends PageParam {

    @Schema(description = "执行门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> executorStoreId;


    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer taskState;

    @Schema(description = "流程唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty
    private String procInstId;

    @Schema(description = "父类流程唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String parentProcInstId;


}

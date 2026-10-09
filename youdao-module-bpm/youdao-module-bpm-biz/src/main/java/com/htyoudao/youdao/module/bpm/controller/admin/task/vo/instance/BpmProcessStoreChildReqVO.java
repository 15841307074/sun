package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmProcessStoreChildReqVO {

    /**
     * 流程唯一标识
     */
    private String procInstId;
}

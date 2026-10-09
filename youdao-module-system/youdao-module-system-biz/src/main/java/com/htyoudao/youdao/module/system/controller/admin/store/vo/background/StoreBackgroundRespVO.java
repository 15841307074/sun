package com.htyoudao.youdao.module.system.controller.admin.store.vo.background;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreBackgroundRespVO {

    private Long backgroundId;
    private String templateName;
    private String backgroundImage;
    @Schema(description = "应用范围：1按门店，2按标签")
    private Integer appScope;
    @Schema(description = "门店范围：1全部门店，2部分门店")
    private Integer storeScope;
    @Schema(description = "发布状态：0关闭，1发布")
    private Integer publishStatus;
    @Schema(description = "是否系统默认模板：0否，1是")
    private Integer defaultFlag;
    private LocalDateTime releaseTime;
    private List<Long> storeIds;
    private List<Long> tagIds;
    @Schema(description = "命中的门店数量")
    private Integer applyStoreCount;
    @Schema(description = "适用门店名称，列表展示使用")
    private List<String> applyStoreNames;
}

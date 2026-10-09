package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * @author DHT
 */
@Data
@Schema(description = "报表下载 - 商品下载请求入参 Request VO")
public class ReportProductDownloadReqVO {

    @NotNull(message = "汇总")
    @Schema(description = "汇总 DAY:按天,MONTH:按月,SUMMARY:汇总")
    private String type;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    /**
     * 开始时间
     */
    @Schema(description = "结束时间")
    @NotNull(message = "结束时间不能为空")
    private String endTime;

    /**
     * 是否全部门店 0 全部门店 1 指定门店
     */
    @Schema(description = "是否全部门店")
    @NotNull(message = "是否全部门店不能为空")
    private Integer allStore;

    /**
     * 门店 ids
     */
    @Schema(description = "门店 ids")
    private Set<Long> storeIds;

    @Schema(description = "需要导出的title")
    @NotEmpty(message = "至少选择一项")
    private Set<String> fields;

    @Schema(description = "指定商品ID集合")
    private List<Long> commodityIds;
}

package com.htyoudao.youdao.module.analysis.controller.admin.vo;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RangeTimeRequestVO {

    @Schema(description = "关注指标")
    @NotBlank
    private String code;

    @Schema(description = "聚合请求参数对象")
    @NotNull
    private AggregationRequestVO aggregationRequest;

    @Schema(description = "时间段信息")
    @NotEmpty
    private List<RangeDTO> rangeHours;
}

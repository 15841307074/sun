package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 余额流水分页响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额流水分页响应参数")
public class BalanceLogPageRespVO {

    @Schema(description = "总记录数")
    private Long total;


    @Schema(description = "流水列表")
    private List<BalanceLogVO> records;
}

package com.htyoudao.youdao.module.order.controller.app.order.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "跑腿订单游标分页响应 VO")
public class ErrandOrderHallPageRespVO {

    @Schema(description = "数据列表")
    private List<ErrandOrderHallRespVO> records = new ArrayList<>();

    @Schema(description = "总量，游标分页不统计总数，固定返回 0")
    private Long total = 0L;

    @Schema(description = "下一页游标；为空表示没有下一页")
    private List<Object> searchAfter;

    public ErrandOrderHallPageRespVO() {
    }

    public ErrandOrderHallPageRespVO(List<ErrandOrderHallRespVO> records) {
        this.records = records;
    }

    public static ErrandOrderHallPageRespVO empty() {
        return new ErrandOrderHallPageRespVO();
    }
}

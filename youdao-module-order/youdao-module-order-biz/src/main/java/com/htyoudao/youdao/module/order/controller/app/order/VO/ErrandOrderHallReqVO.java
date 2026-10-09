package com.htyoudao.youdao.module.order.controller.app.order.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "跑腿接单大厅请求 VO")
public class ErrandOrderHallReqVO {

    @Schema(description = "门店ID，接单大厅必填，我的配送订单可不传", example = "10001")
    private Long storeId;

    @Schema(description = "订单状态，不传查询全部；110待接单 120已接单 50配送中 60已完成 70已退款", example = "50")
    private Integer orderState;

    @Schema(description = "每页数量，默认 50，最大 100", example = "50")
    private Integer pageSize;

    @Schema(description = "游标分页位置，首次查询不传；下一页传上次返回的 searchAfter")
    private List<Object> searchAfter;
}

package com.htyoudao.youdao.module.analysis.dal.es;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "event_logs")
@Data
@Builder
public class BaseEvent {

    @Schema(description = "来源 1.微信小程序 2.支付宝")
    @Field(type = FieldType.Keyword)
    private Integer from;

    @Schema(description = "渠道")
    @Field(type = FieldType.Keyword)
    private String channel;

    @Schema(description = "事件类型")
    @Field(type = FieldType.Keyword)
    private String eventType;

    @Schema(description = "发生时间")
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Date timestamp;

    @Schema(description = "门店ID")
    @Field(type = FieldType.Keyword)
    private Long storeId;

    @Schema(description = "用户ID")
    @Field(type = FieldType.Keyword)
    private Long memberId;

    @Schema(description = "项目ID")
    @Field(type = FieldType.Keyword)
    private Long businessId;

    @Schema(description = "事件ID eg:优惠券ID,商品ID")
    @Field(type = FieldType.Keyword)
    private String eventId;

    @Schema(description = "新客:2 老客:3")
    @Field(type = FieldType.Keyword)
    private Integer crowdStatus;

}

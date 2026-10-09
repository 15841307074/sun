package com.htyoudao.youdao.module.analysis.dal.es;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "ad_event_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdEvent {

    @Schema(description = "来源 1.微信小程序 2.支付宝")
    @Field(type = FieldType.Keyword)
    private Integer from;

    @Schema(description = "广告事件类型 ad_exposure:曝光 ad_click:点击 ad_leave:离开")
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

    @Schema(description = "广告ID")
    @Field(type = FieldType.Keyword)
    private Long adId;

    @Schema(description = "广告名称")
    @Field(type = FieldType.Keyword)
    private String adName;

    @Schema(description = "广告位序号 1-14")
    @Field(type = FieldType.Integer)
    private Integer adInfoPosition;

    @Schema(description = "广告停留时长(毫秒)，仅广告离开事件携带")
    @Field(type = FieldType.Long)
    private Long stayMs;

    @Schema(description = "批量曝光列表，仅 ad_exposure 事件使用")
    private List<AdExposureItem> exposureList;

}

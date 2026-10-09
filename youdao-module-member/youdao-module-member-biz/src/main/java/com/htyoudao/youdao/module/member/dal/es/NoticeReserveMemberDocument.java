package com.htyoudao.youdao.module.member.dal.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

/**
 * @author dht
 * 同意接收消息的用户
 */
@Data
@Document(indexName = "wx_member_reserver")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoticeReserveMemberDocument {

    @Id
    private Long id;

    @Field(name = "memberId", type = FieldType.Long)
    private Long memberId;

    @Field(name = "memberId", type = FieldType.Keyword)
    private String reserverTemplateType;

    @Field(name = "memberId", type = FieldType.Keyword)
    private String openid;

    @Field(name = "memberId", type = FieldType.Keyword)
    private String projectOwnerShip;

    @Field(name = "memberId", type = FieldType.Keyword)
    private String businessId;
}

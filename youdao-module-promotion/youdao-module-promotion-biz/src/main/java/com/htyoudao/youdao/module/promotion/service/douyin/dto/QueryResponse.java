package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Schema(description = "验券准备响应")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -5851216386037790132L;

    @Schema(description = "可用券列表")
    private List<Certificate> certificates;

}
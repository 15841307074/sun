package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author dht
 */
@Builder
@Data
public class ShortUrlRequest {

    private String longUrl;

    private List<String> tags;

    private boolean forwardQuery;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime validSince;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime validUntil;
}
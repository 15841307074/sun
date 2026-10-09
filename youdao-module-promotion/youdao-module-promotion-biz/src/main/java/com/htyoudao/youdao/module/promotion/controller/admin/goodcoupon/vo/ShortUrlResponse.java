package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-15
 */
@Data
public class ShortUrlResponse {
    private String shortUrl;
    private String shortCode;
    private String longUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime dateCreated;

    private List<String> tags;
    private Meta meta;
    private String domain;
    private String title;
    private boolean crawlable;
    private boolean forwardQuery;
    private VisitsSummary visitsSummary;
    private boolean hasRedirectRules;

    @Data
    public static class Meta {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private ZonedDateTime validSince;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private ZonedDateTime validUntil;

        private Integer maxVisits;
    }

    @Data
    public static class VisitsSummary {
        private int total;
        private int nonBots;
        private int bots;
    }
}
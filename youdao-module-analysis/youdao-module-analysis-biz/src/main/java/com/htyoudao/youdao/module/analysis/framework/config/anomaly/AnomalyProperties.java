package com.htyoudao.youdao.module.analysis.framework.config.anomaly;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 经营异常提醒 Nacos 配置
 *
 * <p>对应 Nacos 中 analysis-server 配置的 {@code analysis.anomaly.*} 段。
 * 通过 {@link RefreshScope} 支持运行时刷新，Nacos 改了配置不用重启 Pod。
 *
 * <pre>{@code
 * analysis:
 *   anomaly:
 *     message:
 *       title: 经营异常提醒
 *       content-week: "({year}年第{week}周)您管辖的门店存在经营异常，"
 *       content-month: "({year}年{month}月)您管辖的门店存在经营异常，"
 *       action-text: "点击查看>"
 *       push-content: 您管辖的门店存在经营异常，点击查看>
 *     push:
 *       enabled: false
 *       role-id: 2038870253207044098
 *       url: https://fc-mp-...next.bspapp.com/push
 *       whitelist-enabled: false
 *       whitelist-ids: ""
 * }</pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "analysis.anomaly")
@RefreshScope
public class AnomalyProperties {

    /**
     * 消息文案
     */
    private Message message = new Message();

    /**
     * 推送配置
     */
    private Push push = new Push();

    @Data
    public static class Message {

        /**
         * 消息标题。表里不存，接口返回时给
         */
        private String title = "经营异常提醒";

        /**
         * 周度正文模板，存进 analysis_platform_message.content。
         * 只存正文，不含「点击查看>」（那部分单独走 actionText）。
         * 占位符：{year} 四位数年份、{week} 周数不补零（第38周）
         */
        private String contentWeek = "({year}年第{week}周)您管辖的门店存在经营异常，";

        /**
         * 月度正文模板，同上。
         * 占位符：{year} 四位数年份、{month} 月数补零两位（08月）
         */
        private String contentMonth = "({year}年{month}月)您管辖的门店存在经营异常，";

        /**
         * 操作文案，接口返回时单独给前端，前端渲染成可点击样式（如橙色）。
         * 不落库，不存进 content 字段
         */
        private String actionText = "点击查看>";

        /**
         * 锁屏文案，不带周期前缀，包含完整「点击查看>」。
         * 推送那刻现取，不落库
         */
        private String pushContent = "您管辖的门店存在经营异常，点击查看>";
    }

    @Data
    public static class Push {

        /**
         * 推送总开关。false 时消息照常落库但不调云函数
         */
        private Boolean enabled = false;

        /**
         * 推送接收角色ID。SELECT id FROM system_role WHERE code='anomaly_reminder_receiver'
         */
        private Long roleId;

        /**
         * 推送云函数地址
         */
        private String url;

        /**
         * 白名单开关。true = 只给 whitelistIds 里的人发；false = 角色下所有人都发
         */
        private Boolean whitelistEnabled = false;

        /**
         * 白名单用户ID，逗号分隔，如 "572934469837905931,892345394509987861"。
         * 用 system_users.id。whitelistEnabled=true 且为空时，一个人都不发（安全默认）
         */
        private String whitelistIds = "";
    }

    // ==================== 便捷方法 ====================

    /**
     * 解析白名单用户ID为 Set
     *
     * @return 白名单用户ID集合，未配置或为空时返回空 Set
     */
    public Set<Long> parseWhitelistIds() {
        if (!StringUtils.hasText(push.getWhitelistIds())) {
            return Collections.emptySet();
        }
        return Arrays.stream(push.getWhitelistIds().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}

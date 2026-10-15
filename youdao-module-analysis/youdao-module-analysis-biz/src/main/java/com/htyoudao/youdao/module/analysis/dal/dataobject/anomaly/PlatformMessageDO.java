package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.analysis.enums.MessageTypeEnum;
import com.htyoudao.youdao.module.analysis.enums.PushStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 平台消息（推送记录）DO
 *
 * <p>对应表 {@code analysis_platform_message}。
 *
 * <p>这张表记录「我给他推过哪些消息」，给 App 消息列表查。
 * 定时任务推送时会先往这张表写一条，再调推送云函数，最后回写推送状态。
 *
 * <p><b>重要：这是一张通用表</b>。经营异常提醒只是 message_type 的一种，
 * 将来加「售卖异常提醒」只需要往枚举里加一个值，不用改表结构。
 *
 * @see MessageTypeEnum
 * @see PushStatusEnum
 */
@TableName("analysis_platform_message")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlatformMessageDO extends BusinessBaseDO {

    /**
     * 主键，MyBatis-Plus ASSIGN_ID 雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收人用户ID，对应 system_users.id
     */
    private Long userId;

    /**
     * 消息类型
     *
     * @see MessageTypeEnum
     */
    private Integer messageType;

    /**
     * 业务周期开始日。三个用途：
     * ① 拼消息正文里的「第38周 / 2026年08月」
     * ② 点击消息跳转时定位到哪个周期
     * ③ 和 user_id/message_type 组成幂等键
     */
    private LocalDate periodStart;

    /**
     * 消息正文快照，例如「(2026年第38周)您管辖的门店存在经营异常，点击查看>」。
     * 这是<b>存下来的快照</b>，不是每次查询现拼的，保证以后改了文案模板，历史消息内容不变
     */
    private String content;

    /**
     * 是否已读：false=未读 true=已读。
     * 由 App 接口 PUT /analysis/app/platform-message/read 修改
     */
    private Boolean readStatus;

    /**
     * 已读时间。未读时为 null；重复标记已读时<b>保持首次已读时间不变</b>
     */
    private LocalDateTime readTime;

    /**
     * 推送状态
     *
     * @see PushStatusEnum
     */
    private Integer pushStatus;

    /**
     * 推送失败原因，取云函数返回的 errCode + errMsg，超过 512 字符截断。
     * 排查「用户说没收到推送」时先看这个字段
     */
    private String pushError;
}

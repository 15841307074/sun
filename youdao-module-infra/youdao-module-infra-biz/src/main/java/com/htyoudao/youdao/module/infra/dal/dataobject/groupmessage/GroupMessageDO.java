package com.htyoudao.youdao.module.infra.dal.dataobject.groupmessage;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 群消息 DO
 *
 * @author 超级管理员
 */
@TableName("infra_group_message")
@KeySequence("infra_group_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 临时id,由前端生成
     */
    private String tmpId;
    /**
     * 群id
     */
    private Long groupId;
    /**
     * 发送用户id
     */
    private Long sendId;
    /**
     * 发送用户昵称
     */
    private String sendNickName;
    /**
     * 发送内容
     */
    private String content;
    /**
     * 被@的用户id列表，逗号分隔
     */
    private String atUserIds;
    /**
     * 是否回执消息
     */
    private Boolean receipt;
    /**
     * 回执消息是否完成
     */
    private Boolean receiptOk;
    /**
     * 消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示
     */
    private Integer type;
    /**
     * 状态 0:未发出  2:撤回
     */
    private Integer status;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

}

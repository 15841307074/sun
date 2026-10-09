package com.htyoudao.youdao.module.infra.enums.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * details
 *
 * @author liuzhaowang
 */
@Getter
@AllArgsConstructor
public enum MessageCodeEnum {
    TEXT(0, "文字消息"),
    IMAGE(1, "图片消息"),
    FILE(2, "文件消息"),
    AUDIO(3, "语音消息"),
    VIDEO(4, "视频消息"),
    REVOKE(10, "撤回"),
    READ(11, "已读"),
    RECEIPT(12, "消息已读回执"),
    TIP_TIME(20,"时间提示"),
    TIP_TEXT(21,"文字提示"),
    LOADING(30,"加载中标记"),
    ACT_RT_VOICE(40,"语音通话"),
    ACT_RT_VIDEO(41,"视频通话"),
    USER_BANNED(50,"用户封禁"),
    GROUP_BANNED(51,"群聊封禁"),
    GROUP_UNBAN(52,"群聊解封"),
    FRIEND_NEW(80, "新增好友"),
    FRIEND_DEL(81, "删除好友"),
    FRIEND_DND(82, "好友免打扰"),
    GROUP_NEW(90, "新增群聊"),
    GROUP_DEL(91, "删除群聊"),
    GROUP_DND(92, "群聊免打扰"),
    RTC_CALL_VOICE(100, "语音呼叫"),
    RTC_CALL_VIDEO(101, "视频呼叫"),
    RTC_ACCEPT(102, "接受"),
    RTC_REJECT(103, "拒绝"),
    RTC_CANCEL(104, "取消呼叫"),
    RTC_FAILED(105, "呼叫失败"),
    RTC_HANDUP(106, "挂断"),
    RTC_CANDIDATE(107, "同步candidate"),
    RTC_GROUP_SETUP(200,"发起群视频通话"),
    RTC_GROUP_ACCEPT(201,"接受通话呼叫"),
    RTC_GROUP_REJECT(202,"拒绝通话呼叫"),
    RTC_GROUP_FAILED(203,"拒绝通话呼叫"),
    RTC_GROUP_CANCEL(204,"取消通话呼叫"),
    RTC_GROUP_QUIT(205,"退出通话"),
    RTC_GROUP_INVITE(206,"邀请进入通话"),
    RTC_GROUP_JOIN(207,"主动进入通话"),
    RTC_GROUP_OFFER(208,"推送offer信息"),
    RTC_GROUP_ANSWER(209,"推送answer信息"),
    RTC_GROUP_CANDIDATE(210,"同步candidate"),
    RTC_GROUP_DEVICE(211,"设备操作"),
    ;

    private final Integer code;

    private final String desc;

}

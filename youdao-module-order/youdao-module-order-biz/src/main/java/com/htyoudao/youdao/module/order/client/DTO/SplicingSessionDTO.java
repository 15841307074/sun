package com.htyoudao.youdao.module.order.client.DTO;

import lombok.Data;

@Data
public class SplicingSessionDTO {

    /**
     * channelId
     */
    private String channelId;
    /**
     * SessionId: mainId+":"+openId
     */
    private String sessionId;
    /**
     * 拼单id
     */
    private String mainId;
    /**
     * 用户id
     */
    private String openId;
    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 是否是队长 0:成员， 1：队长
     */
    private int isCaptain;
    /**
     * 进团时间戳
     */
    private String joinTimestamp;
    /**
     * 服务器节点id
     */
    private String serverNodeId;
    /**
     * 当前session创建时间戳
     */
    private String createTime;
    /**
     * 用户菜单
     */
    private String carList;



}

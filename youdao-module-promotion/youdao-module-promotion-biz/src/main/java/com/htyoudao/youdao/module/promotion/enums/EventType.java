package com.htyoudao.youdao.module.promotion.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    LUCKY_DRAW("抽奖参与","lucky_draw"),
    SHARE("抽奖分享","lucky_draw_share"),
    POINT_DRAW("集点参与","activity"),
    POINT_SHARE("集点分享","share"),
    DRAW_LOTS("抽签参与","draw_lots"),
    SIGN_DRAW("签到参与","sign"),
    SIGN_SHARE("签到分享","share"),
    ANSWER("有奖问答参与", "answer"),
    ANSWER_SHARE("有奖问答分享", "answer_share"),
    VOTE("投票参与", "vote"),
    VOTE_SHARE("投票分享", "vote_share"),
    ;
    private final String name;
    private final String code;
}

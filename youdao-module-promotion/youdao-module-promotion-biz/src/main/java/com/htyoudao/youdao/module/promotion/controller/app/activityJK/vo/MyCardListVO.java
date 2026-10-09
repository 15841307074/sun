package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 我的卡片列表响应VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "我的卡片列表响应")
public class   MyCardListVO {

    @Schema(description = "活动全部卡片列表")
    private List<UserCardVO> cardList;
}

package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "抽奖活动推广详情")
public class ActivityJkSpreadRespVO {

    @Schema(description = "活动 id")
    private Long id;



    /** 分享标题 */
    @Schema(name = "shareTitle", description = "分享标题")
    private String shareTitle;

    /** 分享内容 */
    @Schema(name = "shareNote", description = "分享内容")
    private String shareNote;

    /** 分享图片 */
    @Schema(name = "shareImgUrl", description = "分享图片")
    private String shareImgUrl;


    @Schema(description = "活动推广渠道链接 集合")
    private List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();
}

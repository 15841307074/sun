package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "抽奖活动推广页 新增/修改 ")
@Data
public class LotterySpreadSaveReqVO {

    @Schema(description = "活动 id")
    private Long id;

    /** 抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒 */
    @Schema(name = "lotteryType", description = "抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒")
    @NotNull(message = "抽奖类型 不能为空")
    private Integer lotteryType;


    /** 分享标题 */
    @Schema(name = "shareTitle", description = "分享标题")
    @NotNull(message = "分享标题 不能为空")
    private String shareTitle;

    /** 分享内容 */
    @Schema(name = "shareNote", description = "分享内容")
    @NotNull(message = "分享描述 不能为空")
    private String shareNote;

    /** 分享图片 */
    @Schema(name = "shareImgUrl", description = "分享图片")
    @NotNull(message = "分享图片 不能为空")
    private String shareImgUrl;


    @Schema(description = "活动推广相关链接")
    @NotNull(message = "活动推广相关链接 不能为空")
    @Valid
    private List<ActivityChannelSaveReqVO> activityChannelList =new ArrayList<>();
}

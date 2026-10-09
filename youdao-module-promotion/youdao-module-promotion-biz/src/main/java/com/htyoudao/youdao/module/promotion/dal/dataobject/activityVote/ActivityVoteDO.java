package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@TableName(value = "activity_vote", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 活动主表ID */
    private Long activityId;
    /** 活动门店范围 0指定门店 1全部门店 */
    private Integer activityStore;
    /** 活动封面图 */
    private String activityImgUrl;
    /** 活动背景图 */
    private String activityBackground;
    /** 投票背景图 */
    private String voteBackground;
    /** 排行榜背景图 */
    private String rankingBackground;
    /** 背景色 */
    private String backgroundColor;
    /** 投票次数限制 0不限制 1每天可投票 2最多可投票 */
    private Integer voteCountFlag;
    /** 投票次数 */
    private Integer voteCount;
    /** 投票按钮文案 */
    private String voteBtnTitle;
    /** 分享标题 */
    private String shareTitle;
    /** 分享内容 */
    private String shareNote;
    /** 分享图片 */
    private String shareImgUrl;
    /** 分享类型 1不允许转发 2允许转发好友 3允许复制链接 */
    private Integer shareType;
    /** 公共展示 0开启 1关闭 */
    private Integer publicButton;
    /** 社群专享 1不开启 2开启 */
    private Integer communityFlag;
    /** 引导图片 */
    private String guideImage;
    /** 创建人姓名 */
    private String createUserName;
    /** 修改人姓名 */
    private String updateUserName;
}

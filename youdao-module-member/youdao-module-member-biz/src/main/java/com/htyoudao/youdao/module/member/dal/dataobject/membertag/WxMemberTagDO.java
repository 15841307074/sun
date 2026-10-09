package com.htyoudao.youdao.module.member.dal.dataobject.membertag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 标签 DO
 *
 * @author 零零玖零
 */
@TableName("wx_member_tag")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMemberTagDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * memberId
     */
    private Long memberId;

    /**
     * 标签id
     */
    private Long tagId;
}
package com.htyoudao.youdao.module.member.dal.dataobject.tag;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 用户标签关系表 DO
 *
 */
@TableName(value = "wx_member_tag", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTagDO extends BusinessBaseDO {
    /**
     * 自增编号
     */
    @TableId
    private Long id;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 用户ID
     */
    private Long memberId;


}

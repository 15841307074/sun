package com.htyoudao.youdao.module.member.dal.dataobject.wecom;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业微信群成员关系DO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wecom_group_member")
public class WecomGroupMemberDO extends BusinessBaseDO {
    
    /**
     * 群ID
     */
    private String chatId;
    
    /**
     * 成员ID
     */
    private String externalUserId;


    private String unionId;
    
    /**
     * 成员类型：1-企业成员 2-外部联系人
     */
    private Integer memberType;
}

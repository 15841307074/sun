package com.htyoudao.youdao.module.member.dal.dataobject.tagvalue;

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
@TableName("wx_tag_value")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagValueDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 标签名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签组id
     */
    private Long tagGroupId;

}
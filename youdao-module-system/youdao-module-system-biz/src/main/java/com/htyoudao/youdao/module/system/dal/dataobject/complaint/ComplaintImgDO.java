package com.htyoudao.youdao.module.system.dal.dataobject.complaint;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
/**
 *  投诉图片关联 DO
 *
 * @author ssz
 */
@TableName(value = "system_complaint_img", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintImgDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 投诉id
     */
    private Long complaintId;
    /**
     * 图片路径
     */
    private String imgUrl;
}

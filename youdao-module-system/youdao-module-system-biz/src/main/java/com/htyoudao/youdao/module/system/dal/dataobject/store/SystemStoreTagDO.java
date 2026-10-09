package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 门店标签关系表 DO
 *
 * @author ssz
 */
@TableName(value = "system_store_tag", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreTagDO extends BusinessBaseDO {
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
     * 门店ID
     */
    private Long storeId;


}

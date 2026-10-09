package com.htyoudao.youdao.module.system.dal.dataobject.wxstore;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@TableName(value = "system_wecom_img", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreWecomImgDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId
    private Long id;
    /** 店长社群背景图 */
    private String communityManagerImg;
    /** 门店社群背景图 */
    private String communityStoreImg;
}

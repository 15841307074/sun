package com.htyoudao.youdao.module.system.dal.dataobject.applet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * @author villky
 * 营销活动关联门店表
 */
@TableName("applet_page_management_store")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppletPageManagementStoreDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -2331747576330603909L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long appletPageManagementStoreId;

    /**
     * 页面装修模板ID
     */
    private Long appletPageId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店ID
     */
    private String storeName;

    private Integer appletPageLocation;

}

package com.htyoudao.youdao.module.bpm.dal.dataobject.business;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("system_store_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmAllStoreInfoDO extends BusinessBaseDO {

    @TableId
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店负责人 id
     */
    private Long userId;

    /**
     * 门店负责人姓名
     */
    private String storeLeader;

    /**
     * 门店负责人电话
     */
    private String storeLeaderPhone;

    private Integer storeStatus;
}

package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 月度门店组织快照。 */
@TableName("analysis_store_org_snapshot")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreOrgSnapshotDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 月份首日，例如 2026-08-01。 */
    private LocalDate snapshotMonth;

    private Long storeId;
    private String storeName;
    private String storeProvince;
    private Long orgId;
    private String orgName;
    private Long parentOrgId;
    private String parentOrgName;

    /** 从根组织到门店直属组织的完整路径 JSON。 */
    private String orgPathJson;
}

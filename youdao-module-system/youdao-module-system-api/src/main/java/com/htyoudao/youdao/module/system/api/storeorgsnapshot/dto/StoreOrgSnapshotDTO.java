package com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 正常营业门店及其组织链数据。 */
@Data
public class StoreOrgSnapshotDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long storeId;
    private String storeName;
    private String storeProvince;
    private Long orgId;
    private String orgName;
    private Long parentOrgId;
    private String parentOrgName;
    private List<StoreOrgPathNodeDTO> orgPath;
}

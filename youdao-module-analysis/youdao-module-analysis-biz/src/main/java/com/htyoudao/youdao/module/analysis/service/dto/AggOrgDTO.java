package com.htyoudao.youdao.module.analysis.service.dto;

import java.util.List;
import lombok.Data;

@Data
public class AggOrgDTO {
    private Long orgId;
    private String orgName;
    private List<Long> storeIds;
}

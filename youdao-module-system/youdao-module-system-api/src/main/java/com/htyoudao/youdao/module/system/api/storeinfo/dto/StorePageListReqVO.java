package com.htyoudao.youdao.module.system.api.storeinfo.dto;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorePageListReqVO extends PageParam {
    @Schema(description = "门店名称")
    private String storeName;
    @Schema(description = "经营状态，参见 CommonStatusEnum 枚举类")
    private Integer openStatus;
    @Schema(description = "组织ID")
    private Long orgId;
    @Schema(description = "标签id")
    private List<Long> tagId;

    @Schema(description = "类型 1 满足其中一个   2 都满足")
    private Integer type;


    @Schema(description = "用户id")
    private Long userId;

    private Set<Long> orgIds;

    private List<Long> storeIds;

    @Schema(description = "用户id")
    private Long userIds;

    @Schema(description = "经营状态 0经营")
    private Integer storeStatus;
}

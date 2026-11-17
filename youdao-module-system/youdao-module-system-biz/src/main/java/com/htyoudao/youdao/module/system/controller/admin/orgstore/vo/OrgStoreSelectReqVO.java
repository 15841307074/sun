package com.htyoudao.youdao.module.system.controller.admin.orgstore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 组织门店选择条件")
public class OrgStoreSelectReqVO extends PageParam {

    public enum Scope {
        VISIBLE, PROJECT
    }

    public enum Channel {
        STANDARD, DOUYIN, SUPPLY_CHAIN
    }

    @Schema(description = "VISIBLE 当前登录人可见，PROJECT 需具备全量数据权限。均限定本人组织及下级、本人担任店长的门店")
    private Scope scope = Scope.VISIBLE;

    @Schema(description = "项目编号，传入时须与当前项目一致")
    @Positive
    private Long businessId;

    @Schema(description = "STANDARD 常规门店，DOUYIN 已绑定抖音的门店，SUPPLY_CHAIN 支持供应链来源")
    private Channel channel = Channel.STANDARD;

    @Schema(description = "搜索关键词。树支持组织名和门店名，列表按门店名搜索")
    @Size(max = 100)
    private String keyword;

    @Schema(description = "门店名称。嵌套树仅匹配门店名称，省略或传空白时不按门店名称筛选")
    @Size(max = 100)
    private String storeName;

    @Schema(description = "组织编号，可与 orgIds 一起传")
    @Positive
    private Long orgId;

    @Schema(description = "组织编号列表")
    @Size(max = 500)
    private Set<@NotNull @Positive Long> orgIds;

    @Schema(description = "是否包含所选组织的下级，默认 true")
    private Boolean includeChildren = true;

    @Schema(description = "树中是否包含门店，默认 true")
    private Boolean withStore = true;

    @Schema(description = "仅包含这些门店。传空数组时返回空集合")
    @Size(max = 10000)
    private Set<@NotNull @Positive Long> includeStoreIds;

    @Schema(description = "排除已选门店，兼容旧选店分页的 storeIds 排除语义")
    @Size(max = 10000)
    private Set<@NotNull @Positive Long> excludeStoreIds;

    @Schema(description = "是否过滤已闭店门店。未选经营状态时默认排除已闭店，传 false 可查全部")
    private Boolean filterClosedStore;

    @Schema(description = "经营状态多选，0经营中、1闭店、2暂时闭店。省略、传 null 或空数组时默认过滤已闭店",
            example = "[0,2]")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<@NotNull @Min(0) @Max(2) Integer> storeStatus;

    @Schema(description = "供应链门店使用状态")
    private Integer useStatus;

    @Schema(description = "仓库编号")
    @Positive
    private Long warehouseId;

    @Schema(description = "店长编号，仅作查询筛选")
    @Positive
    private Long userId;

    @Schema(description = "标签值编号列表，兼容 tagId。也可传标签组编号")
    @Size(max = 100)
    private List<@NotNull @Positive Long> tagIds;

    @Schema(description = "标签匹配，1 满足全部、2 满足任意、3 无标签。省略时按满足全部处理")
    @Min(1)
    @Max(3)
    private Integer type;

    @Schema(description = "旧选店请求兼容字段，仅接收。经营状态筛选使用 storeStatus")
    private Integer status;

    public void setTagId(List<Long> tagId) {
        if (tagIds == null) {
            tagIds = tagId;
        }
    }
}

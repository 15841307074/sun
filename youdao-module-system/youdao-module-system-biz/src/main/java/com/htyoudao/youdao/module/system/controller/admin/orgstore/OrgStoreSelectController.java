package com.htyoudao.youdao.module.system.controller.admin.orgstore;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreTreeRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreListRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreOptionRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreSelectReqVO;
import com.htyoudao.youdao.module.system.service.org.OrgStoreSelectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 组织门店选择")
@RestController
@RequestMapping("/system/org-store")
@RequiredArgsConstructor
public class OrgStoreSelectController {

    private final OrgStoreSelectService selectService;

    @PostMapping("/tree")
    @Operation(summary = "查询嵌套组织门店树")
    public CommonResult<List<OrgTreeNode>> tree(@Valid @RequestBody OrgStoreSelectReqVO request) {
        return success(selectService.tree(request));
    }

    @PostMapping("/tree/flat")
    @Operation(summary = "查询平铺组织门店节点")
    public CommonResult<List<OrgStoreTreeRespVO>> flatTree(@Valid @RequestBody OrgStoreSelectReqVO request) {
        return success(selectService.flatTree(request));
    }

    @PostMapping("/stores/page")
    @Operation(summary = "分页查询门店选择项")
    public CommonResult<PageResult<OrgStoreOptionRespVO>> storePage(@Valid @RequestBody OrgStoreSelectReqVO request) {
        return success(selectService.storePage(request));
    }

    @PostMapping("/stores/list")
    @Operation(summary = "查询全部匹配的门店选择项")
    public CommonResult<List<OrgStoreListRespVO>> storeList(@Valid @RequestBody OrgStoreSelectReqVO request) {
        return success(selectService.storeList(request));
    }

    @PostMapping("/stores/ids")
    @Operation(summary = "查询全部匹配的门店编号")
    public CommonResult<List<Long>> storeIds(@Valid @RequestBody OrgStoreSelectReqVO request) {
        return success(selectService.storeIds(request));
    }
}

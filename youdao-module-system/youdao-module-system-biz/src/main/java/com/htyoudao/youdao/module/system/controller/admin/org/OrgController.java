package com.htyoudao.youdao.module.system.controller.admin.org;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.OrgUserSaveReqVO;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.htyoudao.youdao.module.system.service.org.OrgTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 组织")
@RestController
@RequestMapping("/system/org")
@Validated
public class OrgController {

    @Resource
    private OrgService orgService;

    @Resource
    private OrgTreeService orgTreeService;


    @GetMapping("getOrgListByUser")
    @Operation(summary = "根据当前登录人获取组织树")
//    @PreAuthorize("@ss.hasPermission('system:org:getOrgListByUser')")
    public CommonResult<List<OrgTreeRespVO>> getOrgListByUser() {
        return success(orgService.getOrgListByUserV2());
    }

    @GetMapping("getOrgAndStoreByLoginUser")
    @Operation(summary = "根据当前登录人获取组织树+门店 -- 闲着没事写一版本")
//    @PreAuthorize("@ss.hasPermission('system:org:getOrgListByUser')")getOrganizationsByLoginUser
    public CommonResult<List<OrgTreeRespVO>> getOrgAndStoreByLoginUser() {
        return success(orgService.getOrgAndStoreByLoginUser());
    }

    @PostMapping("create")
    @Operation(summary = "创建组织")
    @PreAuthorize("@ss.hasPermission('system:org:createOrg')")
    public CommonResult<Long> createOrg(@Valid @RequestBody OrgSaveReqVO orgSaveReqVO) {
        return success(orgService.createOrg(orgSaveReqVO));
    }


    @PostMapping("moveOrg")
    @Operation(summary = "移动组织")
    @PreAuthorize("@ss.hasPermission('system:org:moveOrg')")
    public CommonResult<Integer> moveOrg(@Valid @RequestBody OrgMoveReqVO orgMoveReqVO) {
        return success(orgService.moveOrg(orgMoveReqVO));
    }

    @PutMapping("update")
    @Operation(summary = "更新组织")
    @PreAuthorize("@ss.hasPermission('system:org:updateOrg')")
    public CommonResult<Boolean> updateOrg(@Valid @RequestBody OrgSaveReqVO orgSaveReqVO) {
        orgService.updateOrg(orgSaveReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除组织")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:org:deleteOrg')")
    public CommonResult<Boolean> deleteOrg(@RequestParam("id") Long id) {
        orgService.deleteOrg(id);
        return success(true);
    }

    @GetMapping("getStoreListWithOrg")
    @Operation(summary = "组织添加门店的窗口")
    @PreAuthorize("@ss.hasPermission('system:org:getStoreListWithOrg')")
    public CommonResult<PageResult<OrgStorePageRespVO>> getStoreListWithOrg(OrgStorePageReqVO pageReqVO) {
        return success(orgService.getStoreListWithOrg(pageReqVO));
    }

    @GetMapping("getStoreListByOrgId")
    @Operation(summary = "通过组织查询门店")
    @PreAuthorize("@ss.hasPermission('system:org:getStoreListByOrgId')")
    public CommonResult<List<OrgStoreRespVO>> getStoreListByOrgId(@RequestParam(value = "orgId") Long orgId,
                                                                  @RequestParam(value = "storeName",required = false) String storeName) {
        return success(orgService.getStoreListByOrgId(orgId,storeName,null));
    }

    @GetMapping("getOpenStoreListByOrgId")
    @Operation(summary = "隐藏停用门店")
    @PreAuthorize("@ss.hasPermission('system:org:getOpenStoreListByOrgId')")
    public CommonResult<List<OrgStoreRespVO>> getOpenStoreListByOrgId(@RequestParam(value = "orgId") Long orgId,
                                                                  @RequestParam(value = "storeName",required = false) String storeName) {
        return success(orgService.getOpenStoreListByOrgId(orgId,storeName));
    }

    @PostMapping("addStoreForOrg")
    @Operation(summary = "组织机构添加门店")
    @PreAuthorize("@ss.hasPermission('system:org:addStoreForOrg')")
    public CommonResult<Integer> addStoreForOrg(@RequestBody @Valid OrgStoreSaveReqVO orgStoreSaveReqVO) {
        return success(orgService.addStoreForOrg(orgStoreSaveReqVO));
    }

    @PutMapping("moveStore")
    @Operation(summary = "移动门店")
    @PreAuthorize("@ss.hasPermission('system:org:moveStore')")
    public CommonResult<Integer> moveStore(@Valid @RequestBody OrgStoreMoveReqVO orgStoreMoveReqVO) {
        return success(orgService.moveStore(orgStoreMoveReqVO));
    }

    @DeleteMapping("removeStore")
    @Operation(summary = "移除门店")
    @PreAuthorize("@ss.hasPermission('system:org:removeStore')")
    public CommonResult<Integer> removeStore(@Valid @RequestBody OrgStoreMoveReqVO orgStoreMoveReqVO) {
        return success(orgService.removeStore(orgStoreMoveReqVO));
    }

    @PostMapping("addUserForStore")
    @Operation(summary = "门店添加人员")
    @PreAuthorize("@ss.hasPermission('system:org:addStoreForOrg')")
    public CommonResult<Boolean> addUserForStore(@Valid @RequestBody StoreUserSaveBatchReqVO orgStoreSaveReqVO) {
        return success(orgService.addUserForStore(orgStoreSaveReqVO));
    }

    @PutMapping("setStoreManager")
    @Operation(summary = "设置为店长")
    @PreAuthorize("@ss.hasPermission('system:org:setStoreManager')")
    public CommonResult<Integer> setStoreManager(@Valid @RequestBody StoreManagerReqVO storeManagerReqVO) {
        return success(orgService.setStoreManager(storeManagerReqVO));
    }

    @PutMapping("unStoreManager")
    @Operation(summary = "撤销店长")
    @PreAuthorize("@ss.hasPermission('system:org:unStoreManager')")
    public CommonResult<Integer> unStoreManager(@Valid @RequestBody StoreManagerReqVO storeManagerReqVO) {
        return success(orgService.unStoreManager(storeManagerReqVO));
    }

    @PutMapping("storeSetUserIsVisible")
    @Operation(summary = "门店设置用户可见")
    @PreAuthorize("@ss.hasPermission('system:org:storeSetUserIsVisible')")
    public CommonResult<Integer> storeSetUserIsVisible(@Valid @RequestBody StoreManagerReqVO storeManagerReqVO) {
        return success(orgService.storeSetUserIsVisible(storeManagerReqVO));
    }

    @PutMapping("storeSetUserUnVisible")
    @Operation(summary = "门店设置用户不可见")
    @PreAuthorize("@ss.hasPermission('system:org:storeSetUserUnVisible')")
    public CommonResult<Integer> storeSetUserUnVisible(@Valid @RequestBody StoreManagerReqVO storeManagerReqVO) {
        return success(orgService.storeSetUserUnVisible(storeManagerReqVO));
    }

    @GetMapping("getUserListWithOrg")
    @Operation(summary = "添加下级组织时候选择负责人的窗口/当前组织添加人员窗口")
    @PreAuthorize("@ss.hasPermission('system:org:getUserListWithOrg')")
    public CommonResult<PageResult<OrgUserPageRespVO>> getUserListWithOrg(OrgUserPageReqVO pageReqVO) {
        return success(orgService.getUserListWithOrg(pageReqVO));
    }

    @GetMapping("getUserListWithStore")
    @Operation(summary = "门店添加人员时候的查询窗口")
    @PreAuthorize("@ss.hasPermission('system:org:getUserListWithStore')")
    public CommonResult<PageResult<OrgUserPageRespVO>> getUserListWithStore(OrgUserPageReqVO pageReqVO) {
        return success(orgService.getUserListWithStore(pageReqVO));
    }

    @GetMapping("getUserListByStoreId")
    @Operation(summary = "通过门店查询人员")
    @PreAuthorize("@ss.hasPermission('system:org:getUserListByStoreId')")
    public CommonResult<PageResult<OrgUserPageRespVO>> getUserListByStoreId(StoreUserPageReqVO pageReqVO) {
        return success(orgService.getUserListByStoreId(pageReqVO));
    }

    @GetMapping("getUserListByOrgId")
    @Operation(summary = "通过组织查询当前节点人员")
    @PreAuthorize("@ss.hasPermission('system:org:getUserListByOrgId')")
    public CommonResult<PageResult<OrgUserPageRespVO>> getUserListByOrgId(OrgUserPageReqVO pageReqVO) {
        return success(orgService.getUserListByOrgId(pageReqVO));
    }

    @GetMapping("getUserListByUpdate")
    @Operation(summary = "更新时通过组织查询当前节点人员")
    @PreAuthorize("@ss.hasPermission('system:org:getUserListByUpdate')")
    public CommonResult<List<OrgUserRespVO>> getUserListByUpdate(@RequestParam(value = "orgId") Long orgId) {
        return success(orgService.getUserListByUpdate(orgId));
    }

    @PostMapping("addUserForOrg")
    @Operation(summary = "组织添加人员")
    @PreAuthorize("@ss.hasPermission('system:org:addUserForOrg')")
    public CommonResult<Boolean> addUserForOrg(@RequestBody OrgUserSaveReqVO orgUserSaveReqVO) {
        return success(orgService.addUserForOrg(orgUserSaveReqVO));
    }

    @PutMapping("moveOrgUser")
    @Operation(summary = "组织移动人员")
    @PreAuthorize("@ss.hasPermission('system:org:moveOrgUser')")
    public CommonResult<Boolean> moveOrgUser(@Valid @RequestBody OrgUserMoveReqVO orgMoveReqVO) {
        return success(orgService.moveOrgUser(orgMoveReqVO));
    }

    @DeleteMapping("removeOrgUser")
    @Operation(summary = "组织移除人员")
    @PreAuthorize("@ss.hasPermission('system:org:removeOrgUser')")
    public CommonResult<Integer> removeOrgUser(@Valid @RequestBody OrgUserMoveReqVO orgMoveReqVO) {
        return success(orgService.removeOrgUser(orgMoveReqVO));
    }

    @DeleteMapping("removeStoreUser")
    @Operation(summary = "门店移除人员")
    @PreAuthorize("@ss.hasPermission('system:org:removeStoreUser')")
    public CommonResult<Integer> removeStoreUser(@Valid @RequestBody StoreUserRemoveReqVO storeUserRemoveReqVO) {
        return success(orgService.removeStoreUser(storeUserRemoveReqVO));
    }

    @PutMapping("setCharged")
    @Operation(summary = "设置为组织负责人")
    @PreAuthorize("@ss.hasPermission('system:org:setCharged')")
    public CommonResult<Integer> setCharged(@Valid @RequestBody OrgUserChargedReqVO orgUserChargedReqVO) {
        return success(orgService.setCharged(orgUserChargedReqVO));
    }

    @PutMapping("unCharged")
    @Operation(summary = "撤销组织负责人")
    @PreAuthorize("@ss.hasPermission('system:org:unCharged')")
    public CommonResult<Integer> unCharged(@Valid @RequestBody OrgUserChargedReqVO orgUserChargedReqVO) {
        return success(orgService.unCharged(orgUserChargedReqVO));
    }

    @PutMapping("orgSetUserIsVisible")
    @Operation(summary = "组织设置用户可见")
    @PreAuthorize("@ss.hasPermission('system:org:orgSetUserIsVisible')")
    public CommonResult<Integer> orgSetUserIsVisible(@Valid @RequestBody OrgUserChargedReqVO orgUserChargedReqVO) {
        return success(orgService.orgSetUserIsVisible(orgUserChargedReqVO));
    }

    @PutMapping("orgSetUserUnVisible")
    @Operation(summary = "组织设置用户不可见")
    @PreAuthorize("@ss.hasPermission('system:org:orgSetUserUnVisible')")
    public CommonResult<Integer> orgSetUserUnVisible(@Valid @RequestBody OrgUserChargedReqVO orgUserChargedReqVO) {
        return success(orgService.orgSetUserUnVisible(orgUserChargedReqVO));
    }

    @PutMapping("sortOrg")
    @Operation(summary = "组织排序")
    @PreAuthorize("@ss.hasPermission('system:org:sortOrg')")
    public CommonResult<Boolean> sortOrg(@Valid @RequestBody List<SortOrgReqVO> sortOrgList) {
        return success(orgService.sortOrg(sortOrgList));
    }



    @GetMapping("getOrgListByUserAndBusID")
    @Operation(summary = "根据当前登录人/所选项目 获取组织树")
    @PreAuthorize("@ss.hasPermission('system:org:getOrgListByUserAndBusID')")
    public CommonResult<List<OrgTreeRespVO>> getOrgListByUserAndBusID(@RequestParam(value = "bussinessId",required = false) Long bussinessId) {
        return success(orgService.getOrgListByUserAndBusID(bussinessId));
    }
    @GetMapping("getOrgStoreListByUserAndBusID")
    @Operation(summary = "根据当前登录人/所选项目 获取组织门店树")
    public CommonResult<List<OrgStoreTreeRespVO>> getOrgStoreListByUserAndBusID() {
        return success(orgService.getOrgStoreListByUserAndBusID());
    }
    @GetMapping("getStoreIdsByUser")
    @Operation(summary = "根据当前登入人以及项目 获取本级组织以及下级组织id")
    public CommonResult<Set<Long>> getStoreIdsByUser() {
        return success(orgService.getStoreIdsByUser());
    }
    @GetMapping("getStoreIdListByOrgID")
    @Operation(summary = "根据项目id 获取下级门店id")
    public CommonResult<Set<Long>> getStoreIdListByOrgID(@RequestParam(value = "orgId") Long orgId) {
        return success(orgService.getStoreIdListByOrgID(orgId));
    }
    @GetMapping("getOrgListByLoginUser")
    @Operation(summary = "根据当前登录人获取组织树--门店")
//    @PreAuthorize("@ss.hasPermission('system:org:getOrgListByUser')")
    public CommonResult<List<OrgTreeRespVO>> getOrgListByLoginUser() {
        return success(orgService.getOrgListByUser());
    }

    @GetMapping("getOrgListByUsers")
    @Operation(summary = "根据当前登录人/所选项目 获取组织树")
//    @PreAuthorize("@ss.hasPermission('system:org:getOrgListByUserAndBusID')")
    public CommonResult<List<OrgTreeRespVO>> getOrgListByUsers() {
        return success(orgService.getOrgListByUsers());
    }

    @GetMapping("getOrgStoreListByUserData")
    @Operation(summary = "获取组织及组织下级的门店列表")
    public CommonResult<List<OrgStoreTreeRespVO>> getOrgStoreListByUserData() {
        return success(orgService.getOrgStoreListByUserData());
    }

    @GetMapping("getOrgStoreByCity")
    @Operation(summary = "获取城市信息及对应门店列表")
    public CommonResult<List<OrgStoreCityTreeRespVO>> getOrgStoreByCity() {
        return success(orgService.getOrgStoreByCity());
    }


    @GetMapping("getAllOrgStoreList")
    @Operation(summary = "获取所有组织及门店列表")
    public CommonResult<List<OrgStoreTreeRespVO>> getAllOrgStoreList(@RequestParam(required = false) Boolean filterClosedStore) {
        return success(orgService.getAllOrgStoreList(filterClosedStore));
    }

    @PermitAll
    @GetMapping("gyl/getAllOrgStoreList")
    @Operation(summary = "获取所有组织及门店列表")
    public CommonResult<List<OrgStoreTreeRespVO>> getAllOrgStoreListForGyl() {
        return success(orgService.getAllOrgStoreList(null));
    }

    @GetMapping("getAllOrgList")
    @Operation(summary = "获取所有组织列表")
    public CommonResult<List<OrgTreeRespVO>> getAllOrgList() {
        return success(orgService.getAllOrgList());
    }

    @GetMapping("getOperationStoreList")
    @Operation(summary = "获取经营中的组织门店列表")
    public CommonResult<List<OrgStoreTreeRespVO>> getOperationStoreList() {
        return success(orgService.getOperationStoreList());
    }

    @GetMapping("getOperationStoreListByUserData")
    @Operation(summary = "获取组织及组织下级经营中的门店列表")
    public CommonResult<List<OrgStoreTreeRespVO>> getOperationStoreListByUserData() {
        return success(orgService.getOperationStoreListByUserData());
    }

    @GetMapping("getUserVisibleOrgTree")
    @Operation(summary = "精简出用户能看到的组织树")
    public CommonResult<List<OrgTreeNode>> getUserVisibleOrgTree(@RequestParam(required = false) Boolean withStore) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        if (withStore == null){
            withStore = true;
        }
        return success(orgTreeService.getUserShowOrgTree(userId, withStore));
    }




    @GetMapping("getByLetterOrgTree")
    @Operation(summary = "按字母顺序查询的组织树加门店")
    public CommonResult<List<OrgTreeNode>> getByLetterOrgTree(
            @Parameter(description = "搜索关键词，支持组织名称和门店名称的模糊匹配，不传则返回完整树")
            @RequestParam(required = false) String keyword
    ) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        return success(orgTreeService.getShowOrgTree(userId,keyword));
    }


}

package com.htyoudao.youdao.module.system.controller.admin.applet;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementFullStoreRespVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementReqVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementDO;
import com.htyoudao.youdao.module.system.enums.AppletPageLocationEnum;
import com.htyoudao.youdao.module.system.service.applet.AppletPageManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 小程序配置页面
 */
@Tag(name = "管理后台 - 小程序管理")
@RestController
@RequestMapping("/system/appletPageManagement")
public class AppletPageManagementController {
    @Autowired
    private AppletPageManagementService appletPageManagementService;

    /**
     * pc端获取
     * @param  appletPageManagementReqVO AppletPageManagementReqVO
     * @return appletPageManagementPage
     */
    @PostMapping("/list")
    public CommonResult<PageResult<AppletPageManagementFullStoreRespVO>> list(@RequestBody AppletPageManagementReqVO appletPageManagementReqVO) {
        PageResult<AppletPageManagementFullStoreRespVO> appletPageManagementPage  = appletPageManagementService.getPage(appletPageManagementReqVO);

        return success(BeanUtils.toBean(appletPageManagementPage, AppletPageManagementFullStoreRespVO.class));
    }

    @Operation(summary = "pc端获取页面位置下拉")
    @PostMapping("/getAppletPageLocationEnum")
    public CommonResult<String> getAppletPageLocationEnum(){
        return success(AppletPageLocationEnum.getEle());
    }

    /**
     * 获取单条
     * @param appletPageId long
     * @return appletPageManagement
     */
    @GetMapping("/getInfo")
    public CommonResult<AppletPageManagementRespVO> getInfo(@RequestParam Long appletPageId) {
        AppletPageManagementRespVO appletPageManagement =  appletPageManagementService.getInfo(appletPageId);
        return success(appletPageManagement);
    }

    /**
     * 创建页面
     * @param appletPageManagement AppletPageManagementReqVO
     * @return CommonResult
     */
//    @Log(title = "创建小程序页面", businessType = BusinessType.INSERT, systemType = SystemType.HBGC)
    @PostMapping("/createAppletPage")
    @PreAuthorize("@ss.hasPermission('system:appletPageManagement:add')")
    public CommonResult<Boolean> createAppletPage(@RequestBody AppletPageManagementReqVO appletPageManagement) {
        appletPageManagementService.createAppletPage(appletPageManagement);
        return success(true);
    }
//    @Log(title = "修改小程序页面", businessType = BusinessType. UPDATE, systemType = SystemType.HBGC)
    @PostMapping("/updateAppletPage")
    @PreAuthorize("@ss.hasPermission('system:appletPageManagement:update')")
    public CommonResult<Boolean> updateAppletPage(@RequestBody AppletPageManagementReqVO appletPageManagement) {
        appletPageManagementService.updateAppletPage(appletPageManagement);
        return success(true);
    }

//    @Log(title = "删除小程序页面", businessType = BusinessType.DELETE, systemType = SystemType.HBGC)
    @GetMapping("/deleteAppletPage")
    @PreAuthorize("@ss.hasPermission('system:appletPageManagement:delete')")
    public CommonResult<Boolean> deleteAppletPage(@RequestParam Long appletPageId) {

        appletPageManagementService.deleteAppletPage(appletPageId);
        return success(true);
    }

//    @Log(title = "修改小程序页面状态", businessType = BusinessType. UPDATE, systemType = SystemType.HBGC)
    @PostMapping("/updateAppletPageStatus")
    @PreAuthorize("@ss.hasPermission('system:appletPageManagementStatus:update')")
    public CommonResult<Boolean> updateAppletPageStatus(@RequestBody AppletPageManagementReqVO appletPageManagement) {
        appletPageManagementService.updateAppletPageStatus(appletPageManagement);
        return success(true);
    }

    /**
     * 小程序获取页面
     * @param appletPageManagement AppletPageManagementReqVO
     * @return CommonResult
     */
    @PostMapping("/getAppletPageManagementForApplet")
    public CommonResult<Object> getAppletPageManagementForApplet(@RequestBody AppletPageManagementReqVO appletPageManagement) {
        Object appletPageInfo = appletPageManagementService.getAppletPageManagementForApplet(appletPageManagement);

        return success(appletPageInfo);
    }

    /*@PostMapping("/flushAppletPage")
    public CommonResult<Boolean> flushAppletPage() {
        appletPageManagementService.flushAppletPage();
        return success(true);
    }*/

/*    @PermitAll
    @SentinelResource(value = "getAppletPageManagementForAppletNew")
    @PostMapping("/getAppletPageManagementForAppletNew")
    @Operation(summary = "获取页面装修信息xin")
    public CommonResult<Object> getAppletPageManagementForAppletNew(@RequestBody AppAppletPageManagementReqVO appletPageManagement) throws InterruptedException {
        Object appletPageInfo = appletPageManagementService.getAppletPageManagementForAppletNew(appletPageManagement);
        return success(appletPageInfo);
    }*/


/*    @PermitAll
    @SentinelResource(value = "addPageTag")
    @PostMapping("/addPageTag")
    @Operation(summary = "获取页面装修信息xin")
    public CommonResult<Object> addPageTag() throws InterruptedException {
        appletPageManagementService.addRedisByStoreTag(2018507596281237505L,1955506587305324546L, 10L);
        return success(null);
    }*/

/*    @PermitAll
    @SentinelResource(value = "delPageTag")
    @PostMapping("/delPageTag")
    @Operation(summary = "获取页面装修信息xin")
    public CommonResult<Object> delPageTag() throws InterruptedException {
        List<Long> list = new ArrayList<>();
        list.add(2043920085277757442L);
        appletPageManagementService.delRedisByStoreTags(2041384122160783361L,list, 10L);
        return success(null);
    }*/

/*    @PermitAll
    @SentinelResource(value = "delPageTagGroup")
    @PostMapping("/delPageTagGroup")
    @Operation(summary = "获取页面装修信息xin")
    public CommonResult<Object> delPageTagGroup() throws InterruptedException {
        appletPageManagementService.delRedisByStoreTagGroup(1955506587305324546L, 10L);
        return success(null);
    }*/

}

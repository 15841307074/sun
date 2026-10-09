package com.htyoudao.youdao.module.infra.controller.admin.groupmessage;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessagePageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessageRespVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessageSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmessage.GroupMessageDO;
import com.htyoudao.youdao.module.infra.service.groupmessage.GroupMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 群消息")
@RestController
@RequestMapping("/infra/group-message")
@Validated
public class GroupMessageController {

    @Resource
    private GroupMessageService groupMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送群消息")
    public CommonResult<Long> sendGroupMessage(@Valid @RequestBody GroupMessageSaveReqVO sendReqVO) {
        return success(groupMessageService.sendGroupMessage(sendReqVO));
    }

    @DeleteMapping("/revoke")
    @Operation(summary = "撤回群消息")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> revokeGroupMessage(@NotNull @RequestParam("id") Long id) {
        groupMessageService.revokeGroupMessage(id);
        return success(true);
    }

    @PutMapping("/read")
    @Operation(summary = "消息已读")
    public CommonResult<Boolean> updateGroupMessage(@RequestParam Long groupId) {
        groupMessageService.readGroupMessage(groupId);
        return success(true);
    }

    @GetMapping("/load-offline-message")
    @Operation(summary = "拉取离线消息")
    public CommonResult<List<GroupMessageRespVO>> loadOfflineMessage(@RequestParam Long minId) {
        return CommonResult.success(groupMessageService.loadOfflineMessage(minId));
    }

    @GetMapping("/get")
    @Operation(summary = "获得群消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<GroupMessageRespVO> getGroupMessage(@RequestParam("id") Long id) {
        GroupMessageDO groupMessage = groupMessageService.getGroupMessage(id);
        return success(BeanUtils.toBean(groupMessage, GroupMessageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得群消息分页")
    public CommonResult<PageResult<GroupMessageRespVO>> getGroupMessagePage(@Valid GroupMessagePageReqVO pageReqVO) {
        PageResult<GroupMessageDO> pageResult = groupMessageService.getGroupMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, GroupMessageRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出群消息 Excel")
    @PreAuthorize("@ss.hasPermission('infra:group-message:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupMessageExcel(@Valid GroupMessagePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GroupMessageDO> list = groupMessageService.getGroupMessagePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "群消息.xls", "数据", GroupMessageRespVO.class,
                        BeanUtils.toBean(list, GroupMessageRespVO.class));
    }

}

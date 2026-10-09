package com.htyoudao.youdao.module.member.controller.admin.pointsLog;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;

import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTask;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTaskError;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.job.point.PointJob;
import com.htyoudao.youdao.module.member.service.job.JobService;
import com.htyoudao.youdao.module.member.service.pointsLog.IPointsLogService;
import com.htyoudao.youdao.module.member.service.pointsLog.PointsImportService;
import com.htyoudao.youdao.module.member.service.pointsLog.PointsImportTaskService;
import com.htyoudao.youdao.module.member.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;



@Tag(name = "管理后台 - 积分记录")
@RestController
@Slf4j
@RequestMapping("/member/points-log")
public class PointsLogController {

    @Resource
    private IPointsLogService pointsLogService;

    @Resource
    private PointJob pointJob;

    @Resource
    private PointsImportTaskService pointsImportTaskService;

    @Autowired
    private PointsImportService pointsImportService;

    @Resource
    private ExcelActionService excelActionService;
    @Operation(summary = "查询积分记录列表")
    @GetMapping("/list")
    public CommonResult<PageResult<PointsLogDO>> list(@RequestParam(value = "pageNo", required = false) long pageNo,
                                                      @RequestParam(value = "pageSize", required = false) long pageSize, PointsLogPageListReqVo pageListReqVo) {
        PageResult<PointsLogDO> list = pointsLogService.selectPointsLogListPage(pageNo, pageSize, pageListReqVo);
        return success(list);
    }


    @Operation(summary = "查询积分兑换记录")
    @GetMapping("/exchange/list")
    public CommonResult<PageResult<PointsLogDO>> exchangeList(@RequestParam(value = "pageNo", required = false) long pageNo,
                                                              @RequestParam(value = "pageSize", required = false) long pageSize, PointsLogExchangeListReqVo exchangeListReqVo) {
        PageResult<PointsLogDO> pointsLogs = pointsLogService.selectExchangeListPage(pageNo, pageSize, exchangeListReqVo);
        return success(pointsLogs);
    }

//    @PreAuthorize("@ss.hasPermission('member:express:update')")
    @Operation(summary = "修改积分记录")
    @PostMapping("/edit")
    public CommonResult<Integer> edit(@Valid @RequestBody PointsLogEditReqVO pointsLogVO) {
        return CommonResult.success(pointsLogService.updatePointsLog(pointsLogVO));
    }

    @GetMapping("/export")
    @Operation(summary = "导出兑换记录")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> export(@Valid PointsLogExportReqVo pointsLogExportReqVo, HttpServletRequest request, HttpServletResponse response) throws ServerException {
        pointsLogService.export(pointsLogExportReqVo, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }


    @PostMapping("/import/update")
    @Operation(summary = "导入")
    public CommonResult<String> importUpdateExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 校验文件扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                throw new ServiceException(new ErrorCode(500, "仅支持xlsx、xls格式文件"));
            }

            // 2. 校验文件格式（表头/列映射是否正确）
            byte[] fileBytes = file.getBytes();
            validateExcelFormatFast(fileBytes, originalFilename);

            // 3. 检查文件行数
            int rowCount = ExcelUtils.getRowCount(fileBytes, originalFilename);
            if (rowCount > 1000) {
                throw new ServiceException(new ErrorCode(500, "单个文件最多可以导入1000条数据，请分批次导入"));
            }

            if (rowCount == 0) {
                throw new ServiceException(new ErrorCode(500, "文件为空，请至少填写一条数据"));
            }
            // ==========  新增：校验兑换编码是否为空 ==========
            validatePointsLogIdNotEmpty(fileBytes, originalFilename);


            // 4. 创建导入任务
            String taskId = pointsImportTaskService.createUpdateTask(file.getOriginalFilename());

            // 5. 异步处理导入
            pointsImportService.processUpdateImportAsync(fileBytes, originalFilename, taskId);

            return CommonResult.success("导入任务已创建，任务ID：" + taskId);
        } catch (Exception e) {
            log.error("导入失败", e);
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 查询导入任务状态
     */
    @GetMapping("/import/task/{taskId}")
    @Operation(summary = "查询导入任务状态")
    public CommonResult<PointsImportTask> getImportTask(@PathVariable String taskId) {
        PointsImportTask task = pointsImportTaskService.getTask(taskId);
        return CommonResult.success(task);
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/getAllTasks")
    @Operation(summary = "获取任务列表")
    public CommonResult<PageResult<PointsImportTask>> getTaskList(PageParam pageParam) {
        PageResult<PointsImportTask> tasks = pointsImportTaskService.getAllTasks(pageParam);
        return CommonResult.success(tasks);
    }





    @Operation(summary = "新增积分记录")
    @PostMapping
    public CommonResult<Integer> insertPointsLog(@Valid @RequestBody PointsLogSaveReqVO saveReqVO) {
        return CommonResult.success(pointsLogService.insertPointsLog(saveReqVO));
    }

    @PostMapping("/addMemberPoint")
    public CommonResult<Boolean> addMemberPoint(@RequestBody ClientAddMemberPointReqVO reqVO) {

        return success( pointsLogService.addMemberPoint(reqVO));
    }


    /**
     * 清理过期积分
     */
    @GetMapping("/clearExpiredPoints/{businessId}")
    public void clearExpiredPoints(@PathVariable("businessId")Long businessId) {
        pointsLogService.clearExpiredPoints( businessId);
    }

    @Resource
    private JobService jobService;


    @GetMapping("/updateMemberPointJobHandler/{start}/{end}/{memberId}/{businessId}")
    public void updateMemberPointJobHandler(@PathVariable("start") int start, @PathVariable("end") int end, @PathVariable("memberId") long memberId, @PathVariable("businessId") long businessId) {
        System.out.println("updateMemberPointJobHandler is running.");
        jobService.updateMemberPointTask(start, end, memberId, businessId);
    }

    private void validateExcelFormatFast(byte[] fileBytes, String filename) {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            // 使用 ReadWorkbook 只读取表头信息，不读取数据
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, PointsUpdateImportDTO.class, new AnalysisEventListener<PointsUpdateImportDTO>() {
                @Override
                public void invoke(PointsUpdateImportDTO data, AnalysisContext context) {
                    // 不处理数据
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            });

            // 设置只读取表头行
            ExcelReader excelReader = readerBuilder.build();
            ReadSheet readSheet = EasyExcel.readSheet(0).build();

            try {
                // 读取表头信息（不会读取实际数据）
                excelReader.read(readSheet);
            } finally {
                excelReader.finish();
            }
            // 如果没有抛出异常，说明表头映射成功
        } catch (ExcelAnalysisException e) {
            // EasyExcel 在字段映射失败时会抛出 ExcelAnalysisException
            log.error("Excel格式校验失败: 字段映射错误", e);
            throw new ServiceException(new ErrorCode(500, "表格内字段有误，请重新上传"));
        } catch (Exception e) {
            log.error("Excel格式校验失败", e);
            throw new ServiceException(new ErrorCode(500, "表格内字段有误，请重新上传"));
        }
    }


    /**
     * 校验Excel中兑换编码(pointsLogId)是否为空
     */
    private void validatePointsLogIdNotEmpty(byte[] fileBytes, String filename) {
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        List<PointsUpdateImportDTO> updateList = excelActionService.importExcel(inputStream, PointsUpdateImportDTO.class);

        if (updateList == null || updateList.isEmpty()) {
            return;
        }

        List<Integer> emptyRows = new ArrayList<>();

        for (int i = 0; i < updateList.size(); i++) {
            PointsUpdateImportDTO dto = updateList.get(i);
            if (dto.getPointsLogId() == null) {  // 改为检查 pointsLogId
                emptyRows.add(i + 2);
            }
        }

        if (!emptyRows.isEmpty()) {
            throw new ServiceException(new ErrorCode(500, "兑换编码不能为空，请填写后重新上传"));
        }
    }


}

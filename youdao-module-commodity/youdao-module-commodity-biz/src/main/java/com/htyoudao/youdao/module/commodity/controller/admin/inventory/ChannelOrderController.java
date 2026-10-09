package com.htyoudao.youdao.module.commodity.controller.admin.inventory;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportTask;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportTaskError;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ChannelElemeOrder;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ChannelSankuaiOrder;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import com.htyoudao.youdao.module.commodity.service.inventory.ChannelOrderImportService;
import com.htyoudao.youdao.module.commodity.service.inventory.ImportTaskService;
import com.htyoudao.youdao.module.commodity.util.ProductParserUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/commodity/channel")
public class ChannelOrderController {

    @Autowired
    private ChannelOrderImportService orderImportService;

    @Autowired
    private ImportTaskService importTaskService;

    @PostMapping("/import")
    @Operation(summary = "导入表格")
    public void importExcel(@RequestParam("file") MultipartFile file, @RequestParam String code) {
        // 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(xlsx|xls|csv)$")) {
            throw new ServiceException(new ErrorCode(500, "仅支持xlsx、xls、csv格式文件"));
        }
        
        String taskId = importTaskService.createTask(file.getOriginalFilename(), code);
        ChannelType channelType = ChannelType.getByCode(code);

        try {
            // 在主线程中读取文件字节，避免 Undertow 删除临时文件
            byte[] fileBytes = file.getBytes();

            // 根据类型选择异步处理
            switch (channelType) {
                case ELE_ME -> orderImportService.processImportAsync(fileBytes, originalFilename, ChannelElemeOrder.class, taskId, channelType);
                case SAN_KUAI -> orderImportService.processImportAsync(fileBytes, originalFilename, ChannelSankuaiOrder.class, taskId, channelType);
                default -> throw new IllegalArgumentException("不支持的渠道类型：" + code);
            }
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            importTaskService.updateTaskFailed(taskId, e.getMessage());
        }
    }


    @PostMapping("/importAndParse")
    @Operation(summary = "导入表格 并且解析聚合出商品销量")
    public CommonResult<List<ProductQuantity>> importAndGroupExcel(@RequestParam("file") MultipartFile file,
        @RequestParam String code) throws IOException {

        List<ProductQuantity> quantities = new ArrayList<>();
        ChannelType channelType = ChannelType.getByCode(code);

        switch (channelType) {
            case ELE_ME -> {
                orderImportService.importOrdersFromExcel(file.getInputStream(),
                        ChannelElemeOrder.class)
                    .stream().map(o -> ProductParserUtil.parseProducts(o.getProductInfo(), channelType))
                    .forEach(quantities::addAll);
            }
            case SAN_KUAI -> {
                orderImportService.importOrdersFromExcel(file.getInputStream(),
                        ChannelSankuaiOrder.class)
                    .stream().map(o -> ProductParserUtil.parseProducts(o.getProductInfo(), channelType))
                    .forEach(quantities::addAll);
            }
        }

        //聚合相同商品
        return CommonResult.success(ProductParserUtil.aggregateSameProducts(quantities));
    }


    /**
     * 下载失败文件
     */
    @GetMapping("/download-error/{taskId}")
    @Operation(summary = "下载失败文件")
    public void downloadErrorFile(@PathVariable String taskId, @RequestParam String code,
        HttpServletResponse response) throws IOException {
        List<ImportTaskError> errors = importTaskService.getTaskErrors(taskId);

        if (CollectionUtils.isEmpty(errors)) {
            return;
        }

        List data = null;
        Class clazz = null;

        ChannelType channelType = ChannelType.getByCode(code);
        switch (ChannelType.getByCode(code)) {
            case ELE_ME -> {
                data = errors.stream().map(e -> {
                    ChannelElemeOrder channelElemeOrder = JSON.parseObject(e.getRowData(), ChannelElemeOrder.class);
                    channelElemeOrder.setErrorMessage(e.getErrorMessage());
                    return channelElemeOrder;
                }).toList();
                clazz = ChannelElemeOrder.class;
            }

            case SAN_KUAI -> {
                data = errors.stream().map(e -> {
                    ChannelSankuaiOrder order = JSON.parseObject(e.getRowData(), ChannelSankuaiOrder.class);
                    order.setErrorMessage(e.getErrorMessage());
                    return order;
                }).toList();
                clazz = ChannelSankuaiOrder.class;
            }
        }

        String fileName = String.format("%s_失败原因_%s.xls", channelType.getDescription(), taskId);
        ExcelUtils.write(response, fileName, "失败原因", clazz, data);
    }


    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取任务列表")
    public CommonResult<PageResult<ImportTask>> getTaskList(PageParam pageParam) {
        PageResult<ImportTask> tasks = importTaskService.getAllTasks(pageParam);
        return CommonResult.success(tasks);
    }

}
package com.htyoudao.youdao.module.commodity.controller.app.materialStocktake;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageVO;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialRespVO;
import com.htyoudao.youdao.module.commodity.service.inventory.RawMaterialStocktakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "老板助手 - 盘点")
@RestController
@RequestMapping("/commodity/material/stocktake/app/boss")
public class AppBossStocktakeController {

    @Resource
    private RawMaterialStocktakeService stocktakeService;


    @PostMapping("/list")
    @Operation(summary = "盘点列表")
    @PermitAll
    public CommonResult<PageResult<MaterialStocktakePageVO>> boosStocktakeList(
        @Valid @RequestBody MaterialStocktakePageReq pageReq) {
        return CommonResult.success(stocktakeService.stocktakeList(pageReq));
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    @PermitAll
    public CommonResult<RawMaterialRespVO> boosStocktakeDetail(@RequestParam Long id) {
        return CommonResult.success(stocktakeService.stocktakeDetail(id));
    }

    @GetMapping("/export")
    @Operation(summary = "导出")
    @PermitAll
    public void boosStocktakeExport(@RequestParam Long id, HttpServletResponse response) throws IOException {

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("原材料库存盘点表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 导出Excel
        RawMaterialRespVO data = stocktakeService.stocktakeDetail(id);

        // 导出Excel
        stocktakeService.export(data, response.getOutputStream());
    }

    @GetMapping("/exportZip")
    @Operation(summary = "压缩包批量下载")
    @PermitAll
    public void boosStocktakeExportZip(@RequestParam String ids, HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("原材料库存盘点表批量下载", StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : Arrays.stream(ids.split(",")).map(Long::valueOf).toList()) {
                // 获取每个id对应的数据
                RawMaterialRespVO data = stocktakeService.stocktakeDetail(id);

                // 创建Zip条目，每个Excel文件单独一个条目
                String entryName = "原材料库存盘点表_" + id + ".xlsx";
                zipOut.putNextEntry(new ZipEntry(entryName));

                // 创建一个临时的字节数组输出流来捕获Excel数据
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                stocktakeService.export(data, baos);

                // 将字节数组写入zip输出流
                zipOut.write(baos.toByteArray());

                // 关闭当前条目
                zipOut.closeEntry();

                // 刷新输出流
                zipOut.flush();
            }
            zipOut.finish();
        } catch (Exception e) {
            // 异常处理
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<>();
            map.put("status", "failure");
            map.put("message", "下载文件失败: " + e.getMessage());
            response.getWriter().println(map);
        }
    }
}

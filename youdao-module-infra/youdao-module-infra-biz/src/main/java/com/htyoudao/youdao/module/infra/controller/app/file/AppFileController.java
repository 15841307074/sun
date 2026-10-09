package com.htyoudao.youdao.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import com.htyoudao.youdao.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import com.htyoudao.youdao.module.infra.service.file.AliyunGreenImageModerationService;
import com.htyoudao.youdao.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.FILE_IS_EMPTY;

@Tag(name = "用户 App - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class AppFileController {

    @Resource
    private FileService fileService;

    @Resource
    private AliyunGreenImageModerationService imageModerationService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PermitAll
    public CommonResult<String> uploadFile(AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        String path = uploadReqVO.getPath();

        if (file == null || file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        byte[] content = IoUtil.readBytes(file.getInputStream());
        imageModerationService.check(file.getOriginalFilename(), content);
        return success(fileService.createFile(file.getOriginalFilename(), path, content));
    }

    @PermitAll
    @GetMapping("/download")
    public void downloadFile(@RequestParam String fileUrl, @RequestParam String fileName, HttpServletResponse response) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            // 使用 RestTemplate 执行请求，获取响应流
            restTemplate.execute(fileUrl, HttpMethod.GET, null, (ClientHttpResponse resp) -> {
                // 获取文件类型
                String contentType = resp.getHeaders().getContentType() != null
                        ? resp.getHeaders().getContentType().toString()
                        : "application/octet-stream";
                response.setContentType(contentType);

                // 中文文件名防乱码
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

                // 设置内容长度（可选）
                if (resp.getHeaders().getContentLength() > 0) {
                    response.setContentLengthLong(resp.getHeaders().getContentLength());
                }

                // 流式复制数据
                try (InputStream in = resp.getBody(); OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, len > 0 ? 0 : len, len);
                    }
                    out.flush();
                }

                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址", description = "模式二：前端上传文件：用于前端直接上传七牛、阿里云 OSS 等文件存储器")
    @PermitAll
    public CommonResult<FilePresignedUrlRespVO> getFilePresignedUrl(@RequestParam("path") String path) throws Exception {
        return success(fileService.getFilePresignedUrl(path));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "模式二：前端上传文件：配合 presigned-url 接口，记录上传了上传的文件")
    @PermitAll
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

}

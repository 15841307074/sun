package com.htyoudao.youdao.module.infra.controller.admin.file;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.infra.api.file.dto.FileMultiPartCreateReqDTO;
import com.htyoudao.youdao.module.infra.controller.admin.file.vo.file.*;
import com.htyoudao.youdao.module.infra.dal.dataobject.file.FileDO;
import com.htyoudao.youdao.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.FILE_IS_EMPTY;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;
import static com.htyoudao.youdao.module.infra.framework.file.core.utils.FileTypeUtils.writeAttachment;

@Tag(name = "管理后台 - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "模式一：后端上传文件")
    public CommonResult<String> uploadFile(FileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        String path = uploadReqVO.getPath();

        if (file == null || file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        return success(fileService.createFile(file.getOriginalFilename(), path, IoUtil.readBytes(file.getInputStream())));
    }

    @PostMapping("/multiPartCreate")
    @Operation(summary = "上传文件", description = "分片上传")
    public CommonResult<String> multiPartCreate(@RequestBody FileMultiPartCreateReqDTO createReqDTO) throws Exception {
        File file = createReqDTO.getFile();
        return success(fileService.multiPartCreateFile(file));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址", description = "模式二：前端上传文件：用于前端直接上传七牛、阿里云 OSS 等文件存储器")
    public CommonResult<FilePresignedUrlRespVO> getFilePresignedUrl(@RequestParam("path") String path) throws Exception {
        return success(fileService.getFilePresignedUrl(path));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "模式二：前端上传文件：配合 presigned-url 接口，记录上传了上传的文件")
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) throws Exception {
        fileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/{configId}/get/**")
    @PermitAll
    @Operation(summary = "下载文件")
    @Parameter(name = "configId", description = "配置编号", required = true)
    public void getFileContent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("configId") Long configId) throws Exception {

        String requestURI = request.getRequestURI();

        // 获取请求的路径
        String path = StrUtil.subAfter(requestURI, "/get/", false);
        //CND和OSS均已.com结束
        if(path.contains(".com")){
            path = path.substring(path.lastIndexOf(".com") + 5);
        }
        if (StrUtil.isEmpty(path)) {
            throw new IllegalArgumentException("结尾的 path 路径必须传递");
        }
        // 解码，解决中文路径的问题 https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/807/
        path = URLUtil.decode(path);

        // 读取内容
        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        String fileName = path.substring(path.lastIndexOf("/") + 1);
        writeAttachment(response, request, fileName, content);
    }

    public static void main(String[] args) {
        String path = "https:/stage.files.htyoudao.com/excels/20250411/%E7%94%A8%E6%88%B7%E5%88%97%E8%A1%A8_20250411090757.xlsx";
        System.out.println(path.substring(path.lastIndexOf(".com") + 4));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文件分页")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<FileRespVO>> getFilePage(@Valid FilePageReqVO pageVO) {
        PageResult<FileDO> pageResult = fileService.getFilePage(pageVO);
        return success(BeanUtils.toBean(pageResult, FileRespVO.class));
    }

}

package com.htyoudao.youdao.module.system.controller.app.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.dal.dataobject.sysconfig.ScmIosLinkDO;
import com.htyoudao.youdao.module.system.service.sysconfig.IScmIosLinkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.IOS_LINK_NOT_EXISTS;

@Tag(name = "下载链接")
@RestController
@RequestMapping("/system")
public class IosLinkController {

    @Resource
    private IScmIosLinkService iScmIosLinkService;

    @PermitAll
    @CrossOrigin(origins = "https://h5.htyoudao.com")
    @GetMapping("/iosLink")
    public CommonResult<ScmIosLinkDO> getIosLink() {

        List<ScmIosLinkDO> list = iScmIosLinkService.list(
                new LambdaQueryWrapper<ScmIosLinkDO>()
                        .last(" LIMIT 1")
        );

        if (CollectionUtils.isEmpty(list)) {
            throw exception(IOS_LINK_NOT_EXISTS);
        }

        ScmIosLinkDO scmIosLink = list.get(0);
        iScmIosLinkService.removeById(scmIosLink.getId());

        return CommonResult.success(scmIosLink);
    }
}
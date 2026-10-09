package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.dept.dto.PostRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.PostDO;
import com.htyoudao.youdao.module.system.service.dept.PostService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class PostApiImpl implements PostApi {

    @Resource
    private PostService postService;

    @Override
    public CommonResult<Boolean> validPostList(Collection<Long> ids) {
        postService.validatePostList(ids);
        return success(true);
    }

    @Override
    public CommonResult<List<PostRespDTO>> getPostList(Collection<Long> ids) {
        List<PostDO> list = postService.getPostList(ids);
        return success(BeanUtils.toBean(list, PostRespDTO.class));
    }

}

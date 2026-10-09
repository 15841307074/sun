package com.htyoudao.youdao.module.system.service.appversion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.appversion.vo.AppVersionConfigUpdateReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.appversion.AppVersionConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.appversion.AppVersionConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class AppVersionConfigService {
    private static final List<String> PROJECT_CODES = List.of("cn.hbgc.saas", "com.boos.saas",
            "com.dcj.saas", "com.boss.saas", "com.0090.boss.prod", "com.boos.saas.old");
    private final AppVersionConfigMapper mapper;

    public AppVersionConfigService(AppVersionConfigMapper mapper) {
        this.mapper = mapper;
    }

    public List<AppVersionConfigDO> list() {
        return mapper.selectList(new LambdaQueryWrapper<AppVersionConfigDO>()
                .in(AppVersionConfigDO::getProjectCode, PROJECT_CODES)
                .orderByDesc(AppVersionConfigDO::getId));
    }

    public void update(@Valid AppVersionConfigUpdateReqVO req) {
        // 条件更新同时限制旧记录的项目范围；不允许客户端修改归属、删除标识及创建信息。
        LambdaUpdateWrapper<AppVersionConfigDO> update = new LambdaUpdateWrapper<AppVersionConfigDO>()
                .eq(AppVersionConfigDO::getId, req.getId())
                .in(AppVersionConfigDO::getProjectCode, PROJECT_CODES)
                .set(req.getProjectName() != null, AppVersionConfigDO::getProjectName, req.getProjectName())
                .set(req.getVersionCode() != null, AppVersionConfigDO::getVersionCode, req.getVersionCode())
                .set(req.getCode() != null, AppVersionConfigDO::getCode, req.getCode())
                .set(req.getContent() != null, AppVersionConfigDO::getContent, req.getContent())
                .set(req.getForceUpdate() != null, AppVersionConfigDO::getForceUpdate, req.getForceUpdate())
                .set(req.getActiveVersionCode() != null, AppVersionConfigDO::getActiveVersionCode, req.getActiveVersionCode())
                .set(req.getFilePath() != null, AppVersionConfigDO::getFilePath, req.getFilePath())
                .set(req.getModuleName() != null, AppVersionConfigDO::getModuleName, req.getModuleName())
                .set(AppVersionConfigDO::getUpdateTime, LocalDateTime.now())
                .set(AppVersionConfigDO::getUpdateUserName, SecurityFrameworkUtils.getLoginUserNickname());
        if (mapper.update(null, update) == 0) {
            throw exception(new ErrorCode(404, "版本配置不存在、已删除或项目编号不在允许范围内"));
        }
    }
}

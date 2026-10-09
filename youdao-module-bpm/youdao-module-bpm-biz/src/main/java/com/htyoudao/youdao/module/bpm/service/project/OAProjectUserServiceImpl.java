package com.htyoudao.youdao.module.bpm.service.project;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.project.OAProjectUserDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.oa.OAProjectUserMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * OA 项目 Service 实现类
 *
 * @author jason
 * @author 0090
 */
@Service
public class OAProjectUserServiceImpl extends ServiceImpl<OAProjectUserMapper, OAProjectUserDO> implements OAProjectUserService {


    @Resource
    private OAProjectUserMapper oaProjectUserMapper;

    @Resource
    private OAProjectUserService oaProjectUserService;


    @Override
    public void batchUpdateByProjectId(Long oaProjectId, List<Long> newProjectUsers) {
        removeByOAProjectId(oaProjectId);
        batchSaveByProjectId(oaProjectId, newProjectUsers);
    }

    @Override
    public List<Long> getUserIdsByProjectId(Long oaProjectId) {

        List<OAProjectUserDO> list = oaProjectUserMapper.selectList(OAProjectUserDO::getOaProjectId, oaProjectId);
        if (CollectionUtils.isEmpty(list)){
            return Collections.EMPTY_LIST;
        }

        return list.stream().map(OAProjectUserDO::getUserId).toList();
    }

    @Override
    public void batchSaveByProjectId(Long oaProjectId, List<Long> projectUsers) {
        if (oaProjectId == null || CollectionUtils.isEmpty(projectUsers)){
            return;
        }

        List<OAProjectUserDO> list = new ArrayList<>();
        for (Long projectUser : projectUsers) {
            OAProjectUserDO projectUserDO = new OAProjectUserDO();
            projectUserDO.setOaProjectId(oaProjectId);
            projectUserDO.setUserId(projectUser);
            list.add(projectUserDO);
        }
        oaProjectUserMapper.insertBatch(list);
    }

    @Override
    public void removeByOAProjectId(Long oaProjectId) {
        if (oaProjectId == null){
            return;
        }

        oaProjectUserMapper.delete(OAProjectUserDO::getOaProjectId, oaProjectId);
    }
}

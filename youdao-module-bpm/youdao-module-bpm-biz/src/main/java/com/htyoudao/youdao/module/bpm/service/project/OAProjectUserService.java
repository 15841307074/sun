package com.htyoudao.youdao.module.bpm.service.project;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.bpm.dal.dataobject.project.OAProjectUserDO;
import java.util.List;

/**
 * oa项目成员 Service 接口
 *
 * @author jason
 * @author 0090
 */
public interface OAProjectUserService extends IService<OAProjectUserDO> {

    void batchUpdateByProjectId(Long id, List<Long> projectUsers);

    List<Long> getUserIdsByProjectId(Long oaProjectId);

    void batchSaveByProjectId(Long id, List<Long> projectUsers);

    void removeByOAProjectId(Long id);

}

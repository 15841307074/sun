package com.htyoudao.youdao.module.bpm.dal.dataobject.project;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * OA项目成员表
 */
@TableName("oa_project_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAProjectUserDO  extends BaseDO {

    @Id
    private Long id;

    private Long oaProjectId;

    private Long userId;
}

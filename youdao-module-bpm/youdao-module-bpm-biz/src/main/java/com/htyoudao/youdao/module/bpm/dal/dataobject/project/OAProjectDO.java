package com.htyoudao.youdao.module.bpm.dal.dataobject.project;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * oa项目
 */
@TableName("oa_project")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAProjectDO extends BaseDO {

    @Id
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 所属部门ID 逗号分隔
     */
    private String deptIds;

//    /**
//     * 部门名称
//     */
//    private String deptName;

    /**
     * 项目负责人ID
     */
    private Long projectLeader;

    /**
     * 项目成员（多个成员用逗号分隔）
     */
    private String projectMembers;

    /**
     * 项目开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 项目结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 项目描述（最多500字）
     */
    private String projectDescription;

    /**
     * 项目状态：0-已结束 1-进行中 2-暂停 3-取消
     */
    private Integer status = 1;
}

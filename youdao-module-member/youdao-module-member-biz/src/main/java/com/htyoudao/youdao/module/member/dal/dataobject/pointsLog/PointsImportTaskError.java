package com.htyoudao.youdao.module.member.dal.dataobject.pointsLog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

@Data
@TableName("points_import_task_error")
public class PointsImportTaskError extends BusinessBaseDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private String rowData;
    private String errorMessage;
}
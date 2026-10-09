package com.htyoudao.youdao.module.member.dal.dataobject.pointsLog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

@Data
@TableName("points_import_task")
public class PointsImportTask extends BusinessBaseDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private String channelType;  // 用于区分导入类型：UPDATE_ADDRESS 或 IMPORT_ORDER
    private String taskId;
    private String fileName;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private String errorFileUrl;
    private String remark;

    @TableField(exist = false)
    private String userName;
    
    public enum ImportStatus {
        PROCESSING, COMPLETED, FAILED
    }
}

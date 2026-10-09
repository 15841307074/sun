package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("import_task")
public class ImportTask extends BusinessBaseDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskId;
    private String fileName;
    private String channelType;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private String errorFileUrl;
    private String remark;
    private String storeId;
    private String productParam;
    
    public enum ImportStatus {
        PROCESSING, COMPLETED, FAILED
    }
}

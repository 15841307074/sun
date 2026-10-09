package com.htyoudao.youdao.module.system.dal.dataobject.appversion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 旧版本配置表使用独立字段命名，不继承 BaseDO。 */
@Data
@TableName("app_version_config")
public class AppVersionConfigDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectName;
    private String projectCode;
    private String versionCode;
    private String code;
    private String content;
    private String forceUpdate;
    private LocalDateTime createTime;
    private String createUserName;
    private LocalDateTime updateTime;
    private String updateUserName;
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
    private Integer activeVersionCode;
    private String filePath;
    private String moduleName;
    private Long projectOwnerShip;
}

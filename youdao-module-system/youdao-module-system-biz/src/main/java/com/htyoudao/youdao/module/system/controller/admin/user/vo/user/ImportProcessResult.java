package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import lombok.Data;

import java.util.*;

/**
 * 导入处理结果封装类
 */
@Data
public class ImportProcessResult {
    /** 用户部门关系列表 */
    private List<UserDeptDO> userDeptList = new ArrayList<>();
    /** 待插入的管理员用户列表 */
    private List<AdminUserDO> insertUsers = new ArrayList<>();
    /** 待插入的业务用户列表 */
    private List<BusinessUserDO> insertBusinessUsers = new ArrayList<>();
    /** 新创建用户的手机号-ID映射 */
    private Map<String, Long> newMobileIdMap = new HashMap<>();

    private Set<Long> userIds = new HashSet<>();
}
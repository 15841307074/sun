package com.htyoudao.youdao.module.system.framework.datapermission.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementDO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementStoreDO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.taggroup.TagGroupDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的数据权限 Configuration
 *
 * @author 0090
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // business
            rule.addBusinessColumn(OrgDO.class);
            rule.addBusinessColumn(RoleDO.class);
            rule.addBusinessColumn(BusinessUserDO.class);

            // 投诉
            rule.addBusinessColumn(ComplaintDO.class);
         //   rule.addStoreColumn(ComplaintDO.class);

            // 组织
            rule.addBusinessColumn(OrgDO.class);
            rule.addBusinessColumn(OrgUserDO.class);
            rule.addBusinessColumn(StoreUserDO.class);
            rule.addBusinessColumn(TagGroupDO.class);
            rule.addBusinessColumn(TagValueDO.class);
            //门店
            rule.addBusinessColumn(SystemStoreInfoDO.class);

            //小程序
            rule.addBusinessColumn(AppletPageManagementDO.class);
            rule.addBusinessColumn(AppletPageManagementStoreDO.class);
            rule.addBusinessColumn(AppletPageManagementTagDO.class);

            // 操作日志
            rule.addBusinessColumn(OperateLogDO.class);

            // 部门
            rule.addBusinessColumn(DeptDO.class);

            //todo 后台页面显示business_id
        };
    }

}

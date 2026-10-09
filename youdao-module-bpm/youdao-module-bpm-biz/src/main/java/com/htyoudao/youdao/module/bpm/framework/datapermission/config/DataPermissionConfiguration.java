package com.htyoudao.youdao.module.bpm.framework.datapermission.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionChecklistDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionTypeDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.StoreInspectionTemplateDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.TemplateChecklistDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.TemplateVisibleUserDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
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
            // 巡检
            rule.addBusinessColumn(StoreInspectionItemLogDO.class);
            rule.addBusinessColumn(StoreInspectionRecordDO.class);
            rule.addBusinessColumn(StoreInspectionRecordItemDO.class);

            rule.addBusinessColumn(StoreInspectionChecklistDO.class);
            rule.addBusinessColumn(StoreInspectionTypeDO.class);
            rule.addBusinessColumn(StoreInspectionTemplateDO.class);
            rule.addBusinessColumn(TemplateChecklistDO.class);
            rule.addBusinessColumn(TemplateVisibleUserDO.class);


            //todo 后台页面显示business_id
        };
    }

}

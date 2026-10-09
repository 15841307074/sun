package com.htyoudao.youdao.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 通用参数填充实现类
 * <p>
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author hexiaowu
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject)) {
            Object originalObject = metaObject.getOriginalObject();
            if (originalObject instanceof BaseDO baseDO) {
                LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                // 创建时间为空，则以当前时间为插入时间
                if (Objects.isNull(baseDO.getCreateTime())) {
                    baseDO.setCreateTime(current);
                } else {
                    //忽略毫秒
                    baseDO.setCreateTime(baseDO.getCreateTime().truncatedTo(ChronoUnit.SECONDS));
                }
                // 更新时间为空，则以当前时间为更新时间
                if (Objects.isNull(baseDO.getUpdateTime())) {
                    baseDO.setUpdateTime(current);
                } else {
                    //忽略毫秒
                    baseDO.setUpdateTime(baseDO.getUpdateTime().truncatedTo(ChronoUnit.SECONDS));
                }

                Long userId = SecurityFrameworkUtils.getLoginUserId();
                // 当前登录用户不为空，创建人为空，则当前登录用户为创建人
                if (Objects.nonNull(userId) && Objects.isNull(baseDO.getCreator())) {
                    baseDO.setCreator(userId.toString());
                }
                // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
                if (Objects.nonNull(userId) && Objects.isNull(baseDO.getUpdater())) {
                    baseDO.setUpdater(userId.toString());
                }

                if (originalObject instanceof BusinessBaseDO baseBusinessDO) {
                    // 当前项目 id不为空，则当前项目 id为header 项目 id
                    Long businessId = BusinessContextHolder.getBusinessId();
                    if (Objects.nonNull(businessId) && Objects.isNull(baseBusinessDO.getBusinessId())) {
                        baseBusinessDO.setBusinessId(businessId);
                    }
                }
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间为空，则以当前时间为更新时间
        Object modifyTime = getFieldValByName("updateTime", metaObject);
        if (Objects.isNull(modifyTime)) {
            setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

        // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
        Object modifier = getFieldValByName("updater", metaObject);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (Objects.nonNull(userId) && Objects.isNull(modifier)) {
            setFieldValByName("updater", userId.toString(), metaObject);
        }
    }
}

package com.htyoudao.youdao.framework.datapermission.core.rule.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.rule.DataPermissionRule;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.util.MyBatisUtils;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.permission.PermissionApi;
import com.htyoudao.youdao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于部门的 {@link DataPermissionRule} 数据权限规则实现
 *
 * 注意，使用 DeptDataPermissionRule 时，需要保证表中有 dept_id 部门编号的字段，可自定义。
 *
 * 实际业务场景下，会存在一个经典的问题？当用户修改部门时，冗余的 dept_id 是否需要修改？
 * 1. 一般情况下，dept_id 不进行修改，则会导致用户看不到之前的数据。【youdao-server 采用该方案】
 * 2. 部分情况下，希望该用户还是能看到之前的数据，则有两种方式解决：【需要你改造该 DeptDataPermissionRule 的实现代码】
 *  1）编写洗数据的脚本，将 dept_id 修改成新部门的编号；【建议】
 *      最终过滤条件是 WHERE dept_id = ?
 *  2）洗数据的话，可能涉及的数据量较大，也可以采用 user_id 进行过滤的方式，此时需要获取到 dept_id 对应的所有 user_id 用户编号；
 *      最终过滤条件是 WHERE user_id IN (?, ?, ? ...)
 *  3）想要保证原 dept_id 和 user_id 都可以看的到，此时使用 dept_id 和 user_id 一起过滤；
 *      最终过滤条件是 WHERE dept_id = ? OR user_id IN (?, ?, ? ...)
 *
 * @author 0090
 */
@AllArgsConstructor
@Slf4j
public class DeptDataPermissionRule implements DataPermissionRule {

    /**
     * LoginUser 的 Context 缓存 Key
     */
    protected static final String CONTEXT_KEY = DeptDataPermissionRule.class.getSimpleName();

    private static final String STORE_COLUMN_NAME = "store_id";
    private static final String BUSINESS_COLUMN_NAME = "business_id";
    private static final String ORG_COLUMN_NAME = "org_id";

    static final Expression EXPRESSION_NULL = new NullValue();

    private final PermissionApi permissionApi;

    /**
     * 基于门店的表字段配置
     * 一般情况下，每个表的组织编号字段是 store_id，通过该配置自定义。
     *
     * key：表名
     * value：字段名
     */
    private final Map<String, String> storeColumns = new HashMap<>();
    /**
     * 基于项目的表字段配置
     * 一般情况下，每个表的部门编号字段是 business_id，通过该配置自定义。
     *
     * key：表名
     * value：字段名
     */
    private final Map<String, String> businessColumns = new HashMap<>();

    private final Map<String, String> orgColumns = new HashMap<>();

    /**
     * 所有表名，是 {@link #storeColumns} 和 {@link #businessColumns} 的合集
     */
    private final Set<String> TABLE_NAMES = new HashSet<>();


    @Override
    public Set<String> getTableNames() {
        return TABLE_NAMES;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long businessId = BusinessContextHolder.getBusinessId();

        // 只有有登陆用户的情况下，才进行数据权限的处理
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return buildBusinessExpression(tableName, tableAlias, businessId);
        }
        // 只有管理员类型的用户，才进行数据权限的处理
        if (ObjectUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            return buildBusinessExpression(tableName, tableAlias, businessId);
        }

        // 获得数据权限
        DeptDataPermissionRespDTO deptDataPermission = loginUser.getContext(businessId.toString(), DeptDataPermissionRespDTO.class);
        // 从上下文中拿不到，则调用逻辑进行获取
        if (deptDataPermission == null) {
            deptDataPermission = permissionApi.getDeptDataPermission(loginUser.getId()).getCheckedData();
            if (deptDataPermission == null) {
                log.error("[getExpression][LoginUser({}) 获取数据权限为 null]", JsonUtils.toJsonString(loginUser));
                throw new NullPointerException(String.format("LoginUser(%d) Table(%s/%s) 未返回数据权限",
                        loginUser.getId(), tableName, tableAlias.getName()));
            }
            // 添加到上下文中，避免重复计算
            loginUser.setContext(businessId.toString(), deptDataPermission);
        }

        // 情况一，如果是 ALL 可查看全部，则无需拼接条件
        if (deptDataPermission.getAll()) {
            return buildBusinessExpression(tableName, tableAlias, deptDataPermission.getBusinessId());
        }

        // 情况二，即不能查看门店，又不能查看组织，则说明 100% 无权限
        if (CollUtil.isEmpty(deptDataPermission.getStoreIds()) && deptDataPermission.getBusinessId() == null) {
            log.warn("项目:{},用户数据权限获取失败!!:{}", businessId, JsonUtils.toJsonString(deptDataPermission));
            return new EqualsTo(null, null); // WHERE null = null，可以保证返回的数据为空
        }

        // 情况三，拼接 Store 和 Business 的条件，最后组合
        Expression storeExpression = buildStoreExpression(tableName, tableAlias, deptDataPermission.getStoreIds());
        Expression businessExpression = buildBusinessExpression(tableName, tableAlias, deptDataPermission.getBusinessId());
        if (storeExpression == null && businessExpression == null) {
            //0090：获得不到条件的时候，暂时不抛出异常，而是不返回数据
            log.warn("[getExpression][LoginUser({}) Table({}/{}) DeptDataPermission({}) 构建的条件为空]",
                    JsonUtils.toJsonString(loginUser), tableName, tableAlias, JsonUtils.toJsonString(deptDataPermission));
        }
        if (storeExpression == null) {
            return businessExpression;
        }
        if (businessExpression == null) {
            return storeExpression;
        }
        // 目前，如果有指定项目 + 可查看门店，采用  AND 条件。即，WHERE (store_id IN and business_id = ?)
        return new ParenthesedExpressionList(new AndExpression(storeExpression, businessExpression));
    }

    private Expression buildStoreExpression(String tableName, Alias tableAlias, Set<Long> storeIds) {
        // 如果不存在配置，则无需作为条件
        String columnName = storeColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 如果为空，则无条件
        if (CollUtil.isEmpty(storeIds)) {
            return null;
        }
        // 拼接条件
        return new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
                new ParenthesedExpressionList(new ExpressionList<LongValue>(CollectionUtils.convertList(storeIds, LongValue::new))));
    }


    private Expression buildOrgExpression(String tableName, Alias tableAlias, Set<Long> storeIds) {
        // 如果不存在配置，则无需作为条件
        String columnName = orgColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 如果为空，则无条件
        if (CollUtil.isEmpty(storeIds)) {
            return null;
        }
        // 拼接条件
        return new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
            // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
            new ParenthesedExpressionList(new ExpressionList<LongValue>(CollectionUtils.convertList(storeIds, LongValue::new))));
    }

    private Expression buildBusinessExpression(String tableName, Alias tableAlias, Long businessId) {

        String columnName = businessColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 拼接条件
        return new EqualsTo(MyBatisUtils.buildColumn(tableName, tableAlias, columnName), new LongValue(businessId));
    }

    // ==================== 添加配置 ====================

    public void addStoreColumn(Class<? extends BaseDO> entityClass) {
        addStoreColumn(entityClass, STORE_COLUMN_NAME);
    }

    public void addStoreColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addStoreColumn(tableName, columnName);
    }

    public void addStoreColumn(String tableName, String columnName) {
        storeColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }

    public void addBusinessColumn(Class<? extends BaseDO> entityClass) {
        addBusinessColumn(entityClass, BUSINESS_COLUMN_NAME);
    }

    public void addBusinessColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addBusinessColumn(tableName, columnName);
    }

    public void addBusinessColumn(String tableName, String columnName) {
        businessColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }



    public void addOrgColumn(Class<? extends BaseDO> entityClass) {
        addOrgColumn(entityClass, ORG_COLUMN_NAME);
    }

    public void addOrgColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addOrgColumn(tableName, columnName);
    }

    public void addOrgColumn(String tableName, String columnName) {
        orgColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }

}

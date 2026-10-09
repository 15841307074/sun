package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreFranchiseeInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店加盟商信息关系表 Mapper
 */
@Mapper
public interface SystemStoreFranchiseeInfoMapper extends BaseMapperX<SystemStoreFranchiseeInfoDO> {

    /**
     * 新增或恢复门店加盟商信息，门店已有记录时完整覆盖业务字段。
     *
     * @param franchiseeInfo 门店加盟商信息
     */
    void upsert(SystemStoreFranchiseeInfoDO franchiseeInfo);
}

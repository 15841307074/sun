package com.htyoudao.youdao.module.system.service.store;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SysStoreExtendDO;

/**
 * <p>
 * 店铺关系表 服务类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-08
 */
public interface SysStoreExtendService extends IService<SysStoreExtendDO> {

    Page<SysStoreExtendResVO> listPage(SysStoreExtendReqVO reqVO);

    void createOrUpdate(SysStoreExtendReqVO reqVO);

    void matchingField();


}

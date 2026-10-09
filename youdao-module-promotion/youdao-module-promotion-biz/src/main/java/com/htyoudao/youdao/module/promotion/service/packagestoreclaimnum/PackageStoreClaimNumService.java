package com.htyoudao.youdao.module.promotion.service.packagestoreclaimnum;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.packagestoreclaimnum.PackageStoreClaimNumDO;

import java.util.List;

/**
 * @author dht
 */
public interface PackageStoreClaimNumService extends IService<PackageStoreClaimNumDO> {

    /**
     * 根据券包id查询 各门店领取数量
     * @param id 券包id
     * @return List<PackageStoreClaimNumDO>
     */
    List<PackageStoreClaimNumDO> getByPackageId(Long id);
}

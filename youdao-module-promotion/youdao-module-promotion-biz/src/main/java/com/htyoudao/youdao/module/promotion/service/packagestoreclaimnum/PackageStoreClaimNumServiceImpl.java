package com.htyoudao.youdao.module.promotion.service.packagestoreclaimnum;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.packagestoreclaimnum.PackageStoreClaimNumDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.packagestoreclaimnum.PackageStoreClaimNumMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class PackageStoreClaimNumServiceImpl extends ServiceImpl<PackageStoreClaimNumMapper, PackageStoreClaimNumDO>  implements PackageStoreClaimNumService{

    @Resource
    private PackageStoreClaimNumMapper packageStoreClaimNumMapper;

    @Override
    public List<PackageStoreClaimNumDO> getByPackageId(Long id) {
        QueryWrapper<PackageStoreClaimNumDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("package_id",id);
        return packageStoreClaimNumMapper.selectList(queryWrapper);
    }
}

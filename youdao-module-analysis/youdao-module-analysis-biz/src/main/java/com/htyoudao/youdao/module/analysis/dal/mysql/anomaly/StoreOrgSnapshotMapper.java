package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/** 月度门店组织快照 Mapper。 */
@Mapper
public interface StoreOrgSnapshotMapper extends BaseMapperX<StoreOrgSnapshotDO> {

    default List<StoreOrgSnapshotDO> selectListByMonth(Long businessId, LocalDate snapshotMonth) {
        return selectList(new LambdaQueryWrapperX<StoreOrgSnapshotDO>()
                .eq(StoreOrgSnapshotDO::getBusinessId, businessId)
                .eq(StoreOrgSnapshotDO::getSnapshotMonth, snapshotMonth)
                .eq(StoreOrgSnapshotDO::getDeleted, false)
                .orderByAsc(StoreOrgSnapshotDO::getStoreId));
    }

    default Long selectCountByMonth(Long businessId, LocalDate snapshotMonth) {
        return selectCount(new LambdaQueryWrapperX<StoreOrgSnapshotDO>()
                .eq(StoreOrgSnapshotDO::getBusinessId, businessId)
                .eq(StoreOrgSnapshotDO::getSnapshotMonth, snapshotMonth)
                .eq(StoreOrgSnapshotDO::getDeleted, false));
    }

    /** 重跑时物理删除指定项目、指定月份的旧快照。 */
    @Delete("DELETE FROM analysis_store_org_snapshot " +
            "WHERE business_id = #{businessId} AND snapshot_month = #{snapshotMonth}")
    int physicalDeleteByMonth(@Param("businessId") Long businessId,
                              @Param("snapshotMonth") LocalDate snapshotMonth);
}

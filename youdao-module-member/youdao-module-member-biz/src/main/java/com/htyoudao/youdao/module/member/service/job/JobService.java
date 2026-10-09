package com.htyoudao.youdao.module.member.service.job;

import java.io.IOException;

public interface JobService {
    void updateMemberPointTask(int start, int end, long memberId, long business);

    void clearExpiredPoints(Long businessId);


    void updateHistoryOrderData();

    void rebuildHistoryOrderMetricsByEs(Long businessId, boolean resetTotalOrderNum);

    void updateMemberLabel();

    void updateFirstOrderStoreId();

    void nocHistoryOrderNum();

    void automaticDistributionOnMemberDaysJobHandler();

    void createMemberCrowdTask(long business) throws IOException;

    void updateMemberGroup();
}

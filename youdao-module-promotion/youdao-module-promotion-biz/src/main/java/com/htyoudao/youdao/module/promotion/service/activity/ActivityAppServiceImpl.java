package com.htyoudao.youdao.module.promotion.service.activity;

import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.member.api.wecom.WecomGroupApi;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyMapper;
import com.htyoudao.youdao.module.promotion.service.survey.SurveyService;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;


@Service
public class ActivityAppServiceImpl implements ActivityAppService  {

    @Resource
    private ActivityMapper activityMapper;

    @DubboReference
    private WecomGroupApi wecomGroupApi;

    @Resource
    private SurveyMapper surveyMapper;


    @Override
    public Boolean checkCanJoin(Long activityId) {
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (activityDO == null){
            throw new ServiceException(ErrorCodeConstants.BASE_ACTIVITY_NOT_FOUND);
        }

        return checkCanJoin(activityDO);
    }

    /**
     * 使用已加载的活动信息校验社群参与资格。
     */
    @Override
    public Boolean checkCanJoin(ActivityDO activityDO) {
        if (activityDO == null) {
            throw new ServiceException(ErrorCodeConstants.BASE_ACTIVITY_NOT_FOUND);
        }

        //校验社群信息
        if (!checkWecomUnionGroup(activityDO)) {
            throw new ServiceException(ErrorCodeConstants.BASE_ACTIVITY_NOT_IN_GROUP);
        }



        return true;
    }

    @Override
    public Boolean checkSurveyCanJoin(Long id) {
        SurveyDO surveyDO = surveyMapper.selectById(id);

        if (surveyDO == null){
            throw new ServiceException(ErrorCodeConstants.BASE_ACTIVITY_NOT_FOUND);
        }

        //校验社群信息
        if (!checkSurveyWecomUnionGroup(surveyDO)) {
            throw new ServiceException(ErrorCodeConstants.BASE_ACTIVITY_NOT_IN_GROUP);
        }



        return true;
    }


    /**
     * 校验社群信息
     * @param activityDO
     * @return
     */
    private Boolean checkWecomUnionGroup(ActivityDO activityDO) {
        //不需要校验社群
        if (!Objects.equals(activityDO.getCommunityFlag(),2)) {
            return true;
        }

        String loginUnionid = SecurityFrameworkUtils.getLoginUnionid();
        if (loginUnionid == null){
            return false;
        }

        WecomGroupCheckAnyMemberReqVO reqVO = new WecomGroupCheckAnyMemberReqVO();
        reqVO.setUnionId(loginUnionid);
        return wecomGroupApi.isUserInAnyGroup(reqVO).getCheckedData();
    }

    /**
     * 校验社群信息
     * @param surveyDO
     * @return
     */
    private Boolean checkSurveyWecomUnionGroup(SurveyDO surveyDO) {
        //不需要校验社群
        if (!Objects.equals(surveyDO.getCommunityOnly(),1)) {
            return true;
        }

        String loginUnionid = SecurityFrameworkUtils.getLoginUnionid();
        if (loginUnionid == null){
            return false;
        }

        WecomGroupCheckAnyMemberReqVO reqVO = new WecomGroupCheckAnyMemberReqVO();
        reqVO.setUnionId(loginUnionid);
        return wecomGroupApi.isUserInAnyGroup(reqVO).getCheckedData();
    }
}

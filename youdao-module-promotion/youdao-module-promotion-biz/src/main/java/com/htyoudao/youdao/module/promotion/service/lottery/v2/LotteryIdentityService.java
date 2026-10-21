package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import java.util.*;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/** 抽奖专用身份映射：手机号计数，会员ID仅定位真实账号和分片。 */
@Service
public class LotteryIdentityService {
    public static final ErrorCode MOBILE_REQUIRED = new ErrorCode(1_006_005_033, "请先绑定手机号后参与抽奖");
    @DubboReference(timeout=3000,retries=0) private WxMemberApi members;
    @Resource private JdbcTemplate jdbc;
    private JdbcTemplate mappingJdbc;
    @PostConstruct void initialize() {
        mappingJdbc=new JdbcTemplate(jdbc.getDataSource());mappingJdbc.setQueryTimeout(5);
    }
    private JdbcTemplate db(){return mappingJdbc==null?jdbc:mappingJdbc;}
    /** App调用只能使用登录账号；订单回调不调用此方法。 */
    public void authorize(Long memberId) {
        Long login=SecurityFrameworkUtils.getLoginUserId();
        if(login==null)throw exception(UNAUTHORIZED);
        if(memberId==null||!login.equals(memberId))throw exception(FORBIDDEN);
    }
    public String mobile(long memberId) {
        var response=members.getWxMemberById(memberId);
        if(response==null||!response.isSuccess()||response.getData()==null)throw exception(MOBILE_REQUIRED);
        return mobile(response.getData());
    }
    public String mobile(WxMemberVO member) {
        String value=member==null?null:member.getMemberMobile();
        if(value==null||value.isBlank())throw exception(MOBILE_REQUIRED);
        value=value.trim();
        if(value.startsWith("+86")&&value.length()==14)value=value.substring(3);
        if(!value.matches("[0-9]{6,20}"))throw exception(MOBILE_REQUIRED);
        return value;
    }
    /** 不占用抽奖事务，不在此处锁活动或手机号。 */
    public void register(long activityId,WxMemberVO member) {
        register(activityId,member.getMemberId(),mobile(member),member.getMemberCategory());
    }
    public void register(long activityId,long memberId,String mobile,Integer category) {
        db().update("INSERT INTO lottery_member_identity(business_id,activity_id,member_mobile,member_id,member_category,creator,updater) VALUES(?,?,?,?,?,'lottery','lottery') ON DUPLICATE KEY UPDATE member_category=COALESCE(member_category,VALUES(member_category))",
                BusinessContextHolder.getRequiredBusinessId(),activityId,mobile,memberId,category);
    }
    public List<String> recordPhones(String mobile) {
        return mobile.length()==11?List.of(mobile,"+86"+mobile):List.of(mobile);
    }
    public boolean matchesMobile(String mobile,String recorded) {
        return recorded!=null&&recordPhones(mobile).contains(recorded.trim());
    }
    public Set<Long> relatedMembers(String mobile,long current) {
        Set<Long> result=new LinkedHashSet<>();result.add(current);
        var phones=recordPhones(mobile);var args=new ArrayList<Object>();args.add(BusinessContextHolder.getRequiredBusinessId());args.addAll(phones);
        result.addAll(db().queryForList("SELECT DISTINCT member_id FROM lottery_member_identity WHERE business_id=? AND member_mobile IN ("+String.join(",",Collections.nCopies(phones.size(),"?"))+") AND deleted=0",Long.class,args.toArray()));
        return result;
    }
}

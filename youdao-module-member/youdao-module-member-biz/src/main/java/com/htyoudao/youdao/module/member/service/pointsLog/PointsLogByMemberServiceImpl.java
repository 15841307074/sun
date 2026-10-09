package com.htyoudao.youdao.module.member.service.pointsLog;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.enums.PointsTypeEnum;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsLogVO;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.WxPointsLogDTO;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.ImageDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import com.htyoudao.youdao.module.member.dal.mysql.pointsProduct.PointsProductMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.util.StringUtils;
import com.htyoudao.youdao.module.member.util.enums.SendStatusEnum;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GoodCouponVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UserCouponVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateUtil.isSameDay;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertStringToListS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;


/**
 * 积分记录Service业务层处理
 *
 * @author Qizhongnan
 * @date 2024-02-03
 */
@Service
@DS(DsNameConstants.SHARDING)
public class PointsLogByMemberServiceImpl extends ServiceImpl<PointsLogMapper, PointsLogDO> implements IPointsLogByMemberService {
    private static final Logger log = LoggerFactory.getLogger(PointsLogByMemberServiceImpl.class);
    @Resource
    private PointsLogMapper pointsLogMapper;

    @Override
    public List<PointsLogDO> selectPointsLogList(Long memberId) {
        return pointsLogMapper.selectList(new LambdaQueryWrapper<PointsLogDO>()
                .eq(PointsLogDO::getMemberId, memberId)
                .eq(PointsLogDO::getShardingValue, Integer.parseInt(String.valueOf(memberId % 10))));
    }
}

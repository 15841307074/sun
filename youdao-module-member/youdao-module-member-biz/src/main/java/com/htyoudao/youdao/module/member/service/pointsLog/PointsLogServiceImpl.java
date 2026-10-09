package com.htyoudao.youdao.module.member.service.pointsLog;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.enums.PointsTypeEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsExchangeDetailReqVO;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsLogVO;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.WxPointsLogDTO;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.ImageDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductAttachmentVO;
import com.htyoudao.youdao.module.member.service.pointsProduct.PointsProductAttachmentUtils;
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
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.xxl.job.core.context.XxlJobHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateUtil.isSameDay;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.member.api.enums.LogRecordConstants.*;


/**
 * 积分记录Service业务层处理
 *
 * @author Qizhongnan
 * @date 2024-02-03
 */
@Service
@Component("pointsTask")
@DS(DsNameConstants.SHARDING)
public class PointsLogServiceImpl extends ServiceImpl<PointsLogMapper, PointsLogDO> implements IPointsLogService {
    private static final Logger log = LoggerFactory.getLogger(PointsLogServiceImpl.class);
    @Resource
    private PointsLogMapper pointsLogMapper;

    @Resource
    private IPointsLogService pointsLogService;

    @Resource
    private PointsProductMapper pointsProductMapper;

    @Resource
    private WxMemberMapper wxMemberMapper;

    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @Resource
    private WxMemberService wxMemberService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @DubboReference
    private UserCouponApi userCouponApi;
    @Resource
    private IPointsLogByMemberService iPointsLogByMemberService;

    @Resource
    private ExcelActionService<PointsLogExportReqVo> exportReqVOExcelActionService;


    /**
     * 查询积分记录列表分页
     *
     * @param
     * @return 积分记录
     */
//    @DS(DsNameConstants.SHARDING)
//    @Override
//    public PageResult<PointsLogDO> selectPointsLogListPage(long pageNum, long pageSize, PointsLogPageListReqVo pageListReqVo) {
//        if(pageNum > 1000){
//            throw exception(POINTS_PAGE_1000_NOT_E);
//        }
//        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();
//
//        if (pageListReqVo != null) {
//            if (!org.springframework.util.StringUtils.isEmpty(pageListReqVo.getPointsType())) {
//                queryWrapper.eq(PointsLogDO::getPointsType, pageListReqVo.getPointsType());
//            }
//            if (!StringUtils.isEmpty(pageListReqVo.getOrderSn())) {
////                String orderParam = pageListReqVo.getOrderSn() + "%";
//                queryWrapper.like(PointsLogDO::getOrderSn, pageListReqVo.getOrderSn());
//            }
//
//        }
//        queryWrapper.orderByDesc(PointsLogDO::getCreateTime);
//        Page<PointsLogDO> page = new Page<>(pageNum, pageSize);
//
//        Page<PointsLogDO> list = this.page(page, queryWrapper);
//        for (PointsLogDO log : list.getRecords()) {
//            if (log.getProductId() != null) {
//                LambdaQueryWrapper<PointsProductDO> wrapper = new LambdaQueryWrapper<>();
//                wrapper.eq(PointsProductDO::getProductId, log.getProductId());
//                PointsProductDO pointsProductDO = pointsProductMapper.selectOne(wrapper);
//                log.setPointsProductDO(pointsProductDO);
//                if (Objects.nonNull(log.getExpirationTime()) && new Date().compareTo(log.getExpirationTime()) > 0) {
//                    log.setPointsType(PointsTypeEnum.EXPIRE.getCode());
//                    log.setPointsChange(log.getPointsChange());
//                }
//            }
//        }
//
//
//        PageResult<PointsLogDO> pageResult = new PageResult<>();
//        pageResult.setList(list.getRecords());
//        pageResult.setTotal(list.getTotal());
//        return pageResult;
//    }


    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<PointsLogDO> selectPointsLogListPage(long pageNum, long pageSize,
                                                           PointsLogPageListReqVo pageListReqVo) {
        // 分页限制
        if(pageNum > 1000){
            throw exception(POINTS_PAGE_1000_NOT_E);
        }



        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();

        if (ObjectUtil.isNotEmpty(pageListReqVo.getStartTime()) && ObjectUtil.isNotEmpty(pageListReqVo.getEndTime())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(pageListReqVo.getStartTime(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(pageListReqVo.getEndTime(), formatter);
            queryWrapper.ge(PointsLogDO::getCreateTime, startDate);
            queryWrapper.le(PointsLogDO::getCreateTime, endDate);
        } else {
            // 如果没有传入开始时间，默认查询最近半年数据
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            queryWrapper.ge(PointsLogDO::getCreateTime, sixMonthsAgo);
        }



        if (pageListReqVo != null) {
            // 积分类型查询
            if (!ObjectUtil.isEmpty(pageListReqVo.getPointsType())) {
                queryWrapper.eq(PointsLogDO::getPointsType, pageListReqVo.getPointsType());
            }

            // 2. 优化订单号查询 - 使用精确匹配或右模糊查询
            if (!StringUtils.isEmpty(pageListReqVo.getOrderSn())) {
                // 方案1: 如果订单号是完整格式，使用等值查询（最优）
                // queryWrapper.eq(PointsLogDO::getOrderSn, orderSn);

                // 方案2: 如果必须模糊查询，使用右模糊（'value%'）可以走索引
                queryWrapper.eq(PointsLogDO::getOrderSn, pageListReqVo.getOrderSn());

                // 方案3: 如果数据量大，建议添加订单号长度判断
                // if (orderSn.length() >= 8) { // 根据业务设置合适的最小长度
                //     queryWrapper.likeRight(PointsLogDO::getOrderSn, orderSn);
                // } else {
                //     // 长度太短时不查询或抛出提示
                //     throw exception(ORDER_SN_TOO_SHORT);
                // }
            }
        }

        // 按时间倒序
        queryWrapper.orderByDesc(PointsLogDO::getCreateTime);

        // 3. 优化分页查询
        Page<PointsLogDO> page = new Page<>(pageNum, pageSize);
        Page<PointsLogDO> list = this.page(page, queryWrapper);

        // 4. 批量查询关联的产品信息，避免N+1查询
        Map<Long, PointsProductDO> productMap = batchQueryProductInfo(list.getRecords());

        // 5. 处理过期状态和设置产品信息
        handlePointsLogs(list.getRecords(), productMap);

        // 返回结果
        return new PageResult<>(list.getRecords(), list.getTotal());
    }

    /**
     * 批量查询产品信息
     */
    private Map<Long, PointsProductDO> batchQueryProductInfo(List<PointsLogDO> logs) {
        // 收集所有产品ID
        List<Long> productIds = logs.stream()
                .filter(log -> log.getProductId() != null)
                .map(PointsLogDO::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyMap();
        }

        // 批量查询
        LambdaQueryWrapper<PointsProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PointsProductDO::getProductId, productIds);
        List<PointsProductDO> products = pointsProductMapper.selectList(wrapper);

        // 转换为Map
        return products.stream()
                .collect(Collectors.toMap(PointsProductDO::getProductId, p -> p));
    }

    /**
     * 处理积分记录
     */
    private void handlePointsLogs(List<PointsLogDO> logs, Map<Long, PointsProductDO> productMap) {
        Date now = new Date();

        for (PointsLogDO log : logs) {
            // 设置产品信息
            if (log.getProductId() != null) {
                PointsProductDO product = productMap.get(log.getProductId());
                log.setPointsProductDO(product);
            }

            // 检查是否过期
            if (Objects.nonNull(log.getExpirationTime()) && now.compareTo(log.getExpirationTime()) > 0) {
                log.setPointsType(PointsTypeEnum.EXPIRE.getCode());
                // 注意：这里不应该重新设置 pointsChange，除非有特殊逻辑
                // log.setPointsChange(log.getPointsChange());
            }
        }
    }


    public void makeShardingValueForWx(WxMemberDO wxMember) {

        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
        }
        if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
            int number = 0;
            char letter = wxMember.getOpenid().charAt(wxMember.getOpenid().length() - 1);
            if (StringUtils.isLetter(letter)) {
                number = StringUtils.convertLetterToNumberUsingASCII(letter);
            }
            if (StringUtils.isNumber(letter)) {
                number = Integer.parseInt(String.valueOf(letter));
            }
            wxMember.setShardingValue(number % 10);
        }
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public PageResult<PointsLogDO> selectExchangeListPage(long pageNum, long pageSize, PointsLogExchangeListReqVo exchangeListReqVo) {
        if (pageNum > 1000) {
            throw exception(POINTS_PAGE_1000_NOT_E);
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        QueryWrapper<PointsLogDO> queryWrapper = new QueryWrapper<PointsLogDO>();
        queryWrapper.orderByDesc("create_time");
        if (businessId != null) {
            queryWrapper.eq("business_id", businessId);
        }
        if (exchangeListReqVo != null) {

            if (!org.springframework.util.StringUtils.isEmpty(exchangeListReqVo.getPointsType())) {
                queryWrapper.eq("points_type", exchangeListReqVo.getPointsType());
            }
            if (!org.springframework.util.StringUtils.isEmpty(exchangeListReqVo.getProductType())) {
                queryWrapper.eq("product_type", exchangeListReqVo.getProductType());
            }
            if (!StringUtils.isEmpty(exchangeListReqVo.getOrderSn())) {
                queryWrapper.eq("order_sn", exchangeListReqVo.getOrderSn());
            }
            if (!StringUtils.isEmpty(exchangeListReqVo.getIsNumber())) {
                // 0：查询物流单号为空的数据；1：查询物流单号不为空的数据
                if ("0".equals(exchangeListReqVo.getIsNumber())) {
                    queryWrapper.isNull("tracking_number");
                } else if ("1".equals(exchangeListReqVo.getIsNumber())) {
                    queryWrapper.isNotNull("tracking_number");
                }
            }
            if (!StringUtils.isEmpty(exchangeListReqVo.getMemberMobile())) {
                queryWrapper.eq("member_mobile", exchangeListReqVo.getMemberMobile());
            }
            if (!StringUtils.isEmpty(exchangeListReqVo.getTrackingNumber())) {
                queryWrapper.eq("tracking_number", exchangeListReqVo.getTrackingNumber());
            }
            if (!StringUtils.isEmpty(exchangeListReqVo.getLogCode())) {
                queryWrapper.eq("log_code", exchangeListReqVo.getLogCode());
            }
            // 添加开始结束日期查询条件
            if (ObjectUtil.isNotEmpty(exchangeListReqVo.getStartTime()) && ObjectUtil.isNotEmpty(exchangeListReqVo.getEndTime())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDate = LocalDateTime.parse(exchangeListReqVo.getStartTime(), formatter);
                LocalDateTime endDate = LocalDateTime.parse(exchangeListReqVo.getEndTime(), formatter);
                queryWrapper.ge("create_time", startDate);
                queryWrapper.le("create_time", endDate);
            }
        }

        queryWrapper.in("points_type", Arrays.asList(2,3));

        // 3. 分页查询
        Page<PointsLogDO> page = new Page<>(pageNum, pageSize);
        Page<PointsLogDO> list = this.page(page, queryWrapper);
        List<PointsProductDO> pointsProductDOS = pointsProductMapper.selectPointsProductList(new PointsProductDO());
        if (!pointsProductDOS.isEmpty()) {
            Map<Long, PointsProductDO> idToProductMap = pointsProductDOS.stream().collect(Collectors.toMap(PointsProductDO::getProductId, pointsProduct -> pointsProduct));
            list.getRecords().forEach(pointsLog1 -> pointsLog1.setPointsProductDO(idToProductMap.get(pointsLog1.getProductId())));
        }
        PageResult<PointsLogDO> pageResult = new PageResult<>();

        pageResult.setList(list.getRecords());
        pageResult.setTotal(list.getTotal());
        return pageResult;
    }



    /**
     * 新增积分记录
     *
     * @param
     * @return 结果
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    public int insertPointsLog(PointsLogSaveReqVO saveReqVO) {
        PointsLogDO pointsLogDO = new PointsLogDO();
        BeanUtils.copyProperties(saveReqVO, pointsLogDO);
        Number number = identifierGenerator.nextId(null);
        long l = number.longValue();
        pointsLogDO.setPointsLogId(l);
        if (ObjectUtil.isNotEmpty(pointsLogDO.getShardingValue())) {
            String pointsLogId = l + "" + pointsLogDO.getShardingValue();
            pointsLogDO.setPointsLogId(Long.parseLong(pointsLogId.substring(1)));
        }
        this.save(pointsLogDO);
        return 0;
    }

    @DS(DsNameConstants.SHARDING)
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean addMemberPoint(ClientAddMemberPointReqVO reqVO) {
        if(reqVO.getMemberId()!=null&&reqVO.getMemberId().equals(2121473L)){
            return false;
        }
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        //新建一个积分记录对象
        long sum = 0L;
        if (reqVO.getOrderAmount().compareTo(BigDecimal.ONE) < 0) {
            return false;
        }

        List<PointsLogDO> pointsLogDOList = pointsLogMapper.selectList(new LambdaQueryWrapper<PointsLogDO>().eq(PointsLogDO::getMemberId, reqVO.getMemberId())
                .eq(PointsLogDO::getShardingValue, Integer.parseInt(String.valueOf(reqVO.getMemberId() % 10)))
                .between(PointsLogDO::getCreateTime, getStartOfDay(currentDate), getEndOfDay(currentDate)));

        if (ObjectUtil.isNotEmpty(pointsLogDOList)) {
            sum = pointsLogDOList.stream()
                    .mapToLong(PointsLogDO::getPointsChange)
                    .sum();
            if (sum >= 100) {
                return false;
            }

        }
        PointsLogDO pointsLogDO = new PointsLogDO();
        //在新建的积分记录对象里插入用户 id
        pointsLogDO.setMemberId(reqVO.getMemberId());
        //在新建的积分记录对象里插入订单编号
        pointsLogDO.setOrderSn(reqVO.getOrderSn());
        //在积分记录对象里插入刚才订单的实际金额
        pointsLogDO.setOrderAmount(reqVO.getPayAmount());
        //在积分对象记录里插入是否是因为订单加积分
        pointsLogDO.setIsPointsProduct(2);
        if(!org.springframework.util.StringUtils.isEmpty(reqVO.getBusinessId())){
            pointsLogDO.setBusinessId(reqVO.getBusinessId());
        }

        //通过用户 id 查询用户对象
        WxMemberDO wxMember = new WxMemberDO();
        wxMember.setMemberId(reqVO.getMemberId());
        //获取到当前订单的用户 id
        makeShardingValueForWx(wxMember);
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<WxMemberDO>();
        queryWrapper.eq("member_id", wxMember.getMemberId());
        queryWrapper.eq("sharding_value", wxMember.getShardingValue());
        //用用户 id 查询到当前用户的对象
        wxMember = wxMemberMapper.selectOne(queryWrapper);

        //如果用户是非会员，不产生积分记录
        if (ObjectUtils.isEmpty(wxMember) || wxMember.getUserIdentity() == 0) {
            return false;
        }
        //获取用户生日
        Date memberBirthday = wxMember.getMemberBirthday();
        //获取用户总积分
        Long integralFrozen = wxMember.getIntegralFrozen();
        Long changePoint = 0L;
        if (memberBirthday != null) {
            //计算出用户这次应该加的积分
            changePoint = calculatePointsForUser(reqVO.getPayAmount(), memberBirthday, integralFrozen);
        } else {
            changePoint = calculatePointsForUserWithOutMemberBirthday(reqVO.getPayAmount(), integralFrozen,reqVO.getBusinessId());
        }
        if (changePoint > 100) {
            changePoint = 100L;
        }
        if (sum + changePoint > 100) {
            changePoint = 100L - sum;
        }
        //记录用户可用积分
        // wxMember.setMemberIntegral((int) (wxMember.getMemberIntegral() + changePoint));
        //修改用户对象的总积分
        //wxMember.setIntegralFrozen(wxMember.getIntegralFrozen() + changePoint);
        //记录用户冻结积分
        wxMember.setFreezePoints(wxMember.getFreezePoints() + changePoint);
        //在积分记录对象里放入这次新增的积分
        pointsLogDO.setPointsChange(changePoint);
        pointsLogDO.setMemberName(wxMember.getMemberName());
        pointsLogDO.setMemberNickName(wxMember.getMemberNickName());
        pointsLogDO.setMemberMobile(wxMember.getMemberMobile());
        //修改成功用户对象
        wxMemberMapper.update(wxMember, queryWrapper);
        //在积分记录对象里插入生成的
        pointsLogDO.setLogCode(generateNumber());
        long l = identifierGenerator.nextId(null).longValue();
        pointsLogDO.setPointsLogId(l);
        pointsLogDO.setPointsType(4);
        pointsLogDO.setDeleted(false);
        pointsLogDO.setCreateTime(LocalDateTime.now());
        pointsLogDO.setPointsLogStatus(1);
        pointsLogDO.setShardingValue(wxMember.getShardingValue());
        if (ObjectUtil.isNotEmpty(pointsLogDO.getShardingValue())) {
            String pointsLogId = l + "" + pointsLogDO.getShardingValue();
            pointsLogDO.setPointsLogId(Long.parseLong(pointsLogId.substring(2)));
            pointsLogMapper.insert(pointsLogDO);
        }
        return true;
//        /**时间轮冻结积分任务*/
//        this.addMemberPointTask(wxMember, changePoint);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public WxPointsLogAndAllPointsDTO getMemberAllPoints(PointsLogVO pointsLogVO) {
        WxPointsLogAndAllPointsDTO wxPointsLogAndAllPointsDTO = new WxPointsLogAndAllPointsDTO();
        Long memberId = pointsLogVO.getMemberId();
        if(ObjectUtil.isEmpty(pointsLogVO.getMemberId())){
            throw exception(WX_MEMBER_ID_NOT_EXISTS);
        }
        int parseInt = Integer.parseInt(String.valueOf(pointsLogVO.getMemberId() % 10));
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<WxMemberDO>();
        queryWrapper.eq("sharding_value", parseInt);
        queryWrapper.eq("member_id", memberId);
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(queryWrapper);
        if (wxMemberDO != null) {
            Integer memberIntegral = wxMemberDO.getMemberIntegral();
            //即将过期积分
            Long overduePoints = wxMemberDO.getOverduePoints();
            //冻结积分
            Long freezePoints = wxMemberDO.getFreezePoints();
            if (ObjectUtil.isNotEmpty(memberIntegral)) {
                wxPointsLogAndAllPointsDTO.setAllPoints(memberIntegral != null ? memberIntegral.longValue() : 0L);
            } else {
                wxPointsLogAndAllPointsDTO.setAllPoints(0L);
            }

            if (ObjectUtil.isNotEmpty(overduePoints)) {
                wxPointsLogAndAllPointsDTO.setOverduePoints(overduePoints != null ? overduePoints : 0L);
            } else {
                wxPointsLogAndAllPointsDTO.setOverduePoints(0L);
            }

            if (ObjectUtil.isNotEmpty(freezePoints)) {
                wxPointsLogAndAllPointsDTO.setFreezePoints(freezePoints != null ? freezePoints : 0L);
            } else {
                wxPointsLogAndAllPointsDTO.setFreezePoints(0L);
            }
        }

        return wxPointsLogAndAllPointsDTO;
    }

    //计算
//    public void makeShardingValueForWx(WxMemberDO wxMember) {
//
//        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
//            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
//        }
//        if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
//            int number = 0;
//            char letter = wxMember.getOpenid().charAt(wxMember.getOpenid().length() - 1);
//            if (StringUtils.isLetter(letter)) {
//                number = StringUtils.convertLetterToNumberUsingASCII(letter);
//            }
//            if (StringUtils.isNumber(letter)) {
//                number = Integer.parseInt(String.valueOf(letter));
//            }
//            wxMember.setShardingValue(number % 10);
//        }
////        if(ObjectUtil.isNotEmpty(wxMember.getMemberMobile())){
////
////        }
//    }
    public static String generateNumber() {
        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();

        // 使用当前时间的毫秒数作为编号
        return String.valueOf(currentTimeMillis);
    }

    public Date getStartOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date getEndOfDay(LocalDate date) {
        return Date.from(date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    }


    public Long calculatePointsForUser(BigDecimal orderAmount, Date memberBirthday, Long integralFrozen) {

        //WxMemberCard wxMemberCard  = wxMemberCardService.selectWxMemberCardByMemberLevel(allPoint);
        //通过冻结积分查询出用户对应的会员卡面信息
//        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<WxMemberCardDO>();
//        queryWrapper.gt("max_points_threshold", integralFrozen);
//        queryWrapper.lt("min_points_threshold", integralFrozen);
//        queryWrapper.eq("card_status", 1);

        // 根据订单金额取整加积分
        long points = orderAmount.setScale(0, BigDecimal.ROUND_DOWN).longValue();

//        WxMemberCardDO wxMemberCard = wxMemberCardMapper.selectOne(queryWrapper);

        /*if (ObjectUtils.isEmpty(wxMemberCard)) {
            return points;
        }*/

//        //得到该会员的会员卡日
//        Date memberDay = wxMemberCard.getMemberDay();
//        //得到会员等级
//        Integer memberLevel = wxMemberCard.getMemberLevel();

        // 根据生日加倍积分
        double multiplier = calculateMultiplier(memberBirthday);
        return (long) (points * multiplier);
    }

    private Long calculatePointsForUserWithOutMemberBirthday(BigDecimal orderAmount, Long integralFrozen,Long buss) {

        // 根据订单金额取整加积分
        long points = orderAmount.setScale(0, BigDecimal.ROUND_DOWN).longValue();

        //通过冻结积分查询出用户对应的会员卡面信息
        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<WxMemberCardDO>();
        queryWrapper.gt("max_points_threshold", integralFrozen);
        queryWrapper.lt("min_points_threshold", integralFrozen);
        queryWrapper.eq("card_status", 1);
        if(ObjectUtil.isNotEmpty(buss)){
            queryWrapper.eq("business_id", buss);
        }
        WxMemberCardDO wxMemberCard = wxMemberCardMapper.selectOne(queryWrapper);

        if (ObjectUtils.isEmpty(wxMemberCard)) {
            return points;
        }
        Integer memberLevel = wxMemberCard.getMemberLevel();
        //得到会员卡日
        Date memberDay = wxMemberCard.getMemberDay();

        // 根据会员等级和特殊日期加倍积分
        double multiplier = calculateMultiplierWithOutMemberBirthDay(memberLevel, memberDay);
        return (long) (points * multiplier);
    }

    private double calculateMultiplierWithOutMemberBirthDay(Integer memberLevel, Date memberDay) {
        double multiplier = 1.0;
//        LocalDate today = LocalDate.now();
//        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
//            System.out.println("今天是周一");
//        }

        boolean isMemberDay = isSameDay(new Date(), memberDay);
        switch (memberLevel) {
            case 1:
                multiplier = 1.0;
                break;
            case 2:
                if (isMemberDay) {
                    multiplier = 1.5;
                }
                break;
            case 3:
                if (isMemberDay) {
                    multiplier = 1.5;
                }
                break;
            case 4:
                if (isMemberDay) {
                    multiplier = 2.0;
                }
                break;
            case 5:
                if (isMemberDay) {
                    multiplier = 2.0;
                }
                break;
        }
        return multiplier;
    }

    //计算积分加倍系数
    private static double calculateMultiplier(Date memberBirthday) {
        double multiplier = 1.0;
        boolean isBirthday = isSameDay(new Date(), memberBirthday);
        if (isBirthday){
            multiplier = 2;
        }
        return multiplier;
    }



    @Override
    public List<PointsLogDO> selectPointsLogList(Long memberId) {
        return pointsLogMapper.selectList(new LambdaQueryWrapper<PointsLogDO>()
                .eq(PointsLogDO::getMemberId, memberId)
                .eq(PointsLogDO::getShardingValue, Integer.parseInt(String.valueOf(memberId % 10))));
    }




    /**
     * 修改积分记录
     *
     * @return 结果
     */
    @Override
    @LogRecord(type = MEMBER_POINTS_TYPE, subType = MEMBER_POINTS_UPDATE_TYPE, bizNo = "{{#points.pointsLogId}}", success = MEMBER_POINTS_UPDATE_SUCCESS)
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public int updatePointsLog(PointsLogEditReqVO pointsLogVO) {
        PointsLogDO pointsLogDO = new PointsLogDO();
        pointsLogDO.setPointsLogId(pointsLogVO.getPointsLogId());
        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsLogDO::getPointsLogId, pointsLogVO.getPointsLogId());
        queryWrapper.eq(PointsLogDO::getShardingValue, Integer.parseInt(String.valueOf(pointsLogVO.getMemberId() % 10)));
        pointsLogDO = pointsLogMapper.selectOne(queryWrapper);

        pointsLogDO.setTrackingNumber(pointsLogVO.getTrackingNumber());
        pointsLogDO.setExpressCompany(pointsLogVO.getExpressCompany());
        pointsLogDO.setUpdateTime(LocalDateTime.now());
        LogRecordContext.putVariable("points", pointsLogDO);

        return pointsLogMapper.update(pointsLogDO, queryWrapper);
    }


    @Override
    @DS(DsNameConstants.SHARDING)
    public void productInsert(PointsLogEditReqVO pointsLogVO) {

        PointsLogDO pointsLog = new PointsLogDO();
        pointsLog.setMemberId(pointsLogVO.getMemberId());
        WxMemberDO wxMember = new WxMemberDO();
        wxMember.setMemberId(pointsLogVO.getMemberId());
        //获取到当前订单的用户 id
        this.makeShardingValueForWx(wxMember);
        //用用户 id 查询到当前用户的对象
        wxMember = wxMemberMapper.selectOne(new LambdaQueryWrapperX<WxMemberDO>()
                .eq(WxMemberDO::getShardingValue, wxMember.getShardingValue())
                .eq(WxMemberDO::getMemberId, wxMember.getMemberId()));
        pointsLog.setMemberMobile(pointsLogVO.getMemberMobile());
        pointsLog.setProductId(pointsLogVO.getProductId());
        pointsLog.setMemberName(wxMember.getMemberName());
        pointsLog.setMemberNickName(pointsLogVO.getMemberNickName());
        PointsProductDO pointsProduct = pointsProductMapper.selectOne(new LambdaQueryWrapperX<PointsProductDO>()
                .eq(PointsProductDO::getProductId, pointsLogVO.getProductId()));
        if (wxMember.getMemberIntegral() < pointsProduct.getProductPrice()) {
            throw exception(POINTS_PRODUCT_NOT_E);
        }
        if (ObjectUtil.isEmpty(pointsProduct.getProductInventory()) || pointsProduct.getProductInventory() <= 0) {
            throw exception(POINTS_PRODUCT_NOT_INVENTORY);
        }

        //优惠券
        if (pointsProduct.getProductType() == 1) {
            String couponCode = pointsProduct.getCouponCode();
            RLock productInsertForCouponLock = redissonClient.getLock("productInsertForCouponLock" + couponCode);
            try {

                productInsertForCouponLock.lock();

                GoodCouponVO coupon = userCouponApi.selectCouponByCode(couponCode);
                if (ObjectUtil.isEmpty(coupon)) {
                    throw exception(POINTS_PRODUCT_NOT_COUPON);
                }


                Integer receivedNum = coupon.getReceivedNum();
                if (ObjectUtil.isEmpty(receivedNum)) {
                    receivedNum = 0;
                }

                coupon.setReceivedNum(receivedNum + 1);
                coupon.setCouponNum(coupon.getCouponNum() - 1);
                UserCouponVO userCoupon = new UserCouponVO();
                BeanUtils.copyProperties(coupon, userCoupon);
                userCoupon.setCouponUseTime(coupon.getUseTime());
                userCoupon.setId(null);
                userCoupon.setUserId(pointsLogVO.getMemberId());
                userCoupon.setCouponCode(couponCode);
                userCoupon.setCouponStatus(coupon.getCouponStatus());

                //购买优惠券
                userCoupon.setCouponId(coupon.getId());
                userCoupon.setIsUsed(0);
                userCoupon.setCouponId(userCoupon.getCouponId());
                parseUserCouponTime(coupon, userCoupon);
                userCouponApi.insertCouponByPoints(userCoupon);
                //  goodCouponService.updateGoodCoupon(coupon);

            } catch (BeansException e) {
                e.printStackTrace();
            } finally {
                if (ObjectUtil.isNotNull(productInsertForCouponLock) && productInsertForCouponLock.isLocked() && productInsertForCouponLock.isHeldByCurrentThread()) {
                    productInsertForCouponLock.unlock();
                }
                log.info("积分购买优惠券解锁 成功--->当前线程--->" + Thread.currentThread().getId());
            }
        }

        pointsLog.setIsPointsProduct(1);
        pointsLog.setProductName(pointsProduct.getProductName());
        pointsLog.setProductPrice(pointsProduct.getProductPrice());
        pointsLog.setPointsChange(-pointsProduct.getProductPrice());
        pointsLog.setOrderAmount(new BigDecimal(pointsProduct.getProductPrice()));
        wxMemberMapper.update(wxMember, new LambdaQueryWrapperX<WxMemberDO>().eq(WxMemberDO::getShardingValue, wxMember.getShardingValue()).eq(WxMemberDO::getMemberId, wxMember.getMemberId()));
        pointsLog.setLogCode(generateNumber());

        //更新库存
        int productInventory = pointsProduct.getProductInventory();
        productInventory--;
        pointsProduct.setProductInventory(productInventory);
        pointsProductMapper.updatePointsProduct(pointsProduct);

        wxMember.setMemberIntegral((int) (wxMember.getMemberIntegral() - pointsProduct.getProductPrice()));

        wxMemberMapper.update(wxMember, new LambdaQueryWrapperX<WxMemberDO>().eq(WxMemberDO::getShardingValue, wxMember.getShardingValue()).eq(WxMemberDO::getMemberId, wxMember.getMemberId()));
        pointsLog.setLogCode(generateNumber());
        pointsLog.setLogCode(generateNumber());
        pointsLog.setProductType(pointsProduct.getProductType());
        pointsLog.setPointsType(PointsTypeEnum.INTEGRAL_CONSUMPTION.getCode());
        pointsLog.setReceiveAddress(pointsLogVO.getReceiveAddress());
        Long l = identifierGenerator.nextId(null).longValue();
        pointsLog.setPointsLogId(l);
        pointsLog.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
        if (ObjectUtil.isNotEmpty(pointsLog.getShardingValue())) {
            String pointsLogId = l + "" + pointsLog.getShardingValue();
            BigInteger finalId = (new BigInteger(pointsLogId)).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLog.setPointsLogId(finalId.longValueExact());
            this.save(pointsLog);
        }
    }

    private void parseUserCouponTime(GoodCouponVO goodCoupon, UserCouponVO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            userCoupon.setExpirationTime(goodCoupon.getCouponEndTime());
            userCoupon.setVaildStartTime(goodCoupon.getCouponStartTime());
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            userCoupon.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), DateUtils.YYYY_MM_DD);
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public WxPointsLogAndAllPointsDTO getListByMemberId(PointsLogVO pointsLogVO) {

        if(ObjectUtil.isEmpty(pointsLogVO.getMemberId())){
            throw exception(WX_MEMBER_ID_NOT_EXISTS);
        }
        WxPointsLogAndAllPointsDTO resultDTO = new WxPointsLogAndAllPointsDTO();
        Long memberId = pointsLogVO.getMemberId();
        resultDTO.setMemberId(memberId);

        // 获取登录用户信息
        String username = SecurityFrameworkUtils.getLoginUsername();
        resultDTO.setMemberName(username);
        resultDTO.setMemberNickName(username);

        // 计算分片值
        int shardingValue = (int) (memberId % 10);

        // 定义自定义线程池，避免使用公共线程池
        ExecutorService executor = getCustomExecutor();

        try {
            // 并行查询积分日志和会员信息
            CompletableFuture<List<PointsLogDO>> pointsLogFuture = CompletableFuture.supplyAsync(() -> {
                return iPointsLogByMemberService.selectPointsLogList(memberId);
            }, executor);

            CompletableFuture<WxMemberDO> memberFuture = CompletableFuture.supplyAsync(() -> {
                return wxMemberService.getWxMember(memberId);
            }, executor);

            // 等待所有查询完成，添加超时控制
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(pointsLogFuture, memberFuture);
            try {
                allFuture.get(5, TimeUnit.SECONDS); // 设置5秒超时
            } catch (TimeoutException e) {
                // 处理超时，取消任务
                log.warn("查询操作超时，正在取消任务", e);
                pointsLogFuture.cancel(true);
                memberFuture.cancel(true);
                throw new ServiceTimeoutException("查询操作超时");
            }

            // 获取会员总积分
            WxMemberDO wxMemberDO = memberFuture.get();
            resultDTO.setAllPoints(wxMemberDO != null ? wxMemberDO.getMemberIntegral().longValue() : 0L);

            // 处理积分日志列表
            List<PointsLogDO> pointsLogs = pointsLogFuture.get();
            List<WxPointsLogDTO> logDTOList = new ArrayList<>(pointsLogs.size());

            // 使用 Map 缓存积分类型描述，避免重复计算
            Map<Integer, String> pointsTypeMap = new HashMap<>();
            pointsTypeMap.put(1, "新增积分");
            pointsTypeMap.put(2, "消耗积分");
            pointsTypeMap.put(3, "过期积分");
            pointsTypeMap.put(4, "冻结积分");

            long totalPoints = 0;
            for (PointsLogDO log : pointsLogs) {
                WxPointsLogDTO logDTO = new WxPointsLogDTO();

                // 时间转换优化
                if (log.getCreateTime() != null) {
                    logDTO.setPointLogCreateTime(Date.from(log.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
                }

                logDTO.setMemberId(log.getMemberId());
                logDTO.setPointPrice(log.getPointsChange());
                logDTO.setLogCode(log.getLogCode());

                // 优化积分类型处理
                Integer pointsType = log.getPointsType();
                if (pointsType != null) {
                    logDTO.setPointChangeValue(pointsTypeMap.getOrDefault(pointsType, "未知类型"));

                    // 优化积分计算逻辑
                    if (pointsType == 2) {
                        logDTO.setPointPrice(log.getPointsChange());
                    }
                }

                // 优化订单号处理
                if (StringUtils.hasText(log.getOrderSn())) {
                    logDTO.setPointLogId(log.getPointsLogId());
                    logDTO.setOrderSn(log.getOrderSn());
                } else {
                    logDTO.setPointLogId(log.getPointsLogId());
                }

                // 优化有效积分计算
                if (log.getPointsLogStatus() != null && !log.getPointsLogStatus().equals(3)) {
                    totalPoints += log.getPointsChange();
                }

                logDTOList.add(logDTO);
            }

            // 设置计算的总积分
            resultDTO.setAllPoints(totalPoints);

            // 使用 Stream API 进行排序，代码更简洁
            resultDTO.setWxPointsLogDTOList(logDTOList.stream()
                    .sorted(Comparator.comparing(WxPointsLogDTO::getPointLogCreateTime,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList()));

        } catch (InterruptedException e) {
            // 处理线程中断
            Thread.currentThread().interrupt();
            log.error("操作被中断", e);
            throw new RuntimeException("操作被中断", e);
        } catch (ExecutionException e) {
            // 处理CompletableFuture执行异常
            Throwable cause = e.getCause();
            log.error("查询执行异常", cause);
            throw new RuntimeException("查询执行异常", cause);
        } catch (ServiceTimeoutException e) {
            // 处理自定义超时异常
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 捕获所有其他异常
            log.error("未知异常", e);
            throw new RuntimeException("未知异常", e);
        } finally {
            // 确保线程池被正确关闭
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        log.warn("线程池未及时关闭，强制终止");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("关闭线程池时被中断", e);
                }
            }
        }

        return resultDTO;
    }

    // 获取自定义线程池（示例方法，实际应用中建议使用线程池配置类）
    private ExecutorService getCustomExecutor() {
        return new ThreadPoolExecutor(
                5,
                10,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger threadId = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "points-query-thread-" + threadId.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    // 自定义超时异常类
    class ServiceTimeoutException extends RuntimeException {
        public ServiceTimeoutException(String message) {
            super(message);
        }
    }



    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public List<WxPointsLogDetailDTO> wxGetPointsLogDetailList(PointsLogVO pointsLogVO) {

        if(ObjectUtil.isEmpty(pointsLogVO.getMemberId())){
            throw exception(WX_MEMBER_ID_NOT_EXISTS);
        }
        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<PointsLogDO>();
        queryWrapper.eq(PointsLogDO::getMemberId, pointsLogVO.getMemberId());
        queryWrapper.eq(PointsLogDO::getShardingValue, Integer.parseInt(String.valueOf(pointsLogVO.getMemberId() % 10)));
        queryWrapper.eq(PointsLogDO::getPointsType, 2);
        queryWrapper.eq(pointsLogVO.getPointsLogId() != null, PointsLogDO::getPointsLogId, pointsLogVO.getPointsLogId());
        queryWrapper.eq(StringUtils.isNotBlank(pointsLogVO.getLogCode()), PointsLogDO::getLogCode, pointsLogVO.getLogCode());
        List<PointsLogDO> pointsLogDOS = pointsLogMapper.selectList(queryWrapper);
        // 提取所有需要查询的商品ID
        Set<Long> productIds = pointsLogDOS.stream()
                .map(PointsLogDO::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<WxPointsLogDetailDTO> wxPointsLogDetailDTOList = new ArrayList<>();
        // 批量查询仍存在的商品；商品已删除时继续使用兑换日志中的快照字段返回历史记录。
        Map<Long, PointsProductDO> pointsProductMap = productIds.isEmpty() ? Collections.emptyMap()
                : pointsProductMapper.selectList(new LambdaQueryWrapper<PointsProductDO>()
                        .in(PointsProductDO::getProductId, productIds)).stream()
                .collect(Collectors.toMap(PointsProductDO::getProductId, Function.identity()));

        for (PointsLogDO pointsLog : pointsLogDOS) {
            WxPointsLogDetailDTO detail = new WxPointsLogDetailDTO();
            detail.setPointsLogId(pointsLog.getPointsLogId());
            detail.setLogCode(pointsLog.getLogCode());
            detail.setExchangeRecordNo(pointsLog.getLogCode());
            detail.setExchangeStatus(1);
            detail.setExchangeStatusName("兑换成功");
            detail.setProductName(pointsLog.getProductName());
            detail.setProductPrice(pointsLog.getProductPrice());
            detail.setConsumePoints(pointsLog.getProductPrice());
            detail.setProductQuantity(1);
            detail.setExpressCompany(pointsLog.getExpressCompany());
            detail.setTrackingNumber(pointsLog.getTrackingNumber());
            detail.setReceiveAddress(pointsLog.getReceiveAddress());
            detail.setMemberMobile(pointsLog.getMemberMobile());
            detail.setMemberNickName(pointsLog.getMemberNickName());

            Integer productType = pointsLog.getProductType();
            PointsProductDO product = pointsProductMap.get(pointsLog.getProductId());
            if (product != null) {
                if (!StringUtils.isNotBlank(detail.getProductName())) {
                    detail.setProductName(product.getProductName());
                }
                if (productType == null) {
                    productType = product.getProductType();
                }
                detail.setProductDescription(product.getProductDescription());

                List<PointsProductAttachmentVO> headerAttachments = PointsProductAttachmentUtils.parseAttachments(
                        product.getProductHeaderAttachmentsJson(), product.getProductHeaderImage());
                List<ImageDTO> headerImages = headerAttachments.stream().map(attachment -> {
                    ImageDTO image = new ImageDTO();
                    image.setUrl(attachment.getUrl());
                    image.setType(attachment.getType());
                    return image;
                }).collect(Collectors.toList());
                detail.setProductHeaderImages(headerImages);

                String thumbnailUrl = PointsProductAttachmentUtils.resolveThumbnail(
                        product.getProductThumbnail(), headerAttachments);
                ImageDTO thumbnail = new ImageDTO();
                if (StringUtils.isNotBlank(thumbnailUrl)) {
                    thumbnail.setUrl(thumbnailUrl);
                    thumbnail.setType(2);
                }
                detail.setProductThumbnail(thumbnail);

                List<ImageDTO> detailImages = PointsProductAttachmentUtils.parseAttachments(
                                product.getProductDetailAttachmentsJson(), product.getProductDetailImages()).stream()
                        .map(attachment -> {
                            ImageDTO image = new ImageDTO();
                            image.setUrl(attachment.getUrl());
                            image.setType(attachment.getType());
                            return image;
                        }).collect(Collectors.toList());
                detail.setProductDetailImage(detailImages);
            } else {
                detail.setProductHeaderImages(Collections.emptyList());
                detail.setProductDetailImage(Collections.emptyList());
                detail.setProductThumbnail(new ImageDTO());
            }

            int resolvedProductType = productType == null ? 0 : productType;
            detail.setProductType(resolvedProductType);
            detail.setProductTypeName(resolvedProductType == 1 ? "优惠券"
                    : resolvedProductType == 2 ? "实物" : "");
            if (pointsLog.getCreateTime() != null) {
                Instant instant = pointsLog.getCreateTime().atZone(ZoneId.systemDefault()).toInstant();
                detail.setPointLogCreateTime(Date.from(instant));
            }

            // 优惠券兑换不返回发货状态；实物根据物流信息展示发货进度。
            if (resolvedProductType == 2) {
                detail.setSendStatusName(StringUtils.isEmpty(pointsLog.getTrackingNumber())
                        ? SendStatusEnum.UNSHIPPED.getMsg() : SendStatusEnum.SHIPPED.getMsg());
                if (pointsLog.getUpdateTime() != null
                        && ChronoUnit.DAYS.between(pointsLog.getUpdateTime(), LocalDateTime.now()) >= 10L) {
                    detail.setSendStatusName(SendStatusEnum.RECEIVED.getMsg());
                }
            }
            wxPointsLogDetailDTOList.add(detail);
        }

        wxPointsLogDetailDTOList.sort(Comparator.comparing(WxPointsLogDetailDTO::getPointLogCreateTime,
                Comparator.nullsLast(Date::compareTo)).reversed());

        return wxPointsLogDetailDTOList;
    }

    /**
     * 查询小程序积分商品兑换详情。
     *
     * <p>复用现有兑换记录组装逻辑，确保列表和详情的商品快照、附件及物流状态保持一致。</p>
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    public WxPointsLogDetailDTO getExchangeDetail(PointsExchangeDetailReqVO reqVO) {
        if (reqVO.getPointsLogId() == null && StringUtils.isBlank(reqVO.getLogCode())) {
            throw exception(POINTS_EXCHANGE_DETAIL_ID_REQUIRED);
        }

        PointsLogVO query = new PointsLogVO();
        query.setMemberId(reqVO.getMemberId());
        query.setPointsLogId(reqVO.getPointsLogId());
        query.setLogCode(StringUtils.trim(reqVO.getLogCode()));
        List<WxPointsLogDetailDTO> details = wxGetPointsLogDetailList(query);
        if (details.isEmpty()) {
            throw exception(POINTS_EXCHANGE_RECORD_NOT_EXISTS);
        }
        return details.get(0);
    }




    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    @DataPermission(enable = false)
    @Override
    public void clearExpiredPoints(Long businessId) {
        log.info(">>> 开始清理过期积分 businessID：{}", businessId);
        if (businessId == null) {
            businessId = Long.valueOf(XxlJobHelper.getJobParam());
        }

        int pageSize = 10000;
        int currentPage = 1;
        boolean hasNext = true;

        // 分页查询用户积分记录
        while (hasNext) {
            // 查询用户积分记录
            QueryWrapperX<PointsLogDO> pointsQuery = new QueryWrapperX<>();
            pointsQuery.select("member_id", "points_type", "SUM(points_change) as points_change")
                    .eq("business_id", businessId) // 添加businessId过滤
                    .in("points_type", Arrays.asList(1, 2))
                    .le("create_time", LocalDateTime.now().minusYears(1))
                    .groupBy("member_id","points_type")
                    .last("LIMIT " + (currentPage - 1) * pageSize + ", " + pageSize);

            List<PointsLogDO> pointsLogs = pointsLogMapper.selectList(pointsQuery);

            if (CollectionUtils.isEmpty(pointsLogs)) {
                hasNext = false;
                continue;
            }

            // 构建积分映射
            Map<Long, Long> memberExpiredPointsMap = new HashMap<>();
            Map<Long, Long> memberUsedPointsMap = new HashMap<>();
            pointsLogs.forEach(log -> {
                Long memberId = log.getMemberId();
                // 积分值非负处理（根据业务场景调整）
                if (1 == log.getPointsType()) {
                    // 过期积分：累加同一会员的积分
                    memberExpiredPointsMap.merge(memberId, log.getPointsChange(), Long::sum);
                } else if (2 == log.getPointsType()) {
                    // 使用积分：累加同一会员的积分
                    memberUsedPointsMap.merge(memberId, log.getPointsChange(), Long::sum);
                }
            });
            if (!memberExpiredPointsMap.isEmpty()) {
                // 处理需清理的积分
                List<WxMemberDO> memberList = handleExpiredPoints(memberExpiredPointsMap, memberUsedPointsMap);

                if (!memberList.isEmpty()) {
                    log.info(">>> 开始清理过期积分，批次={}，需清理的积分={}", currentPage, memberList.size());
                    wxMemberService.updatePointBatch(businessId, memberList);
                    log.info(">>> 清理过期积分完成，批次={}", currentPage);
                }
            }

            // 判断是否还有下一页
            if (pointsLogs.size() < pageSize) {
                hasNext = false;
            } else {
                currentPage++;
            }
        }
    }






    private List<WxMemberDO> handleExpiredPoints(Map<Long, Long> memberExpiredPointsMap, Map<Long, Long> memberUsedPointsMap) {
        // 使用getOrDefault简化空值检查
        return memberExpiredPointsMap.entrySet().stream()
                .map(entry -> {
                    Long usedPoints = Optional.ofNullable(memberUsedPointsMap)
                            .map(map -> map.getOrDefault(entry.getKey(), 0L))
                            .orElse(0L);
                    Long actualUsedPoints = Math.abs(usedPoints);

// 3. 计算净积分（过期积分 - 实际使用积分）
                    Long expiredPoints = entry.getValue(); // entry.getValue()是过期积分（正）
                    Long difference = expiredPoints - actualUsedPoints;
                    // 只有积分大于0时才创建会员对象
                    if (difference != null && difference > 0) {
                        WxMemberDO member = new WxMemberDO();
                        member.setMemberId(entry.getKey());
                        member.setMemberIntegral(difference.intValue());
                        return member;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePointsLogWithOverDue(Long businessId) {
        for (int shardingValue = 0; shardingValue < 10;shardingValue++){
                LambdaUpdateWrapper<PointsLogDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(PointsLogDO::getPointsType, 3)
                        .setSql("points_change = -points_change")
                        .eq(PointsLogDO::getBusinessId, businessId) // 添加businessId过滤
                        .eq(PointsLogDO::getPointsType, 1)
                        .eq(PointsLogDO::getShardingValue, shardingValue)
                        .le(PointsLogDO::getCreateTime, LocalDateTime.now().minusYears(1));
                  pointsLogMapper.update(null, updateWrapper);
            }
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void export(PointsLogExportReqVo pointsLogExportReqVo, HttpServletRequest request, HttpServletResponse response) {
        Long businessId = BusinessContextHolder.getBusinessId();


        // 构建查询条件
        LambdaQueryWrapper<PointsLogDO> queryWrapper = buildExchangeWrapper(pointsLogExportReqVo, businessId);

        // 设置分页参数，每页1万条
        int pageSize = 10000;
        Page<PointsLogExportRespVO> pageParam = new Page<>(1, pageSize);


        exportReqVOExcelActionService.exportAsyncExcel(
                PointsLogExportRespVO.class,
                pageParam,
                (Page<PointsLogExportRespVO> param) -> getExchangeData(param, queryWrapper),
                buildExchangeExportFileName(pointsLogExportReqVo)
        );
    }


    private LambdaQueryWrapper<PointsLogDO> buildExchangeWrapper(PointsLogExportReqVo pointsLogExportReqVo, Long businessId) {

        LambdaQueryWrapper<PointsLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PointsLogDO::getCreateTime);

        if (businessId != null) {
            queryWrapper.eq(PointsLogDO::getBusinessId, businessId);
        }

        if (pointsLogExportReqVo != null) {

            if (!org.springframework.util.StringUtils.isEmpty(pointsLogExportReqVo.getPointsType())) {
                queryWrapper.eq(PointsLogDO::getPointsType, pointsLogExportReqVo.getPointsType());
            }
            if (!org.springframework.util.StringUtils.isEmpty(pointsLogExportReqVo.getProductType())) {
                queryWrapper.eq(PointsLogDO::getProductType, pointsLogExportReqVo.getProductType());
            }
            if (!StringUtils.isEmpty(pointsLogExportReqVo.getOrderSn())) {
                queryWrapper.eq(PointsLogDO::getOrderSn, pointsLogExportReqVo.getOrderSn());
            }
            if (!StringUtils.isEmpty(pointsLogExportReqVo.getIsNumber())) {
                // 0：查询物流单号为空的数据；1：查询物流单号不为空的数据
                if ("0".equals(pointsLogExportReqVo.getIsNumber())) {
                    queryWrapper.isNull(PointsLogDO::getTrackingNumber);
                } else if ("1".equals(pointsLogExportReqVo.getIsNumber())) {
                    queryWrapper.isNotNull(PointsLogDO::getTrackingNumber);
                }
            }
            if (!StringUtils.isEmpty(pointsLogExportReqVo.getMemberMobile())) {
                queryWrapper.eq(PointsLogDO::getMemberMobile, pointsLogExportReqVo.getMemberMobile());
            }
            if (!StringUtils.isEmpty(pointsLogExportReqVo.getTrackingNumber())) {
                queryWrapper.eq(PointsLogDO::getTrackingNumber, pointsLogExportReqVo.getTrackingNumber());
            }
            if (!StringUtils.isEmpty(pointsLogExportReqVo.getLogCode())) {
                queryWrapper.eq(PointsLogDO::getLogCode, pointsLogExportReqVo.getLogCode());
            }
            // 添加开始结束日期查询条件
            if (ObjectUtil.isNotEmpty(pointsLogExportReqVo.getStartTime()) && ObjectUtil.isNotEmpty(pointsLogExportReqVo.getEndTime())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDate = LocalDateTime.parse(pointsLogExportReqVo.getStartTime(), formatter);
                LocalDateTime endDate = LocalDateTime.parse(pointsLogExportReqVo.getEndTime(), formatter);
                queryWrapper.ge(PointsLogDO::getCreateTime, startDate);
                queryWrapper.le(PointsLogDO::getCreateTime, endDate);
            } else {
                // 如果没有传入开始时间，默认查询最近半年数据
                LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
                queryWrapper.ge(PointsLogDO::getCreateTime, sixMonthsAgo);
            }
        } else {
            // 如果没有传入查询条件，默认查询最近半年数据
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            queryWrapper.ge(PointsLogDO::getCreateTime, sixMonthsAgo);
        }

        queryWrapper.notIn(PointsLogDO::getPointsType, Arrays.asList(1, 4));
        return queryWrapper;
    }
    @DS(DsNameConstants.SHARDING)
    private List<PointsLogExportRespVO> getExchangeData(Page<PointsLogExportRespVO> pageParam, LambdaQueryWrapper<PointsLogDO> queryWrapper) {
        // 创建DO的分页参数
        Page<PointsLogDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        // 通过Service方法调用，确保@DS注解生效
        Page<PointsLogDO> resultPage = pointsLogService.page(currentPageParam, queryWrapper);
        List<PointsLogDO> logList = resultPage.getRecords();

        // 转换为响应对象
        List<PointsLogExportRespVO> respList = new ArrayList<>(logList.size());
        for (PointsLogDO pointsLogDO : logList) {
            PointsLogExportRespVO pointsLogExportRespVO = new PointsLogExportRespVO();
            BeanUtils.copyProperties(pointsLogDO, pointsLogExportRespVO);
            pointsLogExportRespVO.setPointsLogId(pointsLogDO.getPointsLogId().toString());
            if(ObjectUtil.isNotEmpty(pointsLogDO.getMemberId())){
                pointsLogExportRespVO.setMemberId(pointsLogDO.getMemberId().toString());
            }
            Integer productType = pointsLogDO.getProductType();
            pointsLogExportRespVO.setProductTypeName(convertProductTypeToName(productType));
            Long productId = pointsLogDO.getProductId();
            if(ObjectUtil.isNotEmpty(productId)){
                PointsProductDO pointsProductDO = pointsProductMapper.selectById(productId);
                if(pointsProductDO!=null){
                    pointsLogExportRespVO.setProductPrice(pointsProductDO.getProductPrice());
                    pointsLogExportRespVO.setProductName(pointsProductDO.getProductName());
                }
            }

            respList.add(pointsLogExportRespVO);
        }

        return respList;
    }

    private String buildExchangeExportFileName(PointsLogExportReqVo pointsLogExportReqVo) {
        StringBuilder fileName = new StringBuilder("积分兑换记录");

        // 添加时间戳
        fileName.append("_").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return fileName.toString();
    }
    private String convertProductTypeToName(Integer productType) {
        if (productType == null) {
            return "";
        }
        switch (productType) {
            case 1:
                return "优惠卷";
            case 2:
                return "实物商品";
            case 3:
                return "抽奖商品";
            default:
                return "";
        }
    }



}

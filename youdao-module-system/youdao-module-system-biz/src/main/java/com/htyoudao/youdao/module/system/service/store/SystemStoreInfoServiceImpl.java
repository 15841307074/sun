package com.htyoudao.youdao.module.system.service.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.api.appmap.AppMapApi;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityStoreTagUpdateDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.UpdateCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.promotion.api.seckill.SeckillApi;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.api.store.dto.*;
import com.htyoudao.youdao.module.system.config.GrayStoreConfig;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagGroupRespVO;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValueRespVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.*;
import com.htyoudao.youdao.module.system.controller.app.store.vo.*;
import com.htyoudao.youdao.module.system.controller.app.store.vo.DC.StoreInfoDCRespVo;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.*;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.taggroup.TagGroupDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.businessuser.BusinessUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.*;
import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.taggroup.TagGroupMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.dal.mysql.user.AdminUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomConfigMapper;
import com.htyoudao.youdao.module.system.enums.ChannelType;
import com.htyoudao.youdao.module.system.enums.PrinterTypeEnum;
import com.htyoudao.youdao.module.system.enums.StoreExpensesTypeEnum;
import com.htyoudao.youdao.module.system.enums.StoreStatusEnum;
import com.htyoudao.youdao.module.system.enums.StoreUserEnum;
import com.htyoudao.youdao.module.system.enums.org.StoreUserTypeConstants;
import com.htyoudao.youdao.module.system.enums.wxtemplate.StoreOpenStatusEnum;
import com.htyoudao.youdao.module.system.service.applet.AppletPageManagementService;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheValue;
import com.htyoudao.youdao.module.system.service.storeuser.StoreUserService;
import com.htyoudao.youdao.module.system.service.storebackground.StoreBackgroundCacheService;
import com.htyoudao.youdao.module.system.util.store.CoordinateTransformUtil;
import com.htyoudao.youdao.module.system.util.store.RedisForAppletAd;
import com.htyoudao.youdao.module.system.util.string.PinyinUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.utils.Lists;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

/**
 * 门店 Service 实现类
 *
 * @author 0090
 */
@Service("systemStoreInfoServiceImpl")
@Slf4j
public class SystemStoreInfoServiceImpl implements SystemStoreInfoService {
    @Resource
    private StoreActivityTagCache storeActivityTagCache;
    @Resource
    private StoreActivityTagOutbox storeActivityTagOutbox;


    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private StoreUserMapper storeUserMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private SystemStoreDeliveryScopeMapper systemStoreDeliveryScopeMapper;
    @Resource
    private SystemStoreExpensesMapper systemStoreExpensesMapper;
    @Resource
    private SystemStoreFranchiseeInfoMapper systemStoreFranchiseeInfoMapper;
    @Autowired
    private SystemStoreUserMapper systemStoreUserMapper;
    @Resource
    private SystemStoreTagMapper systemStoreTagMapper;
    @Resource
    private TagValueMapper tagValueMapper;
    @Resource
    private TagGroupMapper tagGroupMapper;
    @Resource
    private ExcelActionService<StoreResVO> storeRespVOExcelActionService;
    @Resource
    private SysStoreExtendService sysStoreExtendService;
    @Resource
    private OrgUserMapper userOrgMapper;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private AdminUserMapper userMapper;
    @Resource
    private StoreWecomConfigMapper storeWecomConfigMapper;
    @Resource
    private StoreCityListCacheService storeCityListCacheService;
    @DubboReference
    private GoodCouponApi goodCouponApi;
    @DubboReference
    private SeckillApi seckillApi;
    @DubboReference
    private ActivityApi activityApi;
    //地球半径
    private static final Double EarthRadius = 6378.137;
    private static final int STATUS_ENABLE = 0;
    private static final int STATUS_DISABLE = 1;
    private static final int STORE_EXPENSE_TYPE_CAMPUS_DELIVERY = StoreExpensesTypeEnum.CAMPUS_DELIVERY.getCode();
    private static final Set<Integer> REQUIRED_STORE_EXPENSE_TYPES = Set.of(
            StoreExpensesTypeEnum.CANTEEN_FOOD.getCode(),
            StoreExpensesTypeEnum.TAKEAWAY_PACKAGE.getCode(),
            StoreExpensesTypeEnum.TAKEAWAY_DELIVERY.getCode());
    @Resource
    private OrgUserService orgUserService;
    @Resource
    @Lazy
    private StoreUserService storeUserService;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private GrayStoreConfig grayStoreConfig;

    private static final String TENCENT_MAP_API_URL = "https://apis.map.qq.com/ws/geocoder/v1/";
    private static final String API_KEY = "CLOUD_SECRET_REQUIRED";

    @Resource
    private BusinessService businessService;
    @Resource
    private SystemStoreStatusLogMapper  systemStoreStatusLogMapper;
    @Resource
    private RedisForAppletAd redisForAppletAd;
    @Resource
    private AppletPageManagementService appletPageManagementService;
    @Resource
    private StoreBackgroundCacheService storeBackgroundCacheService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @DubboReference
    private AppMapApi appMapApi;

    private static final String STORE_BUSINESS_STATUS_CACHE_KEY = "store:business_status:%s";
    private static final DateTimeFormatter STORE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long BOSS_STORE_LIST_SLOW_LOG_MILLIS = 500L;

    @Override
    public List<Long> selectStoreOrgIds(List<Long> storeIds) {
        return systemStoreInfoMapper.selectStoreOrgIds(storeIds);
    }

    @Override
    public PageResult<OrgStorePageRespVO> selectPage(OrgStorePageReqVO pageReqVO) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(SystemStoreInfoDO::getOrgId, 0)
                .eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.like(StringUtils.isNotEmpty(pageReqVO.getStoreName()), SystemStoreInfoDO::getStoreName, pageReqVO.getStoreName());
        PageResult<SystemStoreInfoDO> result = systemStoreInfoMapper.selectPage(pageReqVO, queryWrapper);
        return BeanUtils.toBean(result, OrgStorePageRespVO.class);
    }

    @Override
    public List<SystemStoreInfoDO> getStoreListByOrgId(Long orgId, String storeName) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(SystemStoreInfoDO::getOrgId, orgId)
                .eq(SystemStoreInfoDO::getStoreSource, 0);
        ;
        queryWrapper.like(StringUtils.isNotEmpty(storeName), SystemStoreInfoDO::getStoreName, storeName);
        return systemStoreInfoMapper.selectList(queryWrapper);
    }

    @Override
    public int addStoreForOrg(OrgStoreSaveReqVO orgStoreSaveReqVO) {
        List<Long> storeIds = orgStoreSaveReqVO.getStoreIds();
        Long orgId = orgStoreSaveReqVO.getOrgId();
        LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = Wrappers.lambdaUpdate(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getStoreId, storeIds)
                .set(SystemStoreInfoDO::getOrgId, orgId);
        return systemStoreInfoMapper.update(updateWrapper);
    }

    @Override
    public PageResult<StoreResVO> getStorePage(StorePageReqVO pageReqVO ) {
        normalizeStorePageTagFilter(pageReqVO);
        //查询deptIDs
        Long businessId = BusinessContextHolder.getBusinessId();
        if (businessId.equals(1L)) {
            businessId = null;
        }
        Set<Long> orgIds = new HashSet<>();
        Long userId = WebFrameworkUtils.getLoginUserId();
        //当前用户的机构ID
        if (CollectionUtil.isEmpty(pageReqVO.getOrgIds())) {
            //当前用户的机构ID
            List<Long> orgId = orgUserService.selectUserOrgIds(userId);
            orgIds.addAll(orgId);
            Set<Long> finalOrgIds = new HashSet<>();
            orgId.forEach(orgd -> {
                finalOrgIds.addAll(orgMapper.getChildIdListByStore(orgd).stream().collect(Collectors.toSet()));
            });
            orgIds = orgMapper.getChildIdListByStore(pageReqVO.getOrgId());
            Set<Long> businessIdList = new HashSet<>();
            businessIdList.add(pageReqVO.getOrgId());
            List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
            orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
            orgIds.addAll(finalOrgIds);
            pageReqVO.setOrgIds(orgIds);
        }
        Page<StoreResVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<StoreResVO> pageList = systemStoreInfoMapper.selectPageList(page, pageReqVO, businessId, userId);
        PageResult<StoreResVO> pageResult = new PageResult<>(pageList.getRecords(), pageList.getTotal());
        return joinData(pageResult);
    }

    /**
     * 归一化门店分页的多标签筛选参数，避免重复标签影响“同时满足”数量统计。
     */
    private void normalizeStorePageTagFilter(StorePageReqVO pageReqVO) {
        if (pageReqVO == null || CollectionUtils.isEmpty(pageReqVO.getTagIds())) {
            return;
        }
        pageReqVO.setTagIds(pageReqVO.getTagIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_CREATE_SUB_TYPE, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_CREATE_SUCCESS)
    public Long createStore(StoreSaveReqVO storeUserSaveReqVO) {
        fillCampusDeliveryDefaults(storeUserSaveReqVO);
        validateCampusDeliveryConfig(storeUserSaveReqVO);
        validateTakeawayConfig(storeUserSaveReqVO.getStoreTakeaway(),
                storeUserSaveReqVO.getSystemStoreDeliveryScopeList());
        validateStoreExpensesForCreate(storeUserSaveReqVO);
        if (storeUserSaveReqVO.getStoreStatus().equals(StoreStatusEnum.SHUTDOWN.getStatus()) && storeUserSaveReqVO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus())) {
            throw exception(STORE_STOP_EXISTS);
        }
        if (storeUserSaveReqVO.getStoreTakeaway().equals(StoreStatusEnum.OPEN.getStatus())
                && StringUtils.isEmpty(storeUserSaveReqVO.getDeliveryName()) && StringUtils.isEmpty(storeUserSaveReqVO.getDeliveryPhone()) && CollectionUtil.isEmpty(storeUserSaveReqVO.getSystemStoreDeliveryScopeList())) {
            throw exception(STORE_OPEN_DELIVERY);
        }
        if (StringUtils.isNotEmpty(storeUserSaveReqVO.getMeituanId())){
            List<SystemStoreInfoDO> systemStoreInfoDOList = systemStoreInfoMapper
                    .selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                            .eq(SystemStoreInfoDO::getMeituanId, storeUserSaveReqVO.getMeituanId()));
            if(CollectionUtil.isNotEmpty(systemStoreInfoDOList)){
                throw exception(STORE_MEITUAN_EXISTS);
            }
        }
        if (StringUtils.isNotEmpty(storeUserSaveReqVO.getHungryId())){
            List<SystemStoreInfoDO> systemStoreInfoDOList = systemStoreInfoMapper
                    .selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                            .eq(SystemStoreInfoDO::getHungryId, storeUserSaveReqVO.getHungryId()));
            if(CollectionUtil.isNotEmpty(systemStoreInfoDOList)){
                throw exception(STORE_MEITUAN_EXISTS);
            }
        }
        validateStoreNameUnique(null, storeUserSaveReqVO.getStoreName().trim());
        if(storeUserSaveReqVO.getPeakHours().contains("24:00")){
            storeUserSaveReqVO.setPeakHours(storeUserSaveReqVO.getPeakHours().replaceAll("24:00", "23:59"));
        }
        SystemStoreInfoDO systemStoreInfoDO = BeanUtils.toBean(storeUserSaveReqVO, SystemStoreInfoDO.class);
        systemStoreInfoDO.setBusinessId(BusinessContextHolder.getBusinessId());
        String prrinterTemplate = JsonUtils.toJsonString(new PrintConfigVO());
        systemStoreInfoDO.setPrinterTemplate(prrinterTemplate);
        systemStoreInfoMapper.insert(systemStoreInfoDO);
        saveFranchiseeInfo(systemStoreInfoDO.getStoreId(), storeUserSaveReqVO.getFranchiseeInfo());
        //插入店长关系表
        SystemStoreUserDO systemStoreUserDO = new SystemStoreUserDO();
        systemStoreUserDO.setStoreId(systemStoreInfoDO.getStoreId());
        systemStoreUserDO.setUserId(storeUserSaveReqVO.getUserId());
        systemStoreUserDO.setType(StoreUserEnum.STOREMANAGER.getStatus());
        systemStoreUserDO.setOrgId(storeUserSaveReqVO.getOrgId());
        systemStoreUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
        systemStoreUserDO.setVisible(StoreUserTypeConstants.STORE_USER_VISIBLE);
        systemStoreUserMapper.insert(systemStoreUserDO);
        // 批量标签
        if (!CollectionUtils.isEmpty(storeUserSaveReqVO.getTagIds())) {
            List<SystemStoreTagDO> systemStoreTagDOList = new ArrayList<>();
            storeUserSaveReqVO.getTagIds().forEach(tagId -> {
                SystemStoreTagDO systemStoreTagDO = new SystemStoreTagDO();
                systemStoreTagDO.setStoreId(systemStoreInfoDO.getStoreId());
                systemStoreTagDO.setTagId(tagId);
                systemStoreTagDOList.add(systemStoreTagDO);
            });
            systemStoreTagMapper.insertBatch(systemStoreTagDOList);
        }
        //添加记录
        creatLog(systemStoreInfoDO);
        // 批量插入配送范围和费用信息
        insertBatchData(storeUserSaveReqVO, systemStoreInfoDO.getStoreId(), true);
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(storeUserSaveReqVO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", storeUserSaveReqVO);

        Long storeId = systemStoreInfoDO.getStoreId();

        //创建交换机
        this.createExchange(storeId);
        List<Long> tagIds = storeUserSaveReqVO.getTagIds();
        if(CollectionUtils.isNotEmpty(tagIds)){
            UpdateCouponStoreDTO updateCouponStoreDTO = new UpdateCouponStoreDTO();
            updateCouponStoreDTO.setStoreId(systemStoreInfoDO.getStoreId());
            updateCouponStoreDTO.setTagIds(tagIds);
            updateCouponStoreDTO.setStoreName(storeUserSaveReqVO.getStoreName());
            goodCouponApi.updateCouponStoreByTagIdAndStoreId(List.of(updateCouponStoreDTO), CouponStoreTagSqlTypeEnum.UPDATE);
        }
        redisForAppletAd.delStoreById(BusinessContextHolder.getBusinessId(),storeId);
        List<Long> newTagIds = normalizeTagIds(storeUserSaveReqVO.getTagIds());
        if (!CollectionUtils.isEmpty(newTagIds)) {
            ActivityStoreTagUpdateDTO activityUpdate = new ActivityStoreTagUpdateDTO();
            activityUpdate.setStoreId(storeId); activityUpdate.setTagIds(newTagIds);
            storeActivityTagOutbox.enqueue(List.of(activityUpdate), CouponStoreTagSqlTypeEnum.UPDATE);
            refreshActivityTagCache(storeId, newTagIds, List.of());
            appletPageManagementService.addRedisByStoreTags(storeId, newTagIds, BusinessContextHolder.getBusinessId());
        }
        refreshStoreBusinessStatusCache(systemStoreInfoDO);
        storeBackgroundCacheService.refreshStoresAfterCommit(Collections.singleton(storeId));
        storeCityListCacheService.refreshStoreAfterCommit(storeId, null,
                systemStoreInfoDO.getStoreCity(), false);

        return storeId;
    }

    private void createExchange(Long storeId) {
        String exchangeName = "exchange" + storeId;
        String topicName = "queue" + storeId;
        rabbitMQService.bindExchange(rabbitMQService.creatExchange(exchangeName), rabbitMQService.createQueue(topicName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_UPDATE_SUB_TYPE, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_UPDATE_SUCCESS)
    public void updateStore(StoreSaveReqVO storeUserSaveReqVO) {
        SystemStoreInfoDO systemStoreInfoOldDO = systemStoreInfoMapper.selectById(storeUserSaveReqVO.getStoreId());
        fillCampusDeliveryDefaults(storeUserSaveReqVO);
        validateCampusDeliveryConfig(storeUserSaveReqVO);
        validateTakeawayConfig(storeUserSaveReqVO.getStoreTakeaway(),
                storeUserSaveReqVO.getSystemStoreDeliveryScopeList());
        validateStoreExpensesForUpdate(storeUserSaveReqVO);
        if (storeUserSaveReqVO.getStoreStatus().equals(StoreStatusEnum.SHUTDOWN.getStatus()) && storeUserSaveReqVO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus()) && systemStoreInfoOldDO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus())) {
            throw exception(STORE_STOP_EXISTS);
        }
        if (StringUtils.isNotEmpty(storeUserSaveReqVO.getMeituanId())){
            List<SystemStoreInfoDO> systemStoreInfoDOList = systemStoreInfoMapper
                    .selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                            .eq(SystemStoreInfoDO::getMeituanId, storeUserSaveReqVO.getMeituanId())
                            .ne(SystemStoreInfoDO::getStoreId, storeUserSaveReqVO.getStoreId()));
            if(CollectionUtil.isNotEmpty(systemStoreInfoDOList)){
                throw exception(STORE_MEITUAN_EXISTS);
            }
        }
        if (StringUtils.isNotEmpty(storeUserSaveReqVO.getHungryId())){
            List<SystemStoreInfoDO> systemStoreInfoDOList = systemStoreInfoMapper
                    .selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                            .eq(SystemStoreInfoDO::getHungryId, storeUserSaveReqVO.getHungryId())
                            .ne(SystemStoreInfoDO::getStoreId, storeUserSaveReqVO.getStoreId()));
            if(CollectionUtil.isNotEmpty(systemStoreInfoDOList)){
                throw exception(STORE_MEITUAN_EXISTS);
            }
        }
        validateStoreNameUnique(storeUserSaveReqVO.getStoreId(), storeUserSaveReqVO.getStoreName().trim());
        SystemStoreInfoDO systemStoreInfoDO = BeanUtils.toBean(storeUserSaveReqVO, SystemStoreInfoDO.class);
        if(!systemStoreInfoDO.getStoreStatus().equals(systemStoreInfoOldDO.getStoreStatus())){
            creatLog(systemStoreInfoDO);
        }
        //适配旧门店数据
        storeUserSaveReqVO.getSystemStoreExpensesList()
                .stream()
                .filter(item -> item != null)
                .map(item -> {
                    if (item.getStoreExpensesType() == 0) {
                        systemStoreInfoDO.setPackingCharge(item.getAdditionaaCosts());
                    }
                    return null;
                })
                .collect(Collectors.toList());
        systemStoreInfoMapper.updateById(systemStoreInfoDO);
        // 获取当前登录用户ID和业务ID
        Long currentUserId = storeUserSaveReqVO.getUserId();
        Long businessId = BusinessContextHolder.getBusinessId();
        Long storeId = systemStoreInfoDO.getStoreId();

// 获取门店的当前店长信息
        SystemStoreUserDO currentManager = systemStoreUserMapper.selectOne(
                Wrappers.lambdaQuery(SystemStoreUserDO.class)
                        .eq(SystemStoreUserDO::getStoreId, storeId)
                        .eq(SystemStoreUserDO::getType, StoreUserEnum.STOREMANAGER.getStatus())
                        .eq(SystemStoreUserDO::getBusinessId, businessId)
        );

// 获取门店的当前店员信息（包括店长）
        List<SystemStoreUserDO> storeClerks = systemStoreUserMapper.selectList(
                Wrappers.lambdaQuery(SystemStoreUserDO.class)
                        .eq(SystemStoreUserDO::getStoreId, storeId)
                        .eq(SystemStoreUserDO::getBusinessId, businessId)
        );

// 检查当前用户是否已经是该门店的店员
        boolean isCurrentUserClerk = storeClerks.stream()
                .anyMatch(clerk -> clerk.getUserId().equals(currentUserId));

        // 如果当前用户已是店员且不是店长，则直接修改其角色为店长
        if (isCurrentUserClerk && (currentManager == null || !currentManager.getUserId().equals(currentUserId))) {
            // 删除原店长
            if (currentManager != null) {
                systemStoreUserMapper.delete(
                        Wrappers.lambdaQuery(SystemStoreUserDO.class)
                                .eq(SystemStoreUserDO::getStoreId, storeId)
                                .eq(SystemStoreUserDO::getType, StoreUserEnum.STOREMANAGER.getStatus())
                                .eq(SystemStoreUserDO::getBusinessId, businessId)
                );
            }

            // 将当前用户设置为店长
            SystemStoreUserDO userToUpdate = storeClerks.stream()
                    .filter(clerk -> clerk.getUserId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到该店员记录"));

            userToUpdate.setType(StoreUserEnum.STOREMANAGER.getStatus());
            systemStoreUserMapper.updateById(userToUpdate);
        }
        // 如果当前用户不是店员，则删除原店长并添加新店长
        else if (!isCurrentUserClerk) {
            // 删除原店长
            if (currentManager != null) {
                systemStoreUserMapper.delete(
                        Wrappers.lambdaQuery(SystemStoreUserDO.class)
                                .eq(SystemStoreUserDO::getStoreId, storeId)
                                .eq(SystemStoreUserDO::getType, StoreUserEnum.STOREMANAGER.getStatus())
                                .eq(SystemStoreUserDO::getBusinessId, businessId)
                );
            }

            // 删除当前用户在其他门店的店长记录（可选，根据业务需求）
//            systemStoreUserMapper.delete(
//                    Wrappers.lambdaQuery(SystemStoreUserDO.class)
//                            .eq(SystemStoreUserDO::getUserId, currentUserId)
//                            .eq(SystemStoreUserDO::getBusinessId, businessId)
//            );
            // 添加新店长
            SystemStoreUserDO newManager = new SystemStoreUserDO();
            newManager.setStoreId(storeId);
            newManager.setUserId(currentUserId);
            newManager.setType(StoreUserEnum.STOREMANAGER.getStatus());
            newManager.setOrgId(storeUserSaveReqVO.getOrgId());
            newManager.setVisible(StoreUserTypeConstants.STORE_USER_VISIBLE);
            newManager.setBusinessId(businessId);
            systemStoreUserMapper.insert(newManager);
        }
        // 更新门店表中的店长信息
        systemStoreInfoDO.setUserId(currentUserId);
        if(storeUserSaveReqVO.getPeakHours().contains("24:00")){
            storeUserSaveReqVO.setPeakHours(storeUserSaveReqVO.getPeakHours().replaceAll("24:00", "23:59"));
        }
        systemStoreInfoMapper.updateById(systemStoreInfoDO);
        saveFranchiseeInfo(systemStoreInfoDO.getStoreId(), storeUserSaveReqVO.getFranchiseeInfo());

        // 批量插入配送范围和费用信息
        insertBatchData(storeUserSaveReqVO, systemStoreInfoDO.getStoreId(), false);
        // 批量标签
        List<Long> oldTagIds = getStoreTagIds(systemStoreInfoDO.getStoreId());
        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(Wrappers.lambdaQuery(SystemStoreTagDO.class)
                .eq(SystemStoreTagDO::getStoreId, systemStoreInfoDO.getStoreId()));
        UpdateCouponStoreDTO updateCouponStoreDTO = new UpdateCouponStoreDTO();
        if(CollectionUtils.isNotEmpty(systemStoreTagDOS)){
            // 原来
            List<Long> storeTagIds = systemStoreTagDOS.stream().map(SystemStoreTagDO::getTagId).toList();
            // 新的
            List<Long> tagIds = storeUserSaveReqVO.getTagIds();
            if(CollectionUtils.isNotEmpty(tagIds)){
                List<Long> deleteTagIds = storeTagIds.stream().filter(storeTagId -> !tagIds.contains(storeTagId)).toList();
                if(CollectionUtils.isNotEmpty(deleteTagIds)){
                    //delete
                    updateCouponStoreDTO.setStoreId(systemStoreInfoDO.getStoreId());
                    updateCouponStoreDTO.setTagIds(deleteTagIds);
                    goodCouponApi.updateCouponStoreByTagIdAndStoreId(List.of(updateCouponStoreDTO), CouponStoreTagSqlTypeEnum.DELETE);
                }
                List<Long> insertTagIds = tagIds.stream().filter(tagId -> !storeTagIds.contains(tagId)).toList();
                if(CollectionUtils.isNotEmpty(insertTagIds)){
                    //insert
                    updateCouponStoreDTO.setStoreId(systemStoreInfoDO.getStoreId());
                    updateCouponStoreDTO.setTagIds(insertTagIds);
                    updateCouponStoreDTO.setStoreName(storeUserSaveReqVO.getStoreName());
                    goodCouponApi.updateCouponStoreByTagIdAndStoreId(List.of(updateCouponStoreDTO), CouponStoreTagSqlTypeEnum.UPDATE);
                }
            }else {
                // delete
                updateCouponStoreDTO.setStoreId(systemStoreInfoDO.getStoreId());
                updateCouponStoreDTO.setTagIds(storeTagIds);
                goodCouponApi.updateCouponStoreByTagIdAndStoreId(List.of(updateCouponStoreDTO), CouponStoreTagSqlTypeEnum.DELETE);
            }
        }else{
            List<Long> tagIds = storeUserSaveReqVO.getTagIds();
            if(CollectionUtils.isNotEmpty(tagIds)){
                updateCouponStoreDTO.setStoreId(systemStoreInfoDO.getStoreId());
                updateCouponStoreDTO.setTagIds(tagIds);
                updateCouponStoreDTO.setStoreName(storeUserSaveReqVO.getStoreName());
                goodCouponApi.updateCouponStoreByTagIdAndStoreId(List.of(updateCouponStoreDTO), CouponStoreTagSqlTypeEnum.UPDATE);
            }
        }

        systemStoreTagMapper.delete(Wrappers.lambdaQuery(SystemStoreTagDO.class)
                .eq(SystemStoreTagDO::getStoreId, systemStoreInfoDO.getStoreId()));
        List<Long> newTagIds = normalizeTagIds(storeUserSaveReqVO.getTagIds());
        if (!CollectionUtils.isEmpty(newTagIds)) {
            List<SystemStoreTagDO> systemStoreTagDOList = new ArrayList<>();
            newTagIds.forEach(tagId -> {
                SystemStoreTagDO systemStoreTagDO = new SystemStoreTagDO();
                systemStoreTagDO.setStoreId(systemStoreInfoDO.getStoreId());
                systemStoreTagDO.setTagId(tagId);
                systemStoreTagDOList.add(systemStoreTagDO);
            });
            systemStoreTagMapper.insertBatch(systemStoreTagDOList);
        }
        GoodCouponStoreDTO goodCouponStoreDTO = new GoodCouponStoreDTO();
        goodCouponStoreDTO.setStoreName(storeUserSaveReqVO.getStoreName());
        goodCouponStoreDTO.setStoreId(storeUserSaveReqVO.getStoreId());
        goodCouponApi.updateCouponStore(goodCouponStoreDTO);
        //创建交换机
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(storeUserSaveReqVO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", storeUserSaveReqVO);
        this.createExchange(systemStoreInfoDO.getStoreId());
        redisForAppletAd.delStoreById(BusinessContextHolder.getBusinessId(),storeId);
        refreshStoreTagRedis(storeId, oldTagIds, newTagIds);
        // 计算标签差集，未变化的标签不参与缓存同步与联动，避免重叠标签同时走新增/删除两条链路
        List<Long> removedTagIds = oldTagIds == null ? Collections.emptyList()
                : oldTagIds.stream().filter(tagId -> !newTagIds.contains(tagId)).collect(Collectors.toList());
        List<Long> addedTagIds = newTagIds.stream().filter(tagId -> !oldTagIds.contains(tagId)).collect(Collectors.toList());
        refreshActivityTagCache(storeId, addedTagIds, removedTagIds);
        // 联动维护营销活动-门店绑定（activity_store）：新增标签补绑定，移除标签按缓存命中判断后删绑定
        if (!CollectionUtils.isEmpty(addedTagIds)) {
            ActivityStoreTagUpdateDTO addUpdate = new ActivityStoreTagUpdateDTO();
            addUpdate.setStoreId(storeId);
            addUpdate.setTagIds(addedTagIds);
            storeActivityTagOutbox.enqueue(List.of(addUpdate), CouponStoreTagSqlTypeEnum.UPDATE);
        }
        if (!CollectionUtils.isEmpty(removedTagIds)) {
            ActivityStoreTagUpdateDTO delUpdate = new ActivityStoreTagUpdateDTO();
            delUpdate.setStoreId(storeId);
            delUpdate.setTagIds(removedTagIds);
            storeActivityTagOutbox.enqueue(List.of(delUpdate), CouponStoreTagSqlTypeEnum.DELETE);
        }
        refreshStoreBusinessStatusCache(systemStoreInfoDO);
        storeCityListCacheService.refreshStoreAfterCommit(storeId,
                systemStoreInfoOldDO.getStoreCity(), systemStoreInfoDO.getStoreCity(), false);
        storeBackgroundCacheService.refreshStoresAfterCommit(Collections.singleton(storeId));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_DELETE_SUB_TYPE, bizNo = "1",
            success = SYSTEM_STORE_DELETE_SUCCESS)
    public void deleteStore(Long storeId) {
        SystemStoreInfoDO systemStoreInfoDO = validateStorenameDel(storeId);
        List<Long> removedActivityTagIds = systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                        .eq(SystemStoreTagDO::getStoreId, storeId)).stream().map(SystemStoreTagDO::getTagId)
                .filter(Objects::nonNull).distinct().toList();
        systemStoreInfoMapper.deleteById(storeId);
        // 同一事务清理门店标签关系，避免已删除门店继续占用标签组。
        int deletedTagCount = systemStoreTagMapper.delete(new LambdaQueryWrapperX<SystemStoreTagDO>()
                .eq(SystemStoreTagDO::getStoreId, storeId));
        log.info("删除门店清理标签关系 businessId={}, storeId={}, relationCount={}",
                BusinessContextHolder.getBusinessId(), storeId, deletedTagCount);
        if (!removedActivityTagIds.isEmpty()) {
            ActivityStoreTagUpdateDTO activityUpdate = new ActivityStoreTagUpdateDTO();
            activityUpdate.setStoreId(storeId); activityUpdate.setTagIds(removedActivityTagIds);
            storeActivityTagOutbox.enqueue(List.of(activityUpdate), CouponStoreTagSqlTypeEnum.DELETE);
        }
        refreshActivityTagCache(storeId, List.of(), removedActivityTagIds);
        systemStoreFranchiseeInfoMapper.delete(new LambdaQueryWrapperX<SystemStoreFranchiseeInfoDO>()
                .eq(SystemStoreFranchiseeInfoDO::getStoreId, storeId));
        systemStoreDeliveryScopeMapper.update(new LambdaUpdateWrapper<SystemStoreDeliveryScopeDO>().set(SystemStoreDeliveryScopeDO::getDeleted, 1).eq(SystemStoreDeliveryScopeDO::getStoreId, storeId));
        systemStoreExpensesMapper.update(new LambdaUpdateWrapper<SystemStoreExpensesDO>()
                .set(SystemStoreExpensesDO::getDeleted, 1).eq(SystemStoreExpensesDO::getStoreId, storeId));
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(systemStoreInfoDO, SystemStoreInfoDO.class));
        LogRecordContext.putVariable("store", systemStoreInfoDO);
        redisForAppletAd.delStoreById(BusinessContextHolder.getBusinessId(),storeId);
        RedissonUtils.deleteObject(formatStoreBusinessStatusCacheKey(storeId));
        storeCityListCacheService.refreshStoreAfterCommit(storeId,
                systemStoreInfoDO == null ? null : systemStoreInfoDO.getStoreCity(), null, true);
        storeBackgroundCacheService.removeStoreAfterCommit(storeId);

    }

    @Override
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_UPDATE_SUB_TYPE, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_UPDATE_SUCCESS)
    public void updateOpenStatus(Long storeId, Integer status) {
        // 更新状态
        SystemStoreInfoDO systemStoreInfoOldDO = systemStoreInfoMapper.selectById(storeId);
        if (status.equals(StoreStatusEnum.SHUTDOWN.getStatus()) && status.equals(StoreStatusEnum.OPEN.getStatus()) && systemStoreInfoOldDO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus())) {
            throw exception(STORE_STOP_EXISTS);
        }
        SystemStoreInfoDO storeInfoDO = new SystemStoreInfoDO();
        storeInfoDO.setStoreId(storeId);
        storeInfoDO.setOpenStatus(status);
        systemStoreInfoMapper.updateById(storeInfoDO);
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(systemStoreInfoOldDO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", systemStoreInfoOldDO);
        this.createExchange(systemStoreInfoOldDO.getStoreId());
        redisForAppletAd.delStoreById(BusinessContextHolder.getBusinessId(),storeId);
        systemStoreInfoOldDO.setOpenStatus(status);
        refreshStoreBusinessStatusCache(systemStoreInfoOldDO);
        storeCityListCacheService.refreshStoreAfterCommit(storeId,
                systemStoreInfoOldDO.getStoreCity(), systemStoreInfoOldDO.getStoreCity(), false);
    }

    @Override
    public void getStoreQRCode(Long id) {
    }

    @Override
    public String getWxaCodeByStoreId(Long storeId) {


        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);

        return ObjectUtils.isEmpty(systemStoreInfoDO) ? Strings.EMPTY : systemStoreInfoDO.getStoreQrcodeImage();
    }

    @Override
    @DataPermission(enable = false)
    public StoreDetailResVO getStoreDetail(Long storeId) {
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        if( systemStoreInfoDO.getUserId()!=null){
            AdminUserDO adminUserDO = userMapper.selectById(systemStoreInfoDO.getUserId());
            if(adminUserDO!=null){
                systemStoreInfoDO.setStoreLeader(adminUserDO.getNickname());
                systemStoreInfoDO.setStoreLeaderPhone(adminUserDO.getMobile());
            }
        }
        StoreDetailResVO storeDetailResVO = BeanUtils.toBean(systemStoreInfoDO, StoreDetailResVO.class);
        SystemStoreFranchiseeInfoDO franchiseeInfo = systemStoreFranchiseeInfoMapper.selectOne(
                new LambdaQueryWrapperX<SystemStoreFranchiseeInfoDO>()
                        .eq(SystemStoreFranchiseeInfoDO::getStoreId, storeId));
        if (franchiseeInfo != null) {
            storeDetailResVO.setFranchiseeInfo(BeanUtils.toBean(franchiseeInfo, StoreFranchiseeInfoRespVO.class));
        }


        List<SystemStoreDeliveryScopeDO> systemStoreDeliveryScopeList = systemStoreDeliveryScopeMapper.selectList(new LambdaQueryWrapper<SystemStoreDeliveryScopeDO>().eq(SystemStoreDeliveryScopeDO::getStoreId, storeId));
        storeDetailResVO.setSystemStoreDeliveryScopeList(BeanUtils.toBean(systemStoreDeliveryScopeList, SystemStoreDeliveryScopeVO.class));
        List<SystemStoreExpensesDO> systemStoreExpensesList = systemStoreExpensesMapper.selectList(new LambdaQueryWrapper<SystemStoreExpensesDO>().eq(SystemStoreExpensesDO::getStoreId, storeId).orderByAsc(SystemStoreExpensesDO::getStoreExpensesType));
        storeDetailResVO.setSystemStoreExpensesList(normalizeStoreExpensesForQuery(systemStoreExpensesList));
        List<SystemStoreTagDO> systemStoreTagList = systemStoreTagMapper.selectList(new LambdaQueryWrapper<SystemStoreTagDO>().eq(SystemStoreTagDO::getStoreId, storeId));
        if (CollectionUtil.isNotEmpty(systemStoreTagList)) {
            Set<Long> tagIds = systemStoreTagList.stream().map(SystemStoreTagDO::getTagId).collect(Collectors.toSet());
            List<TagValueDO> tagValueList = tagValueMapper.selectList(new LambdaQueryWrapperX<TagValueDO>().in(TagValueDO::getId, tagIds));
            Map<Long, List<TagValueRespVO>> tagValueMap = tagValueList.stream().collect(Collectors.groupingBy(TagValueDO::getTagGroupId, Collectors.mapping(item -> BeanUtils.toBean(item, TagValueRespVO.class), Collectors.toList())));
            List<Long> tagGroupIds = tagValueList.stream().map(TagValueDO::getTagGroupId).toList();
            if (CollectionUtils.isNotEmpty(tagGroupIds)) {
                List<TagGroupDO> tagGroupList = tagGroupMapper.selectBatchIds(tagGroupIds);
                List<TagGroupRespVO> tagGroupResList = BeanUtils.toBean(tagGroupList, TagGroupRespVO.class);
                tagGroupResList.forEach(tagGroup -> {
                    List<TagValueRespVO> tagValues = tagValueMap.get(tagGroup.getId());
                    tagGroup.setTagValues(tagValues);
                });
                storeDetailResVO.setStoreTagList(tagGroupResList);
            }
        }
        return storeDetailResVO;
    }

    /**
     * 保存门店加盟商信息。请求未携带加盟商对象时保持原数据不变，字段全部为空时逻辑删除。
     */
    private void saveFranchiseeInfo(Long storeId, StoreFranchiseeInfoSaveReqVO reqVO) {
        if (reqVO == null) {
            return;
        }
        String franchiseeName = trimToNull(reqVO.getFranchiseeName());
        String franchiseeMobile = trimToNull(reqVO.getFranchiseeMobile());
        String idCardNo = trimToNull(reqVO.getIdCardNo());
        String bankName = trimToNull(reqVO.getBankName());
        String bankProvince = trimToNull(reqVO.getBankProvince());
        String bankCity = trimToNull(reqVO.getBankCity());
        String bankCardNo = trimToNull(reqVO.getBankCardNo());
        if (bankCardNo != null) {
            bankCardNo = bankCardNo.replaceAll("\\s+", "");
        }
        if (Stream.of(franchiseeName, franchiseeMobile, idCardNo, bankName,
                bankProvince, bankCity, bankCardNo).allMatch(Objects::isNull)) {
            systemStoreFranchiseeInfoMapper.delete(new LambdaQueryWrapperX<SystemStoreFranchiseeInfoDO>()
                    .eq(SystemStoreFranchiseeInfoDO::getStoreId, storeId));
            return;
        }

        SystemStoreFranchiseeInfoDO franchiseeInfo = new SystemStoreFranchiseeInfoDO();
        franchiseeInfo.setStoreId(storeId);
        franchiseeInfo.setFranchiseeName(franchiseeName);
        franchiseeInfo.setFranchiseeMobile(franchiseeMobile);
        franchiseeInfo.setIdCardNo(idCardNo);
        franchiseeInfo.setBankName(bankName);
        franchiseeInfo.setBankProvince(bankProvince);
        franchiseeInfo.setBankCity(bankCity);
        franchiseeInfo.setBankCardNo(bankCardNo);
        systemStoreFranchiseeInfoMapper.upsert(franchiseeInfo);
    }

    /**
     * 去除文本首尾空白，空文本统一转换为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void insertBatchData(StoreSaveReqVO storeUserSaveReqVO, Long storeId, boolean createMode) {
        List<SystemStoreDeliveryScopeDO> deliveryScopes = Optional.ofNullable(storeUserSaveReqVO.getSystemStoreDeliveryScopeList())
                .orElse(Collections.emptyList())
                .stream()
                .map(item -> {
                    SystemStoreDeliveryScopeDO scope = BeanUtils.toBean(item, SystemStoreDeliveryScopeDO.class);
                    scope.setStoreId(storeId);
                    return scope;
                })
                .collect(Collectors.toList());
        List<SystemStoreExpensesDO> expenses = deduplicateStoreExpenses(storeUserSaveReqVO.getSystemStoreExpensesList())
                .stream()
                .map(item -> {
                    SystemStoreExpensesDO expense = BeanUtils.toBean(item, SystemStoreExpensesDO.class);
                    expense.setStoreId(storeId); // 统一设置门店ID
                    normalizeCampusDeliveryExpense(expense);
                    clearStoreExpenseAuditFields(expense);
                    return expense;
                })
                .filter(expense -> expense.getStoreExpensesType() != null)
                // 按 "store_id + store_expenses_type" 去重，保留最后一个出现的重复项
                .collect(Collectors.toMap(
                        // 以门店ID和费用类型作为联合唯一键（因为store_id已固定，也可简化为仅用type）
                        SystemStoreExpensesDO::getStoreExpensesType,
                        Function.identity(),
                        (existing, replacement) -> replacement // 重复时保留新值（可改为existing保留旧值）
                ))
                .values() // 提取去重后的value集合
                .stream()
                .collect(Collectors.toList());
        systemStoreDeliveryScopeMapper.delete(new LambdaQueryWrapperX<SystemStoreDeliveryScopeDO>()
                .eq(SystemStoreDeliveryScopeDO::getStoreId, storeId));
        if (!CollectionUtils.isEmpty(deliveryScopes)) {
            systemStoreDeliveryScopeMapper.insertBatch(deliveryScopes);
        }
        if (createMode) {
            systemStoreExpensesMapper.delete(new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                    .eq(SystemStoreExpensesDO::getStoreId, storeId));
            if (!CollectionUtils.isEmpty(expenses)) {
                systemStoreExpensesMapper.insertBatch(expenses);
            }
            return;
        }
        upsertStoreExpenses(storeId, expenses);
    }

    //数据拼接
    /**
     * 校园配送 type=3 只保存代取起送费，配送费不再配置。
     */
    private void normalizeCampusDeliveryExpense(SystemStoreExpensesDO expense) {
        if (expense == null || !Objects.equals(expense.getStoreExpensesType(), STORE_EXPENSE_TYPE_CAMPUS_DELIVERY)) {
            return;
        }
        expense.setAdditionaaCosts(BigDecimal.ZERO);
        expense.setStoreCalculationType(null);
    }

    /**
     * 清空前端可能传入的审计字段，创建/修改人和时间统一由后端自动填充。
     */
    private void clearStoreExpenseAuditFields(SystemStoreExpensesDO expense) {
        if (expense == null) {
            return;
        }
        expense.setId(null);
        expense.setCreator(null);
        expense.setUpdater(null);
        expense.setCreateTime(null);
        expense.setUpdateTime(null);
        expense.setDeleted(null);
    }

    /**
     * 修改门店费用时只新增或更新传入的费用类型，未传类型保持原样。
     */
    private void upsertStoreExpenses(Long storeId, List<SystemStoreExpensesDO> expenses) {
        if (CollectionUtil.isEmpty(expenses)) {
            return;
        }
        List<SystemStoreExpensesDO> oldExpenses = systemStoreExpensesMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                        .eq(SystemStoreExpensesDO::getStoreId, storeId));
        Map<Integer, SystemStoreExpensesDO> oldExpenseMap = CollectionUtil.isEmpty(oldExpenses)
                ? Collections.emptyMap()
                : oldExpenses.stream()
                .filter(item -> item != null && item.getStoreExpensesType() != null)
                .collect(Collectors.toMap(
                        SystemStoreExpensesDO::getStoreExpensesType,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        for (SystemStoreExpensesDO expense : expenses) {
            SystemStoreExpensesDO oldExpense = oldExpenseMap.get(expense.getStoreExpensesType());
            if (oldExpense == null) {
                systemStoreExpensesMapper.insert(expense);
                continue;
            }
            systemStoreExpensesMapper.update(null, new LambdaUpdateWrapper<SystemStoreExpensesDO>()
                    .set(SystemStoreExpensesDO::getMinimumDeliveryFee, expense.getMinimumDeliveryFee())
                    .set(SystemStoreExpensesDO::getAdditionaaCosts, expense.getAdditionaaCosts())
                    .set(SystemStoreExpensesDO::getStoreCalculationType, expense.getStoreCalculationType())
                    .eq(SystemStoreExpensesDO::getId, oldExpense.getId()));
        }
    }

    private PageResult<StoreResVO> joinData(PageResult<StoreResVO> pageResult) {
        if (pageResult.getList() == null || CollectionUtils.isEmpty(pageResult.getList())) {
            return pageResult;
        }
        // 收集需要查询的ID
        Set<Long> orgIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        for (StoreResVO item : pageResult.getList()) {
            if (item.getOrgId() != null) {
                orgIds.add(item.getOrgId());
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                userDOList.forEach(item1 -> {
                    userIds.add(item1.getUserId());
                });
            }
            List<SystemStoreTagDO> tagDOList = systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                    .eqIfPresent(SystemStoreTagDO::getStoreId, item.getStoreId())
                    .eq(SystemStoreTagDO::getDeleted, 0)
            );
            if (tagDOList != null && !CollectionUtils.isEmpty(tagDOList)) {
                tagDOList.forEach(item1 -> {
                    tagIds.add(item1.getTagId());
                });
            }
        }
        Map<Long, OrgDO> orgMap = new HashMap<>();
        Map<Long, AdminUserDO> userMap = new HashMap<>();
        Map<Long, TagValueDO> tagMap = new HashMap<>();
        // 批量查询
        if (!CollectionUtils.isEmpty(orgIds)) {
            orgMap = orgMapper.selectBatchIds(orgIds).stream()
                    .collect(Collectors.toMap(OrgDO::getId, org -> org));
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            userMap = adminUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(AdminUserDO::getId, user -> user));
        }
        if (!CollectionUtils.isEmpty(tagIds)) {
            tagMap = tagValueMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(TagValueDO::getId, tag -> tag));
        }
        // 填充数据
        for (StoreResVO item : pageResult.getList()) {
            OrgDO org = orgMap.get(item.getOrgId());
            if (org != null) {
                item.setOrgName(org.getName());
                AdminUserDO adminUserDO = findResponsibleBusinessUser(org.getId());
                if (adminUserDO != null) {
                    item.setOrgName(org.getName() + "/" + adminUserDO.getNickname() + " " + adminUserDO.getMobile());
                }
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (!CollectionUtils.isEmpty(userMap) && userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                StringBuilder regionUsers = new StringBuilder();
                for (StoreUserDO userDO : userDOList) {
                    AdminUserDO adminUserDO = userMap.get(userDO.getUserId());
                    if (adminUserDO != null) {
                        if (regionUsers.length() > 0) {
                            regionUsers.append(", ");
                        }
                        regionUsers.append(adminUserDO.getNickname()).append("/").append(adminUserDO.getMobile());
                    }
                }
                item.setRegionUser(regionUsers.toString());
                item.setStoreLeader(regionUsers.toString());
            }
            if (!CollectionUtils.isEmpty(tagMap)) {
                List<SystemStoreTagDO> tagDOList = systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                        .eqIfPresent(SystemStoreTagDO::getStoreId, item.getStoreId())
                        .eq(SystemStoreTagDO::getDeleted, 0)
                );
                Map<Long, TagValueDO> finalTagMap = tagMap;

                tagDOList.forEach(item1 -> {
                    TagValueDO tagValueDO = finalTagMap.get(item1.getTagId());
                    if (tagValueDO != null) {
                        if (item.getTagName() == null) {
                            item.setTagName(tagValueDO.getName());
                        } else {
                            item.setTagName(item.getTagName() + "," + tagValueDO.getName());
                        }
                    }
                });
            }
//            String storeLeader = Optional.ofNullable(item.getStoreLeader()).orElse("");
//            String storeLeaderPhone = Optional.ofNullable(item.getStoreLeaderPhone()).orElse("");
//
//            String result = storeLeader;
//            if (!storeLeaderPhone.isEmpty()) {
//                result += " / " + storeLeaderPhone;
//            }

            //    item.setStoreLeader(result);
        }
        return pageResult;
    }


    //数据拼接
    private PageResult<StorePageResVO> joinDataTwo(PageResult<StorePageResVO> pageResult) {
        if (pageResult.getList() == null || CollectionUtils.isEmpty(pageResult.getList())) {
            return pageResult;
        }
        // 收集需要查询的ID
        Set<Long> orgIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        for (StorePageResVO item : pageResult.getList()) {
            if (item.getOrgId() != null) {
                orgIds.add(item.getOrgId());
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                userDOList.forEach(item1 -> {
                    userIds.add(item1.getUserId());
                });
            }
            List<SystemStoreTagDO> tagDOList = systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                    .eqIfPresent(SystemStoreTagDO::getStoreId, item.getStoreId())
                    .eq(SystemStoreTagDO::getDeleted, 0)
            );
            if (tagDOList != null && !CollectionUtils.isEmpty(tagDOList)) {
                tagDOList.forEach(item1 -> {
                    tagIds.add(item1.getTagId());
                });
            }
        }
        Map<Long, OrgDO> orgMap = new HashMap<>();
        Map<Long, AdminUserDO> userMap = new HashMap<>();
        Map<Long, TagValueDO> tagMap = new HashMap<>();
        // 批量查询
        if (!CollectionUtils.isEmpty(orgIds)) {
            orgMap = orgMapper.selectBatchIds(orgIds).stream()
                    .collect(Collectors.toMap(OrgDO::getId, org -> org));
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            userMap = adminUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(AdminUserDO::getId, user -> user));
        }
        if (!CollectionUtils.isEmpty(tagIds)) {
            tagMap = tagValueMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(TagValueDO::getId, tag -> tag));
        }
        // 填充数据
        for (StorePageResVO item : pageResult.getList()) {
            OrgDO org = orgMap.get(item.getOrgId());
            if (org != null) {
                item.setOrgName(org.getName());
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (!CollectionUtils.isEmpty(userMap) && userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                StringBuilder regionUsers = new StringBuilder();
                for (StoreUserDO userDO : userDOList) {
                    AdminUserDO adminUserDO = userMap.get(userDO.getUserId());
                    if (adminUserDO != null) {
                        if (regionUsers.length() > 0) {
                            regionUsers.append(", ");
                        }
                        regionUsers.append(adminUserDO.getNickname()).append("/").append(adminUserDO.getMobile());
                    }
                }
                item.setRegionUser(regionUsers.toString());
            }
            if (!CollectionUtils.isEmpty(tagMap)) {
                List<SystemStoreTagDO> tagDOList = systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                        .eqIfPresent(SystemStoreTagDO::getStoreId, item.getStoreId())
                        .eq(SystemStoreTagDO::getDeleted, 0)
                );
                Map<Long, TagValueDO> finalTagMap = tagMap;

                tagDOList.forEach(item1 -> {
                    TagValueDO tagValueDO = finalTagMap.get(item1.getTagId());
                    if (tagValueDO != null) {
                        if (item.getTagName() == null) {
                            item.setTagName(tagValueDO.getName());
                        } else {
                            item.setTagName(item.getTagName() + "," + tagValueDO.getName());
                        }
                    }
                });
            }
            item.setStoreLeader(item.getStoreLeader() + "/" + item.getStoreLeaderPhone());
        }
        return pageResult;
    }


    @VisibleForTesting
    SystemStoreInfoDO validateStoreNameUnique(Long id, String storeName) {
/*        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectOne(
                new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .eq(SystemStoreInfoDO::getStoreSource, 0)
                        .apply("store_name COLLATE utf8mb4_bin = {0}", storeName)
        );*/
        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectOne(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                .eq(SystemStoreInfoDO::getStoreSource, 0)
                .eq(SystemStoreInfoDO::getStoreName, storeName)
                .last("limit 1")
        );
        if (storeInfoDO == null) {
            return null;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(STORE_REPEAT_EXISTS);
        }
        if (!storeInfoDO.getStoreId().equals(id)) {
            throw exception(STORE_REPEAT_EXISTS);
        }
        return storeInfoDO;
    }

    @VisibleForTesting
    SystemStoreInfoDO validateStorenameDel(Long id) {

        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectById(id);
        if (storeInfoDO == null) {
            throw exception(STORE_NOT_EXISTS);
        }
        if (id == null) {
            throw exception(STORE_NOT_EXISTS);
        }
        if (storeInfoDO.getStoreStatus() != 1 && storeInfoDO.getOpenStatus() != 1) {
            throw exception(STORE_OPEN_EXISTS);
        }
        return storeInfoDO;
    }

    @Override
    public int moveStore(OrgStoreMoveReqVO orgStoreMoveReqVO) {
        Long newOrgId = orgStoreMoveReqVO.getNewOrgId();
        Long oldOrgId = orgStoreMoveReqVO.getOldOrgId();
        List<Long> storeIds = orgStoreMoveReqVO.getStoreIds();
        LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SystemStoreInfoDO::getStoreId, storeIds)
                .set(SystemStoreInfoDO::getOrgId, newOrgId)
                .eq(SystemStoreInfoDO::getOrgId, oldOrgId);
        return systemStoreInfoMapper.update(updateWrapper);
    }

    @Override
    public int removeStore(OrgStoreMoveReqVO orgStoreMoveReqVO) {
        Long oldOrgId = orgStoreMoveReqVO.getOldOrgId();
        List<Long> storeIds = orgStoreMoveReqVO.getStoreIds();
        LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SystemStoreInfoDO::getStoreId, storeIds)
                .set(SystemStoreInfoDO::getOrgId, 0)
                .eq(SystemStoreInfoDO::getOrgId, oldOrgId);
        return systemStoreInfoMapper.update(updateWrapper);
    }



    @Override
    public List<SystemStoreInfoDO> getStoreListByOrgIdAndStoreId(Long orgId, String storeName,Integer storeStatus) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(ObjectUtil.isNotEmpty(orgId),SystemStoreInfoDO::getOrgId, orgId);
        queryWrapper.like(StringUtils.isNotEmpty(storeName), SystemStoreInfoDO::getStoreName, storeName)
                .eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.eq(ObjectUtil.isNotEmpty(storeStatus),SystemStoreInfoDO::getStoreStatus, storeStatus);
        //queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
        return systemStoreInfoMapper.selectList(queryWrapper);
    }

    @Override
    public List<SystemStoreInfoDO> getOpenStoreListByOrgId(Long orgId, String storeName) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(SystemStoreInfoDO::getOrgId, orgId).eq(SystemStoreInfoDO::getStoreStatus, 0)
                .eq(SystemStoreInfoDO::getStoreSource, 0);
        ;
        queryWrapper.like(StringUtils.isNotEmpty(storeName), SystemStoreInfoDO::getStoreName, storeName);
        return systemStoreInfoMapper.selectList(queryWrapper);
    }

    @Override
    public List<OrgStoreNum> getNodeStoreNum(Collection<Long> unionIds) {
        return systemStoreInfoMapper.getNodeStoreNum(unionIds);
    }

    @Override
    public Boolean hasOrgStore(Long id) {
        QueryWrapper<SystemStoreInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("org_id", id);
        return systemStoreInfoMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public int updateStoreLeader(Long storeId, AdminUserDO adminUserDO) {
        LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemStoreInfoDO::getStoreId, storeId)
                .set(SystemStoreInfoDO::getStoreLeader, adminUserDO.getNickname())
                .set(SystemStoreInfoDO::getStoreLeaderPhone, adminUserDO.getMobile())
                .set(SystemStoreInfoDO::getUserId, adminUserDO.getId());
        return systemStoreInfoMapper.update(updateWrapper);
    }

    @Override
    public Set<Long> getStoreIdsByOrgIds(Set<Long> orgIds) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getOrgId, orgIds);
        return new HashSet<>(systemStoreInfoMapper.selectList(queryWrapper).stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toList()));
    }

    @Override
    public int unStoreManager(Long storeId) {
        LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SystemStoreInfoDO::getStoreLeader, null);
        updateWrapper.set(SystemStoreInfoDO::getStoreLeaderPhone, null);
        updateWrapper.set(SystemStoreInfoDO::getUserId, null);
        updateWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
        return systemStoreInfoMapper.update(updateWrapper);
    }

    @Override
    public List<SystemStoreInfoDO> selectStoreByOrgIds(Set<Long> orgIds) {
        // 分批查询，每批50个orgId
        int batchSize = 50;
        List<Long> orgIdList = new ArrayList<>(orgIds);
        List<SystemStoreInfoDO> result = new ArrayList<>();

        for (int i = 0; i < orgIdList.size(); i += batchSize) {
            List<Long> batch = orgIdList.subList(i, Math.min(i + batchSize, orgIdList.size()));
            LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                    .select(
                            SystemStoreInfoDO::getStoreId,
                            SystemStoreInfoDO::getStoreName,
                            SystemStoreInfoDO::getStoreCity,
                            SystemStoreInfoDO::getOrgId,
                            SystemStoreInfoDO::getIdentificationTemplate,
                            SystemStoreInfoDO::getStoreStatus,
                            SystemStoreInfoDO::getUseStatus
                    )
                    .in(SystemStoreInfoDO::getOrgId, batch)
                    .eq(SystemStoreInfoDO::getStoreSource, 0);
            result.addAll(systemStoreInfoMapper.selectList(queryWrapper));
        }
        return result;
    }
    @Override
    public List<SystemStoreInfoDO> selectByLetterStoreByOrgIds(Set<Long> orgIds) {
        // 分批查询，每批50个orgId
        int batchSize = 50;
        List<Long> orgIdList = new ArrayList<>(orgIds);
        List<SystemStoreInfoDO> result = new ArrayList<>();

        for (int i = 0; i < orgIdList.size(); i += batchSize) {
            List<Long> batch = orgIdList.subList(i, Math.min(i + batchSize, orgIdList.size()));
            LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                    .select(
                            SystemStoreInfoDO::getStoreId,
                            SystemStoreInfoDO::getStoreName,
                            SystemStoreInfoDO::getStoreCity,
                            SystemStoreInfoDO::getOrgId,
                            SystemStoreInfoDO::getIdentificationTemplate,
                            SystemStoreInfoDO::getStoreStatus,
                            SystemStoreInfoDO::getUseStatus
                    )
                    .in(SystemStoreInfoDO::getOrgId, batch)
                    .eq(SystemStoreInfoDO::getStoreSource, 0)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0);
            result.addAll(systemStoreInfoMapper.selectList(queryWrapper));
        }
        return result;
    }

    @Override
    public void updateBatchTag(StoresUpdateVO storeUpdateVO) {
        Map<Long, UpdateCouponStoreDTO> updateCouponStoreMap = new HashMap<>();
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getStoreName);
                queryWrapper.in(SystemStoreInfoDO::getStoreId, storeUpdateVO.getStoresBatchVOList().stream().map(StoresBatchVO::getStoreId).collect(Collectors.toList()));
        List<SystemStoreInfoDO> storeList = systemStoreInfoMapper.selectList(queryWrapper);
        Map<Long, SystemStoreInfoDO> storeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(storeList)) {
            storeMap = storeList.stream().collect(Collectors.toMap(SystemStoreInfoDO::getStoreId, Function.identity(), (existing, replacement) -> existing));
        }
        if (!CollectionUtils.isEmpty(storeUpdateVO.getStoresBatchVOList())) {
            List<ActivityStoreTagUpdateDTO> activityStoreAddUpdates = new ArrayList<>();
            List<ActivityStoreTagUpdateDTO> activityStoreDelUpdates = new ArrayList<>();
            List<SystemStoreTagDO> list = systemStoreTagMapper.selectList(new LambdaQueryWrapper<SystemStoreTagDO>()
                    .in(SystemStoreTagDO::getStoreId, storeUpdateVO.getStoresBatchVOList().stream().map(StoresBatchVO::getStoreId).collect(Collectors.toList())));
            Map<Long, List<SystemStoreTagDO>> collect = list.stream().collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));
            Map<Long, SystemStoreInfoDO> finalStoreMap = storeMap;
            // 同一请求内 storeId 去重，避免重复门店基于旧标签快照插入重复绑定
            Set<Long> processedStoreIds = new HashSet<>();
            storeUpdateVO.getStoresBatchVOList().forEach(storesBatchVO -> {
                if (storesBatchVO.getStoreId() == null || !processedStoreIds.add(storesBatchVO.getStoreId())) {
                    return;
                }
                List<SystemStoreTagDO> storesStoreTagList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(collect.get(storesBatchVO.getStoreId()))) {
                    storesStoreTagList = collect.get(storesBatchVO.getStoreId());
                }
                List<Long> currentTagIds = storesStoreTagList.stream()
                        .map(SystemStoreTagDO::getTagId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                List<Long> newTagIds = normalizeTagIds(storesBatchVO.getTagIds());
                if (!CollectionUtils.isEmpty(newTagIds)) {
                    // 批量标签
                    List<Long> finalCurrentTagIds = currentTagIds;
                    List<Long> addedTagIds = new ArrayList<>();
                    newTagIds.forEach(tagId -> {
                        if (finalCurrentTagIds.contains(tagId)) {
                            return;
                        }
                        SystemStoreTagDO systemStoreTagDO = new SystemStoreTagDO();
                        systemStoreTagDO.setStoreId(storesBatchVO.getStoreId());
                        systemStoreTagDO.setTagId(tagId);
                        systemStoreTagMapper.insert(systemStoreTagDO);
                        addedTagIds.add(tagId);

                        // 记录新标签关系，供优惠券模块更新适用门店。
                        UpdateCouponStoreDTO dto = updateCouponStoreMap.get(storesBatchVO.getStoreId());
                        if (dto == null) {
                            dto = new UpdateCouponStoreDTO();
                            dto.setStoreId(storesBatchVO.getStoreId());
                            dto.setTagIds(new ArrayList<>());
                            dto.setStoreName(finalStoreMap.get(storesBatchVO.getStoreId()).getStoreName());
                            updateCouponStoreMap.put(storesBatchVO.getStoreId(), dto);
                        }
                        dto.getTagIds().add(tagId);
                    });
                    if (!CollectionUtils.isEmpty(addedTagIds)) {
                        appletPageManagementService.addRedisByStoreTags(storesBatchVO.getStoreId(), addedTagIds, BusinessContextHolder.getBusinessId());
                        // 同步更新门店标签 activity_tag 缓存
                        refreshActivityTagCache(storesBatchVO.getStoreId(), addedTagIds, null);
                        // 联动维护活动-门店绑定（activity_store）
                        ActivityStoreTagUpdateDTO addUpdate = new ActivityStoreTagUpdateDTO();
                        addUpdate.setStoreId(storesBatchVO.getStoreId());
                        addUpdate.setTagIds(addedTagIds);
                        activityStoreAddUpdates.add(addUpdate);
                    }
                } else {
                    // 批量标签
                    systemStoreTagMapper.delete(Wrappers.lambdaQuery(SystemStoreTagDO.class)
                            .eq(SystemStoreTagDO::getStoreId, storesBatchVO.getStoreId()));
                    if (!CollectionUtils.isEmpty(currentTagIds)) {
                        appletPageManagementService.delRedisByStoreTags(storesBatchVO.getStoreId(), currentTagIds, BusinessContextHolder.getBusinessId());
                    }
                    // 提交后失效标签缓存，避免旧标签继续命中活动。
                    deleteActivityTagCache(storesBatchVO.getStoreId());
                    // 标签全部移除后通知活动模块，处理时按最新标签判断。
                    if (!CollectionUtils.isEmpty(currentTagIds)) {
                        ActivityStoreTagUpdateDTO delUpdate = new ActivityStoreTagUpdateDTO();
                        delUpdate.setStoreId(storesBatchVO.getStoreId());
                        delUpdate.setTagIds(currentTagIds);
                        activityStoreDelUpdates.add(delUpdate);
                    }
                }

            });
            // 如果有新增的门店-标签关系，通知优惠券模块去更新优惠券门店绑定关系
            if(CollectionUtil.isNotEmpty(updateCouponStoreMap)){
                goodCouponApi.updateCouponStoreByTagIdAndStoreId(new ArrayList<>(updateCouponStoreMap.values()), CouponStoreTagSqlTypeEnum.UPDATE);
            }
            // 联动维护营销活动-门店绑定关系（activity_store），仅标签范围的活动受影响
            if (CollectionUtil.isNotEmpty(activityStoreAddUpdates)) {
                storeActivityTagOutbox.enqueue(activityStoreAddUpdates, CouponStoreTagSqlTypeEnum.UPDATE);
            }
            if (CollectionUtil.isNotEmpty(activityStoreDelUpdates)) {
                storeActivityTagOutbox.enqueue(activityStoreDelUpdates, CouponStoreTagSqlTypeEnum.DELETE);
            }
            redisForAppletAd.delAllStore(BusinessContextHolder.getBusinessId());
            storeBackgroundCacheService.refreshStoresAfterCommit(
                    storeUpdateVO.getStoresBatchVOList().stream()
                            .map(StoresBatchVO::getStoreId).filter(Objects::nonNull).collect(Collectors.toSet()));

        }
    }

    @Override
    public void updateDelTag(StoresUpdateVO storeUpdateVO) {

        List<UpdateCouponStoreDTO> updateCouponStoreList = new ArrayList<>();
        List<ActivityStoreTagUpdateDTO> activityStoreDelUpdates = new ArrayList<>();
        if (!CollectionUtils.isEmpty(storeUpdateVO.getStoresBatchVOList())) {
            // 先查各门店已绑定的标签，用于过滤入参中未绑定的标签
            List<Long> queryStoreIds = storeUpdateVO.getStoresBatchVOList().stream()
                    .map(StoresBatchVO::getStoreId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, Set<Long>> boundTagIdsByStoreId = queryStoreIds.isEmpty() ? Map.of()
                    : systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                            .in(SystemStoreTagDO::getStoreId, queryStoreIds))
                    .stream()
                    .filter(tag -> tag.getStoreId() != null && tag.getTagId() != null)
                    .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId,
                            Collectors.mapping(SystemStoreTagDO::getTagId, Collectors.toSet())));
            Set<Long> processedStoreIds = new HashSet<>();
            storeUpdateVO.getStoresBatchVOList().forEach(storesBatchVO -> {
                if (storesBatchVO.getStoreId() == null || !processedStoreIds.add(storesBatchVO.getStoreId())) {
                    return;
                }
                // 只保留该门店已绑定的标签；未绑定的标签不删除，也不产生任何后续操作
                List<Long> boundTagIds = normalizeTagIds(storesBatchVO.getTagIds()).stream()
                        .filter(tagId -> boundTagIdsByStoreId.getOrDefault(storesBatchVO.getStoreId(), Set.of()).contains(tagId))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(boundTagIds)) {
                    return;
                }
                appletPageManagementService.delRedisByStoreTags(storesBatchVO.getStoreId(), boundTagIds, BusinessContextHolder.getBusinessId());
                // 同步更新门店标签 activity_tag 缓存
                refreshActivityTagCache(storesBatchVO.getStoreId(), null, boundTagIds);
                UpdateCouponStoreDTO dto = new UpdateCouponStoreDTO();
                dto.setStoreId(storesBatchVO.getStoreId());
                dto.setTagIds(boundTagIds);
                updateCouponStoreList.add(dto);
                // 联动维护活动-门店绑定（activity_store），删除分支依赖缓存判断门店是否仍命中活动标签
                ActivityStoreTagUpdateDTO delUpdate = new ActivityStoreTagUpdateDTO();
                delUpdate.setStoreId(storesBatchVO.getStoreId());
                delUpdate.setTagIds(boundTagIds);
                activityStoreDelUpdates.add(delUpdate);

            });
            // 批量标签：逐门店精确删除，不能用 storeId IN + tagId IN 交叉积，
            // 否则会误删某门店绑定了但未要求删除的标签
            Set<Long> storeIds = updateCouponStoreList.stream()
                    .map(UpdateCouponStoreDTO::getStoreId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!CollectionUtils.isEmpty(updateCouponStoreList)) {
                for (UpdateCouponStoreDTO dto : updateCouponStoreList) {
                    if (dto.getStoreId() == null || CollectionUtils.isEmpty(dto.getTagIds())) {
                        continue;
                    }
                    systemStoreTagMapper.delete(Wrappers.lambdaQuery(SystemStoreTagDO.class)
                            .eq(SystemStoreTagDO::getStoreId, dto.getStoreId())
                            .in(SystemStoreTagDO::getTagId, dto.getTagIds()));
                }
                goodCouponApi.updateCouponStoreByTagIdAndStoreId(updateCouponStoreList, CouponStoreTagSqlTypeEnum.DELETE);
            }
            // 联动维护营销活动-门店绑定关系（activity_store），仅标签范围的活动受影响
            if (CollectionUtil.isNotEmpty(activityStoreDelUpdates)) {
                storeActivityTagOutbox.enqueue(activityStoreDelUpdates, CouponStoreTagSqlTypeEnum.DELETE);
            }
            redisForAppletAd.delAllStore(BusinessContextHolder.getBusinessId());
            storeBackgroundCacheService.refreshStoresAfterCommit(storeIds);
        }
    }

    private void refreshStoreTagRedis(Long storeId, List<Long> oldTagIds, List<Long> newTagIds) {
        List<Long> normalizedOldTagIds = normalizeTagIds(oldTagIds);
        if (!CollectionUtils.isEmpty(normalizedOldTagIds)) {
            appletPageManagementService.delRedisByStoreTags(storeId, normalizedOldTagIds, BusinessContextHolder.getBusinessId());
        }
        List<Long> normalizedNewTagIds = normalizeTagIds(newTagIds);
        if (!CollectionUtils.isEmpty(normalizedNewTagIds)) {
            appletPageManagementService.addRedisByStoreTags(storeId, normalizedNewTagIds, BusinessContextHolder.getBusinessId());
        }
    }

    private List<Long> getStoreTagIds(Long storeId) {
        return systemStoreTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                        .eq(SystemStoreTagDO::getStoreId, storeId))
                .stream()
                .map(SystemStoreTagDO::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return new ArrayList<>();
        }
        return tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /** 门店标签提交后使缓存失效；新增和删除都由系统标签表重新回填。 */
    private void refreshActivityTagCache(Long storeId, List<Long> addedTagIds, List<Long> removedTagIds) {
        if (storeId == null) return;
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        Runnable refresh = () -> {
            try { storeActivityTagCache.invalidate(businessId, storeId); }
            catch (RuntimeException ex) { log.warn("门店标签缓存失效失败，等待活动同步时刷新，storeId={}", storeId); }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() { refresh.run(); }
                    });
        } else refresh.run();
    }

    private void deleteActivityTagCache(Long storeId) {
        refreshActivityTagCache(storeId, List.of(), List.of());
    }

    /** 按系统标签表重建门店标签缓存。 */
    @Override
    public Boolean initStoreTagCache() {
        List<SystemStoreInfoDO> stores = systemStoreInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getBusinessId));
        // 全量预热也使用带版本校验的写入，避免旧快照覆盖新标签。
        for (SystemStoreInfoDO store : stores) {
            if (store.getStoreId() != null && store.getBusinessId() != null)
                storeActivityTagCache.refresh(store.getBusinessId(), store.getStoreId());
        }
        return true;
    }

    /** 判断门店是否命中任一活动标签；缓存未就绪时受控回源。 */
    @Override
    public Boolean matchStoreTagCache(Long businessId, Long storeId, List<Long> tagIds) {
        if (businessId == null || storeId == null || CollectionUtils.isEmpty(tagIds)) {
            return false;
        }
        List<Long> distinctTagIds = tagIds.stream()
                .filter(tagId -> tagId != null && tagId > 0)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctTagIds)) {
            return false;
        }
        // SMISMEMBER 一次往返判断多个标签是否命中；StringRedisTemplate 的序列化器只接受 String，需先转换
        String key = StoreActivityTagCache.key(businessId, storeId);
        storeActivityTagCache.ensure(businessId, storeId);
        Object[] tagIdValues = distinctTagIds.stream()
                .map(String::valueOf)
                .toArray();
        Map<Object, Boolean> results = stringRedisTemplate.opsForSet().isMember(key, tagIdValues);
        return CollUtil.isNotEmpty(results) && results.containsValue(Boolean.TRUE);
    }

    @Override
    public List<StoreResVO> selectStoreByTag(StoreTagReqVO storeTagReqVO) {

        List<StoreSimpleResVO> list = new ArrayList<>();

        Long userId = WebFrameworkUtils.getLoginUserId();
        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoMapper.selectStoreOrgIds(storeIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        List<OrgDO> orgListTwo = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(orgIds)) {
            //本级组织
            orgListTwo = orgMapper.selectByIds(orgIds);
        }


        Set<OrgDO> allOrgSetTwo = new HashSet<>();
        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
            allOrgSetTwo.addAll(childOrgList);
        }
        allOrgSet.addAll(orgList);
        allOrgSetTwo.addAll(orgListTwo);
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());
        Long businessId = BusinessContextHolder.getBusinessId();
        Map<Long, List<SystemStoreInfoDO>> storeMap = new HashMap<>();
        // 组织节点店数 map
        if (!org.springframework.util.StringUtils.isEmpty(storeTagReqVO.getStatus()) && storeTagReqVO.getStatus().equals(1)) {
            storeMap = systemStoreInfoService.selectOperationStoreByOrgIds(collect).stream()
                    .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));

//            if (ObjectUtil.isNotEmpty(storeIds)) {
//                List<SystemStoreInfoDO> listData = new ArrayList<>();
//                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//                lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
//                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
//                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
//                if (ObjectUtil.isNotEmpty(systemStoreInfoDOS)) {
//                    listData.addAll(systemStoreInfoDOS);
//                }
//                if (ObjectUtil.isNotEmpty(listData)) {
//                    Map<Long, List<SystemStoreInfoDO>> listMap = listData.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
//                    for (Long aLong : listMap.keySet()) {
//                        if (!orgIds.contains(aLong)) {
//                            storeMap.put(aLong, listMap.get(aLong));
//                        }
//                    }
//                }
//            }
            if (ObjectUtil.isNotEmpty(storeIds)) {
                Set<Long> longSet = new HashSet<>();
                for (Long storeId : storeIds) {
                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
                    SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                    if (ObjectUtil.isNotEmpty(systemStoreInfoDO)) {
                        Long orgId = systemStoreInfoDO.getOrgId();
                        if (ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isNotEmpty(allOrgSetTwo)) {
                            List<Long> collectList = allOrgSetTwo.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                            if (!collectList.contains(orgId)) {
                                longSet.add(storeId);
                            }
                        } else if (ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isEmpty(allOrgSetTwo)) {
                            longSet.add(storeId);
                        }
                    }

                }

                if (ObjectUtil.isNotEmpty(longSet)) {
                    List<SystemStoreInfoDO> listStores = new ArrayList<>();
                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    if (ObjectUtil.isNotEmpty(systemStoreInfoDOS)) {
                        listStores.addAll(systemStoreInfoDOS);
                    }
                    if (ObjectUtil.isNotEmpty(listStores)) {
                        Map<Long, List<SystemStoreInfoDO>> listMap = listStores.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                        for (Long aLong : listMap.keySet()) {
                            if (!orgIds.contains(aLong)) {
                                storeMap.put(aLong, listMap.get(aLong));
                            }
                        }
                    }
                }

            }
        } else {

            Set<Long> longs = new HashSet<>();
            LambdaQueryWrapper<OrgDO> orgDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orgDOLambdaQueryWrapper.eq(OrgDO::getStatus, 1);
//            orgDOLambdaQueryWrapper.eq(OrgDO::getBusinessId,businessId);
            List<OrgDO> orgDOList = orgMapper.selectList(orgDOLambdaQueryWrapper);
            if (!ObjectUtil.isEmpty(orgDOList)) {
                longs = orgDOList.stream().map(mm -> mm.getId()).collect(Collectors.toSet());
            }

            storeMap = systemStoreInfoService.selectStoreByOrgIdsTwo(longs).stream()
                    .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
            if (ObjectUtil.isNotEmpty(storeIds)) {
                Set<Long> longSet = new HashSet<>();
                for (Long storeId : storeIds) {
                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
                    SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                    if (ObjectUtil.isNotEmpty(systemStoreInfoDO)) {
                        Long orgId = systemStoreInfoDO.getOrgId();
                        if (ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isNotEmpty(allOrgSetTwo)) {
                            List<Long> collectList = allOrgSetTwo.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                            if (!collectList.contains(orgId)) {
                                longSet.add(storeId);
                            }
                        } else if (ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isEmpty(allOrgSetTwo)) {
                            longSet.add(storeId);
                        }
                    }

                }

                if (ObjectUtil.isNotEmpty(longSet)) {
                    List<SystemStoreInfoDO> listStores = new ArrayList<>();
                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    if (ObjectUtil.isNotEmpty(systemStoreInfoDOS)) {
                        listStores.addAll(systemStoreInfoDOS);
                    }
                    if (ObjectUtil.isNotEmpty(listStores)) {
                        Map<Long, List<SystemStoreInfoDO>> listMap = listStores.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                        for (Long aLong : listMap.keySet()) {
                            if (!orgIds.contains(aLong)) {
                                storeMap.put(aLong, listMap.get(aLong));
                            }
                        }
                    }
                }

            }
        }

        // 加入上级节点
        for (Long l : storeMap.keySet()) {

            List<SystemStoreInfoDO> stores = storeMap.get(l);
            if (ObjectUtil.isNotEmpty(stores)) {
                for (SystemStoreInfoDO store : stores) {
                    StoreSimpleResVO storeSimpleResVO = new StoreSimpleResVO();
                    storeSimpleResVO.setStoreName(store.getStoreName());
                    storeSimpleResVO.setStoreId(store.getStoreId());
                    storeSimpleResVO.setIdentificationTemplate(store.getIdentificationTemplate());
                    list.add(storeSimpleResVO);
                }
            }
        }
        List<StoreResVO> voList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(list)) {
            if (storeTagReqVO.getType().equals(1)) {
//                for (StoreSimpleResVO storeSimpleResVO : list) {
//                    StoreResVO storeResVO = new StoreResVO();
//                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
//                    wrapper.eq(SystemStoreTagDO::getStoreId,storeSimpleResVO.getStoreId());
//                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);
//                    List<Long> tagIds = storeTagReqVO.getTagIds();
//                    // 检查是否存在交集
//                    boolean hasIntersection = systemStoreTagDOS.stream()
//                            .map(SystemStoreTagDO::getTagId)
//                            .anyMatch(tagIds::contains);
//
//                    if(hasIntersection){
//                        BeanUtils.copyProperties(storeSimpleResVO,storeResVO);
//                        voList.add(storeResVO);
//                    }
//
//                }
                // 1. 收集所有storeId和需要匹配的tagIds
                List<Long> storeIdList = list.stream()
                        .map(StoreSimpleResVO::getStoreId)
                        .distinct()  // 去重
                        .collect(Collectors.toList());
                List<Long> requiredTagIds = storeTagReqVO.getTagIds();

                // 2. 批量查询所有相关标签记录（只查询需要的storeId和tagId组合）
                LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                if (!requiredTagIds.isEmpty()) {
                    wrapper.in(SystemStoreTagDO::getTagId, requiredTagIds); // 只查询可能匹配的tagId
                }
                List<SystemStoreTagDO> allStoreTags = systemStoreTagMapper.selectList(wrapper);

                // 3. 构建包含匹配tag的storeId集合（使用Set提高contains性能）
                Set<Long> matchedStoreIds = allStoreTags.stream()
                        .map(SystemStoreTagDO::getStoreId)
                        .collect(Collectors.toSet());

                // 4. 处理结果
                voList = list.stream()
                        .filter(store -> matchedStoreIds.contains(store.getStoreId()))
                        .map(store -> {
                            StoreResVO vo = new StoreResVO();
                            BeanUtils.copyProperties(store, vo);
                            return vo;
                        })
                        .collect(Collectors.toList());
            } else if (storeTagReqVO.getType().equals(2)) {
//                for (StoreSimpleResVO storeSimpleResVO : list) {
//                    StoreResVO storeResVO = new StoreResVO();
//                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
//                    wrapper.eq(SystemStoreTagDO::getStoreId,storeSimpleResVO.getStoreId());
//                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);
//                    List<Long> tagIds = storeTagReqVO.getTagIds();
//                    // 提取 store 的所有 tagId
//                    Set<Long> storeTagIds = systemStoreTagDOS.stream()
//                            .map(SystemStoreTagDO::getTagId)
//                            .collect(Collectors.toSet());
//
//                    boolean allMatch = storeTagIds.containsAll(tagIds);
//                    if (allMatch) {
//                        BeanUtils.copyProperties(storeSimpleResVO, storeResVO);
//                        voList.add(storeResVO);
//                    }
//                }

                // 1. 收集所有storeId和需要匹配的tagIds
                List<Long> storeIdList = list.stream()
                        .map(StoreSimpleResVO::getStoreId)
                        .collect(Collectors.toList());
                List<Long> requiredTagIds = storeTagReqVO.getTagIds(); // 需要匹配的tagIds

                // 2. 批量查询所有相关标签记录
                LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                if (!requiredTagIds.isEmpty()) {
                    wrapper.in(SystemStoreTagDO::getTagId, requiredTagIds); // 只查询需要的tagIds
                }
                List<SystemStoreTagDO> allStoreTags = systemStoreTagMapper.selectList(wrapper);

                // 3. 按storeId分组，并收集每个store的tagIds
                Map<Long, Set<Long>> tagsByStoreId = allStoreTags.stream()
                        .collect(Collectors.groupingBy(
                                SystemStoreTagDO::getStoreId,
                                Collectors.mapping(SystemStoreTagDO::getTagId, Collectors.toSet())
                        ));

                // 4. 处理结果：筛选出包含所有requiredTagIds的store
                voList = list.stream()
                        .filter(store -> {
                            Set<Long> storeTagIds = tagsByStoreId.getOrDefault(store.getStoreId(), Collections.emptySet());
                            return storeTagIds.containsAll(requiredTagIds);
                        })
                        .map(store -> {
                            StoreResVO vo = new StoreResVO();
                            BeanUtils.copyProperties(store, vo);
                            return vo;
                        })
                        .collect(Collectors.toList());
            } else if (storeTagReqVO.getType().equals(3)) {
//                for (StoreSimpleResVO storeSimpleResVO : list) {
//                    StoreResVO storeResVO = new StoreResVO();
//                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
//                    wrapper.eq(SystemStoreTagDO::getStoreId,storeSimpleResVO.getStoreId());
//                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);
//
//                    if(ObjectUtil.isEmpty(systemStoreTagDOS)){
//                        BeanUtils.copyProperties(storeSimpleResVO,storeResVO);
//                        voList.add(storeResVO);
//                    }
//
//                }

                List<Long> storeIdList = list.stream()
                        .map(StoreSimpleResVO::getStoreId)
                        .collect(Collectors.toList());


                LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                        .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                voList = list.stream()
                        .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                        .map(store -> {
                            StoreResVO vo = new StoreResVO();
                            BeanUtils.copyProperties(store, vo);
                            return vo;
                        })
                        .collect(Collectors.toList());

            }
        }

        return voList;
    }

    /**
     * 根据ids查询门店
     */
    @Override
    public List<StoreSimpleResVO> getStoreSimpleResDtoList(List<Long> storeIds) {
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoMapper.selectBatchIds(storeIds);
        return BeanUtils.toBean(storeInfoDOS, StoreSimpleResVO.class);
    }

    @Override
    public void exportStoreList(StorePageReqVO pageReqVO, HttpServletRequest request, HttpServletResponse response) {
        normalizeStorePageTagFilter(pageReqVO);
        Long businessId = BusinessContextHolder.getBusinessId();
        Long userId = WebFrameworkUtils.getLoginUserId();
        //查询deptIDs
        Set<Long> orgIds = new HashSet<>();
        if (pageReqVO.getOrgId() != null) {
            orgIds = orgMapper.getChildIdListByStore(pageReqVO.getOrgId());
            if (orgIds.isEmpty()) {
                Set<Long> businessIdList = new HashSet<>();
                businessIdList.add(pageReqVO.getOrgId());
                List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
                orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
            }
            orgIds.add(pageReqVO.getOrgId());
            pageReqVO.setOrgIds(orgIds);
        }
        Page<StoreResVO> page = new Page<>(pageReqVO.getPageNo(), 500);

        //异步分页导出
        storeRespVOExcelActionService.exportAsyncExcel(StoreResVO.class, page, param -> selectPageList(page, pageReqVO, businessId, userId), "门店列表");
    }

    public List<StoreResVO> selectPageList(Page<StoreResVO> page, StorePageReqVO pageReqVO, Long businessId, Long userId) {
        IPage<StoreResVO> pageList = systemStoreInfoMapper.selectPageList(page, pageReqVO, businessId, userId);
        List<StoreResVO> records = pageList.getRecords();
        Set<Long> orgIds = new HashSet<>();
        Set<Long> storeIds = new HashSet<>();
        Set<Long> requiredTagIds = new HashSet<>();
        records.forEach(storeResVO -> {
            storeResVO.setStoreIdStr(storeResVO.getStoreId().toString());
            storeResVO.setStoreStatusName(StoreStatusEnum.getMessageByCode(storeResVO.getStoreStatus()));
            storeResVO.setOpenStatusName(StoreOpenStatusEnum.getMessageByCode(storeResVO.getOpenStatus()));
            String storeLeader = Optional.ofNullable(storeResVO.getStoreLeader()).orElse("");
            String storeLeaderPhone = Optional.ofNullable(storeResVO.getStoreLeaderPhone()).orElse("");

            String result = storeLeader;
            if (!storeLeaderPhone.isEmpty()) {
                result += " / " + storeLeaderPhone;
            }
            storeResVO.setStoreLeader(result);
            if (storeResVO.getOrgId() != null) {
                orgIds.add(storeResVO.getOrgId());
            }
            storeIds.add(storeResVO.getStoreId());

        });

        Map<Long, OrgDO> orgMap = new HashMap<>();
        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = new HashMap<>();
        Map<Long, String> tagsNameByStoreId = new HashMap<>();
        Map<Long, SystemStoreFranchiseeInfoDO> franchiseeByStoreId = new HashMap<>();
        if (!CollectionUtils.isEmpty(storeIds)) {
            tagsByStoreId = systemStoreTagMapper.selectList(new LambdaQueryWrapper<SystemStoreTagDO>().eq(SystemStoreTagDO::getBusinessId, businessId).in(SystemStoreTagDO::getStoreId, storeIds))
                    .stream().collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));
            // 每页一次批量读取加盟商信息，避免导出时逐门店查询。
            franchiseeByStoreId = systemStoreFranchiseeInfoMapper.selectList(
                            new LambdaQueryWrapperX<SystemStoreFranchiseeInfoDO>()
                                    .eq(SystemStoreFranchiseeInfoDO::getBusinessId, businessId)
                                    .in(SystemStoreFranchiseeInfoDO::getStoreId, storeIds))
                    .stream().collect(Collectors.toMap(SystemStoreFranchiseeInfoDO::getStoreId,
                            info -> info, (first, ignored) -> first));
        }
        // 如果有门店标签关联数据，继续处理
        if (!CollectionUtils.isEmpty(tagsByStoreId)) {
            // 2. 提取所有关联的标签ID（去重）
            Set<Long> allTagIds = tagsByStoreId.values().stream()
                    .flatMap(List::stream) // 扁平化所有门店的标签关系对象
                    .map(SystemStoreTagDO::getTagId) // 提取标签ID
                    .collect(Collectors.toSet());

            // 3. 查询所有标签ID对应的标签名称（假设标签信息存在SystemTagDO中）
            Map<Long, String> tagIdToNameMap;
            if (!allTagIds.isEmpty()) {
                tagIdToNameMap = tagValueMapper.selectList( // 注意：这里需要用标签表的mapper
                                new LambdaQueryWrapper<TagValueDO>()
                                        .in(TagValueDO::getId, allTagIds)
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                TagValueDO::getId,    // 键：标签ID
                                TagValueDO::getName // 值：标签名称
                        ));
            } else {
                tagIdToNameMap = new HashMap<>();
            }

            // 4. 为每个门店拼接标签名称
            tagsNameByStoreId = tagsByStoreId.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, // 键：门店ID
                            entry -> entry.getValue().stream()
                                    // 将每个标签ID转换为名称（不存在则用空字符串）
                                    .map(tagRel -> tagIdToNameMap.getOrDefault(tagRel.getTagId(), ""))
                                    // 用逗号拼接所有标签名称
                                    .collect(Collectors.joining(","))
                    ));
        }
        // 批量查询
        if (!CollectionUtils.isEmpty(orgIds)) {
            orgMap = orgMapper.selectBatchIds(orgIds).stream()
                    .collect(Collectors.toMap(OrgDO::getId, org -> org));
        }
        for (StoreResVO item : records) {
            OrgDO org = orgMap.get(item.getOrgId());
            if (org != null) {
                item.setOrgName(org.getName());
                AdminUserDO adminUserDO = findResponsibleBusinessUser(org.getId());
                findResponsibleBusinessUser(org.getId());
                if (adminUserDO != null) {
                    item.setOrgName(org.getName() + "/" + adminUserDO.getNickname() + " " + adminUserDO.getMobile());
                }
            }
            item.setTagName(tagsNameByStoreId.get(item.getStoreId()));
            SystemStoreFranchiseeInfoDO franchiseeInfo = franchiseeByStoreId.get(item.getStoreId());
            if (franchiseeInfo != null) {
                item.setFranchiseeName(franchiseeInfo.getFranchiseeName());
                item.setFranchiseeMobile(franchiseeInfo.getFranchiseeMobile());
                item.setIdCardNo(franchiseeInfo.getIdCardNo());
                item.setBankName(franchiseeInfo.getBankName());
                item.setBankProvince(franchiseeInfo.getBankProvince());
                item.setBankCity(franchiseeInfo.getBankCity());
                item.setBankCardNo(franchiseeInfo.getBankCardNo());
            }
        }
        return records;
    }

    /**
     * 根据完整城市名称查询小程序门店列表。
     *
     * <p>门店静态数据优先读取 Redis，优惠券、秒杀、灰度、营业状态、距离和排序逻辑保持不变。</p>
     */
    @Override
    public List<StoreWecomConfigResVO> storeListByCity(StoreWecomConfigReqVO dto) {
        if (dto == null || !org.springframework.util.StringUtils.hasText(dto.getCityName())) {
            return Lists.newArrayList();
        }
        List<Long> storeIdList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(dto.getCouponId())) {
            CommonResult<GoodCouponDataDTO> byIdCoupon = goodCouponApi.getByIdCoupon(dto.getCouponId());
            GoodCouponDataDTO checkedData = byIdCoupon.getData();
            if (ObjectUtil.isNotEmpty(checkedData)) {
                storeIdList = checkedData.getStoreIdList();
            } else {
                return new ArrayList<>();
            }
        }
        List<Long> storeIDs = new ArrayList<>();
        //查询秒杀门店
        if (ObjectUtil.isNotEmpty(dto.getActivityId())) {
            storeIDs = seckillApi.setectStoreByActivityId(dto.getActivityId());
        }
        List<StoreCityListCacheValue> cachedStores = storeCityListCacheService
                .getStoresByCity(dto.getCityName().trim());
        if (CollectionUtils.isEmpty(cachedStores)) {
            return Lists.newArrayList();
        }

        Set<Long> couponStoreIds = storeIdList == null ? Collections.emptySet() : new HashSet<>(storeIdList);
        Set<Long> activityStoreIds = storeIDs == null ? Collections.emptySet() : new HashSet<>(storeIDs);
        // 保留原接口的灰度门店、名称、优惠券和秒杀门店过滤规则。
        ListIterator<StoreCityListCacheValue> iterator = cachedStores.listIterator();
        while (iterator.hasNext()) {
            StoreCityListCacheValue next = iterator.next();
            Long storeId = next.getStoreId();
            Set<String> memberIdsByStore = grayStoreConfig.getMemberIdsByStore(storeId);
            boolean grayStoreInvisible = CollectionUtils.isNotEmpty(memberIdsByStore)
                    && !memberIdsByStore.contains(dto.getMemberId());
            boolean nameMismatch = org.springframework.util.StringUtils.hasText(dto.getStoreName())
                    && (next.getStoreName() == null || !next.getStoreName().toLowerCase(Locale.ROOT)
                    .contains(dto.getStoreName().toLowerCase(Locale.ROOT)));
            boolean couponMismatch = !couponStoreIds.isEmpty() && !couponStoreIds.contains(storeId);
            boolean activityMismatch = ObjectUtil.isNotEmpty(dto.getActivityId())
                    && !activityStoreIds.isEmpty() && !activityStoreIds.contains(storeId);
            if (grayStoreInvisible || nameMismatch || couponMismatch || activityMismatch) {
                iterator.remove();
            }
        }
        if (cachedStores.isEmpty()) {
            return Lists.newArrayList();
        }
        Map<Long, String> backgroundImages = storeBackgroundCacheService.getBackgroundImages(
                cachedStores.stream().map(StoreCityListCacheValue::getStoreId).toList());
        return cachedStores.stream().map(store -> toStoreListResponse(store, dto, backgroundImages.get(store.getStoreId())))
                .sorted(Comparator.comparing((StoreWecomConfigResVO vo) ->
                                (vo.getStoreId() != null && !vo.getStoreId().equals(dto.getStoreId())) ? 1 : 0)
                        .thenComparing(StoreWecomConfigResVO::getDistance))
                .collect(Collectors.toList());
    }

    /**
     * 将门店静态缓存转换为接口响应，并实时计算营业状态和距离。
     */
    private StoreWecomConfigResVO toStoreListResponse(StoreCityListCacheValue store,
                                                       StoreWecomConfigReqVO dto,
                                                       String backgroundImage) {
        StoreWecomConfigResVO vo = new StoreWecomConfigResVO();
        vo.setStoreId(store.getStoreId());
        vo.setStoreName(store.getStoreName());
        vo.setStoreAddress(store.getStoreAddress());
        vo.setLongitude(store.getLongitude());
        vo.setLatitude(store.getLatitude());
        vo.setQrCode(store.getQrCode());
        vo.setQrType(store.getQrType());
        vo.setOpenStatus(store.getOpenStatus());
        if (Objects.equals(store.getOpenStatus(), 0)) {
            vo.setOpenStatus(isWithinBusinessHours(store.getStoreHours()));
        }
        vo.setStoreTakeaway(store.getStoreTakeaway());
        vo.setStoreHours(store.getStoreHours());
        vo.setStoreAnnouncement(store.getStoreAnnouncement());
        vo.setStorePhone(store.getStorePhone());
        vo.setAdditionaaCosts(store.getAdditionaaCosts());
        vo.setMinimumDeliveryFee(store.getMinimumDeliveryFee());
        vo.setPackCosts(store.getPackCosts());
        vo.setMinimumPackageFee(store.getMinimumPackageFee());
        vo.setStoreCalculationType(store.getStoreCalculationType());
        vo.setPackDeliveryCosts(store.getPackDeliveryCosts());
        vo.setMinimumDeliveryPackFee(store.getMinimumDeliveryPackFee());
        vo.setStoreDeliveryCalculationType(store.getStoreDeliveryCalculationType());
        vo.setStorePayType(store.getStorePayType());
        vo.setStoreWithoutPayment(store.getStoreWithoutPayment());
        vo.setCampusDeliveryStatus(store.getCampusDeliveryStatus());
        vo.setCampusDeliverySubsidy(store.getCampusDeliverySubsidy());
        vo.setCampusMinimumDeliveryFee(store.getCampusMinimumDeliveryFee());
        vo.setCampusDeliveryFee(store.getCampusDeliveryFee());
        vo.setCampusDeliveryCalculationType(store.getCampusDeliveryCalculationType());
        vo.setTiktokId(store.getTiktokId());
        vo.setDeliveryTime(store.getDeliveryTime());
        vo.setIsPrompt(store.getIsPrompt());
        vo.setPromptText(store.getPromptText());
        vo.setCityName(store.getCityName());
        vo.setStoreBackgroundImage(backgroundImage);
        vo.setDistance(calculateStoreDistance(dto.getLongitude(), dto.getLatitude(),
                store.getLongitude(), store.getLatitude()));
        if (Objects.equals(store.getOpenStatus(), 0)) {
            vo.setOpenStatus(getOpenStatus(store.getOpenStatus(), store.getStoreHours()));
        }
        return vo;
    }

    /**
     * 根据当前登入人获取负责区域的门店或店长的门店
     */
    @Override
    public List<StoreSimpleResVO> storeListByUser() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        // 1. 获取用户关联的所有组织
        Set<Long> userOrgIds = getOrgIdsByUserId(userId);


        // 2. 构建组织层级关系（包含所有用户关联组织及其上下级）
        OrganizationHierarchy hierarchy = buildOrganizationHierarchy(userOrgIds);

        Set<Long> userStoreIds = storeUserMapper.selectList(
                new LambdaQueryWrapperX<StoreUserDO>()
                        .eq(StoreUserDO::getUserId, userId)
                        .eq(StoreUserDO::getDeleted, 0)).stream().map(StoreUserDO::getStoreId).collect(Collectors.toSet());
        // 3. 获取门店数据

        List<SystemStoreInfoDO> storeInfoDOS = queryStoresByOrgs(hierarchy.getAllOrgIds(), userStoreIds);

        // 4. 获取用户与门店的关系（用于优先级最高的判断）
        Map<Long, Integer> storeVisibilityMap = getUserStoreVisibilityMap(userId);

        // 5. 获取组织可见性配置
        Map<Long, Integer> orgVisibilityMap = getUserOrgVisibilityMap(userId);
        // 6. 转换并设置可见性
        List<StoreSimpleResVO> result = BeanUtils.toBean(storeInfoDOS, StoreSimpleResVO.class);
        Map<Long, SystemStoreExpensesDO> campusDeliveryExpenseMap = getStoreExpensesMap(
                storeInfoDOS.stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toSet()),
                STORE_EXPENSE_TYPE_CAMPUS_DELIVERY);
        for (StoreSimpleResVO vo : result) {
            Long storeId = vo.getStoreId();
            Long orgId = vo.getOrgId();
            fillCampusDeliveryFields(vo, campusDeliveryExpenseMap.get(storeId));

            // 优先使用门店直接关联的可见性设置
            if (storeVisibilityMap.containsKey(storeId)) {
                vo.setVisible(storeVisibilityMap.get(storeId));
                continue;
            }
            // 根据组织层级结构确定可见性
            Integer visibility = hierarchy.getEffectiveVisibility(orgId, orgVisibilityMap);
            vo.setVisible(visibility != null ? visibility : 0);
        }
        Map<Long, String> backgroundImages = storeBackgroundCacheService.getBackgroundImages(
                result.stream().map(StoreSimpleResVO::getStoreId).toList());
        result.forEach(item -> item.setStoreBackgroundImage(backgroundImages.get(item.getStoreId())));

        return result;
    }

    // 构建组织层级结构
    private OrganizationHierarchy buildOrganizationHierarchy(Set<Long> userOrgIds) {
        // 1. 获取所有用户关联组织及其子组织
        if (userOrgIds != null && !userOrgIds.isEmpty()) {

            Set<Long> allOrgIds = new HashSet<>(userOrgIds);
            allOrgIds.addAll(getChildOrgIds(userOrgIds));
            // 2. 查询所有组织信息（包括父级关系）
            Map<Long, OrgDO> orgMap = batchGetOrgMap(allOrgIds);
            // 3. 构建层级结构
            return new OrganizationHierarchy(orgMap, userOrgIds);
        }
        return new OrganizationHierarchy(new HashMap<>(), new HashSet<>());
    }

    private Map<Long, Integer> getUserStoreVisibilityMap(Long userId) {
        return storeUserMapper.selectList(
                        new LambdaQueryWrapperX<StoreUserDO>()
                                .eq(StoreUserDO::getUserId, userId)
                                .eq(StoreUserDO::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(
                        StoreUserDO::getStoreId,
                        StoreUserDO::getVisible,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 获取用户组织可见性映射（一次查询）
     */
    private Map<Long, Integer> getUserOrgVisibilityMap(Long userId) {
        return userOrgMapper.selectList(
                        new LambdaQueryWrapperX<OrgUserDO>()
                                .eq(OrgUserDO::getUserId, userId)
                                .eq(OrgUserDO::getDeleted, 0)
                )
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OrgUserDO::getOrgId,
                        OrgUserDO::getVisible,
                        (existing, replacement) -> existing,
                        HashMap::new // 用HashMap更高效，无需保持插入顺序
                ));
    }

    /**
     * 批量查询组织信息（一次查库，避免循环查询）
     */
    private Map<Long, OrgDO> batchGetOrgMap(Set<Long> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return Collections.emptyMap();
        }
        List<OrgDO> orgs = orgMapper.selectBatchIds(orgIds);
        return orgs.stream()
                .filter(Objects::nonNull) // 过滤无效组织
                .collect(Collectors.toMap(
                        OrgDO::getId,
                        org -> org,
                        (existing, replacement) -> existing
                ));
    }

    // 查询指定组织下的所有门店
    private List<SystemStoreInfoDO> queryStoresByOrgs(Set<Long> orgIds, Set<Long> storeIds) {
        if(CollectionUtil.isEmpty(orgIds)&&CollectionUtil.isEmpty(storeIds)){
            return new ArrayList<>();
        }
        LambdaQueryWrapperX<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper
                .eq(SystemStoreInfoDO::getStoreSource, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0)
                .eqIfPresent(SystemStoreInfoDO::getStoreStatus, StoreStatusEnum.OPEN.getStatus())
                .inIfPresent(SystemStoreInfoDO::getOrgId, orgIds);
        if (orgIds != null && !orgIds.isEmpty() && storeIds != null && !storeIds.isEmpty()) {
            queryWrapper.or();
        }
        if (storeIds != null && !storeIds.isEmpty()) {

            queryWrapper.inIfPresent(SystemStoreInfoDO::getStoreId, storeIds);
        }
        return systemStoreInfoMapper.selectList(queryWrapper).stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SystemStoreInfoDO::getStoreId))),
                        ArrayList::new
                ));
    }

    // 组织层级结构类
    private static class OrganizationHierarchy {
        // 组织ID -> 组织对象
        private final Map<Long, OrgDO> orgMap;
        // 用户直接关联的组织
        private final Set<Long> userOrgIds;
        // 组织ID -> 最近的用户关联父组织
        private final Map<Long, Long> orgToUserParentMap = new HashMap<>();

        public OrganizationHierarchy(Map<Long, OrgDO> orgMap, Set<Long> userOrgIds) {
            this.orgMap = orgMap;
            this.userOrgIds = userOrgIds;
            buildOrgToUserParentMap();
        }

        // 构建组织到用户关联父组织的映射
        private void buildOrgToUserParentMap() {
            for (Long orgId : orgMap.keySet()) {
                // 查找该组织最近的用户关联父组织
                Long userParentOrgId = findNearestUserParentOrg(orgId);
                if (userParentOrgId != null) {
                    orgToUserParentMap.put(orgId, userParentOrgId);
                }
            }
        }

        // 查找组织最近的用户关联父组织
        private Long findNearestUserParentOrg(Long orgId) {
            Long currentId = orgId;
            while (currentId != null) {
                if (userOrgIds.contains(currentId)) {
                    return currentId; // 找到用户关联的组织
                }
                // 向上查找父组织
                OrgDO org = orgMap.get(currentId);
                if (org == null || org.getParentId() == null || org.getParentId() == 0) {
                    break;
                }
                currentId = org.getParentId();
            }
            return null; // 没有找到用户关联的父组织
        }

        // 获取组织的有效可见性（根据最近的用户关联父组织）
        public Integer getEffectiveVisibility(Long orgId, Map<Long, Integer> orgVisibilityMap) {
            // 获取该组织最近的用户关联父组织
            Long userParentOrgId = orgToUserParentMap.get(orgId);
            if (userParentOrgId == null) {
                return null; // 没有找到用户关联的父组织
            }

            // 返回该用户关联父组织的可见性设置
            return orgVisibilityMap.get(userParentOrgId);
        }

        // 获取所有组织ID
        public Set<Long> getAllOrgIds() {
            return orgMap.keySet();
        }
    }

    @Override
    public PrintAllInfoVo selectByStorePrinterInfo(PrinterTomplateReqVO printerTomplateReqVO) {
        PrintAllInfoVo printAllInfoVo = new PrintAllInfoVo();
        if (printerTomplateReqVO != null) {
            if (!org.springframework.util.StringUtils.isEmpty(printerTomplateReqVO.getStoreId())) {
                Long storeId = printerTomplateReqVO.getStoreId();
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);

//                if(systemStoreInfoDO==null){
//
//                    if(org.springframework.util.StringUtils.isEmpty(systemStoreInfoDO.getPrinterTemplate())){
//                        return printAllInfoVo;
//                    }
//                    return printAllInfoVo;
//                }else {
//                    return printAllInfoVo;
//                }

                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    PrintConfigVO printConfigVO = objectMapper.readValue(systemStoreInfoDO.getPrinterTemplate(), new TypeReference<PrintConfigVO>() {
                    });
                    if (ObjectUtil.isNotEmpty(printerTomplateReqVO.getPrinterType())) {
                        if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.STORE.getStatus())) {
                            PrintInfoStoreVO store = printConfigVO.getStore();
                            BeanUtils.copyProperties(store, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.MEMBER.getStatus())) {
                            PrintInfoMemberVO member = printConfigVO.getMember();
                            BeanUtils.copyProperties(member, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.KITCHEN.getStatus())) {
                            PrintInfoKitchenVO kitchen = printConfigVO.getKitchen();
                            BeanUtils.copyProperties(kitchen, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.DELIVERY.getStatus())) {
                            PrintInfoDeliveryVO delivery = printConfigVO.getDelivery();
                            BeanUtils.copyProperties(delivery, printAllInfoVo);
                            return printAllInfoVo;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                Long storeId = printerTomplateReqVO.getStoreId();
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);

                if (org.springframework.util.StringUtils.isEmpty(systemStoreInfoDO.getPrinterTemplate())) {

                    return null;
                }

                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    PrintConfigVO printConfigVO = objectMapper.readValue(systemStoreInfoDO.getPrinterTemplate(), new TypeReference<PrintConfigVO>() {
                    });
                    if (ObjectUtil.isNotEmpty(printerTomplateReqVO.getPrinterType())) {
                        if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.STORE.getStatus())) {
                            PrintInfoStoreVO store = printConfigVO.getStore();
                            BeanUtils.copyProperties(store, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.MEMBER.getStatus())) {
                            PrintInfoMemberVO member = printConfigVO.getMember();
                            BeanUtils.copyProperties(member, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.KITCHEN.getStatus())) {
                            PrintInfoKitchenVO kitchen = printConfigVO.getKitchen();
                            BeanUtils.copyProperties(kitchen, printAllInfoVo);
                            return printAllInfoVo;
                        } else if (printerTomplateReqVO.getPrinterType().equals(PrinterTypeEnum.DELIVERY.getStatus())) {
                            PrintInfoDeliveryVO delivery = printConfigVO.getDelivery();
                            BeanUtils.copyProperties(delivery, printAllInfoVo);
                            return printAllInfoVo;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Set<Long> getOrgIdsByUserId(Long userId) {
        return userOrgMapper.selectList(
                new LambdaQueryWrapperX<OrgUserDO>()
                        .eq(OrgUserDO::getUserId, userId)
                        .eq(OrgUserDO::getDeleted, 0)
        ).stream().map(OrgUserDO::getOrgId).collect(Collectors.toSet());
    }

    private Set<Long> getChildOrgIds(Set<Long> orgIds) {
        if (orgIds != null && !orgIds.isEmpty()) {
            return orgMapper.selectChildByIds(orgIds).stream().map(OrgDO::getId).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private Set<Long> getStoreIdsByUserId(Long userId) {
        return storeUserMapper.selectList(
                new LambdaQueryWrapperX<StoreUserDO>()
                        .eq(StoreUserDO::getUserId, userId).eq(StoreUserDO::getDeleted, 0)
        ).stream().map(StoreUserDO::getStoreId).collect(Collectors.toSet());
    }

    /**
     * 经纬度转换为弧度
     *
     * @param d
     * @return
     */
    private static double rab(double d) {
        return d * Math.PI / 180.0;
    }

    private double calculateStoreDistance(Double userLongitude, Double userLatitude, Double storeLongitude, Double storeLatitude) {
        if (userLongitude == null || userLatitude == null || storeLongitude == null || storeLatitude == null || userLongitude == 0.0 || userLatitude == 0.0) {
            return 0D;
        }

        double userLongitudeRab = rab(userLongitude);
        double userLatitudeRab = rab(userLatitude);
        double storeLongitudeRab = rab(storeLongitude);
        double storeLatitudeRab = rab(storeLatitude);
        double dLon = userLongitudeRab - storeLongitudeRab;
        double dLat = userLatitudeRab - storeLatitudeRab;
        double distance = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(dLat / 2), 2) +
                        Math.cos(userLatitudeRab) * Math.cos(storeLatitudeRab) * Math.pow(Math.sin(dLon / 2), 2)
        )) * EarthRadius;
        return Math.round(distance * 100d) / 100d;
    }

    @Override
    public List<StoreInfoDTO> getStoreListByName(String storeName) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .like(SystemStoreInfoDO::getStoreName, storeName)
                .eq(SystemStoreInfoDO::getStoreSource, 0);
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().map(systemStoreInfoDO -> {
                StoreInfoDTO storeInfoDTO = new StoreInfoDTO();
                storeInfoDTO.setStoreId(systemStoreInfoDO.getStoreId());
                storeInfoDTO.setStoreName(systemStoreInfoDO.getStoreName());
                return storeInfoDTO;
            }).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<StoreInfoDTO> getStoresByStoreIds(List<Long> storeIds) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getStoreId, storeIds);
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().map(systemStoreInfoDO -> {
                StoreInfoDTO storeInfoDTO = new StoreInfoDTO();
                storeInfoDTO.setStoreId(systemStoreInfoDO.getStoreId());
                storeInfoDTO.setStoreName(systemStoreInfoDO.getStoreName());
                return storeInfoDTO;
            }).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public StoreInfoDCRespVo getStoreDetailForDc(Long storeId) {
        //获取门店对象
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        if (ObjectUtil.isEmpty(systemStoreInfoDO)) {
            throw exception(STORE_NOT_EXISTS);
        }
        StoreInfoDCRespVo storeInfoDCRespVo = BeanUtils.toBean(systemStoreInfoDO, StoreInfoDCRespVo.class);
        List<SystemStoreExpensesDO> systemStoreExpensesDOS = systemStoreExpensesMapper.selectList(new LambdaQueryWrapper<SystemStoreExpensesDO>().eq(SystemStoreExpensesDO::getStoreId, storeId));
        storeInfoDCRespVo.setSystemStoreExpensesList(normalizeStoreExpensesForQuery(systemStoreExpensesDOS));
        fillCampusDeliveryFields(storeInfoDCRespVo, getFirstExpenseByType(systemStoreExpensesDOS, STORE_EXPENSE_TYPE_CAMPUS_DELIVERY));
        List<SystemStoreDeliveryScopeDO> systemStoreDeliveryScopeList = systemStoreDeliveryScopeMapper.selectList(new LambdaQueryWrapper<SystemStoreDeliveryScopeDO>().eq(SystemStoreDeliveryScopeDO::getStoreId, storeId));
        if (ObjectUtil.isNotEmpty(systemStoreDeliveryScopeList)) {
            storeInfoDCRespVo.setSystemStoreDeliveryScopeList(BeanUtils.toBean(systemStoreDeliveryScopeList, SystemStoreDeliveryScopeVO.class));
        }

        List<SystemStoreTagDO> systemStoreTagList = systemStoreTagMapper.selectList(new LambdaQueryWrapper<SystemStoreTagDO>().eq(SystemStoreTagDO::getStoreId, storeId));
        if (ObjectUtil.isNotEmpty(systemStoreTagList)) {
            Set<Long> tagIds = systemStoreTagList.stream().map(SystemStoreTagDO::getTagId).collect(Collectors.toSet());
            List<TagValueDO> tagValueList = tagValueMapper.selectList(new LambdaQueryWrapperX<TagValueDO>().in(TagValueDO::getId, tagIds));
            Map<Long, List<TagValueRespVO>> tagValueMap = tagValueList.stream().collect(Collectors.groupingBy(TagValueDO::getTagGroupId, Collectors.mapping(item -> BeanUtils.toBean(item, TagValueRespVO.class), Collectors.toList())));
            List<Long> tagGroupIds = tagValueList.stream().map(TagValueDO::getTagGroupId).toList();
            if (CollectionUtils.isNotEmpty(tagGroupIds)) {
                List<TagGroupDO> tagGroupList = tagGroupMapper.selectBatchIds(tagGroupIds);
                List<TagGroupRespVO> tagGroupResList = BeanUtils.toBean(tagGroupList, TagGroupRespVO.class);
                tagGroupResList.forEach(tagGroup -> {
                    List<TagValueRespVO> tagValues = tagValueMap.get(tagGroup.getId());
                    tagGroup.setTagValues(tagValues);
                });
                storeInfoDCRespVo.setStoreTagList(tagGroupResList);
            }
        }
        storeInfoDCRespVo.setStoreBackgroundImage(storeBackgroundCacheService.getBackgroundImage(storeId));
        return storeInfoDCRespVo;

    }

    @Override
    public List<Long> getStoreIdsByDeptId(Long orgId) {
        QueryWrapper<SystemStoreInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", orgId);
        queryWrapper.eq("store_source", 0);
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<StoreInfoDTO> getAllStoreList() {
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList();
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(systemStoreInfoDO -> {
                StoreInfoDTO storeInfoDTO = new StoreInfoDTO();
                storeInfoDTO.setStoreId(systemStoreInfoDO.getStoreId());
                storeInfoDTO.setStoreName(systemStoreInfoDO.getStoreName());
                return storeInfoDTO;
            }).collect(Collectors.toList());
        }
        return List.of();
    }


    @Override
    public List<StoreInfoDTO> getStoreListByBusinessId(Long businessId) {
        BusinessContextHolder.setBusinessId(businessId);
        return getAllStoreList();
    }

    /**
     * 查询城市
     */
    @Override
    public List<SysDeptCityVO> getCityName(String deptName) {
        OrgDO dept = new OrgDO();
        if (Objects.nonNull(deptName) && !Objects.equals(deptName, "null")) {
            dept.setName(deptName);
        }
        List<OrgDO> deptList = systemStoreInfoMapper.selectCityList(dept);
        List<CityVO> cities = new ArrayList<>();
        List<SysDeptCityVO> result = null;
        if (!deptList.isEmpty()) {
            List<String> cityName = deptList.stream().map(OrgDO::getName).toList();
            AtomicInteger i = new AtomicInteger();
            cityName.forEach(name -> {
                i.getAndIncrement();
                log.info("第{}次循环", i);
                log.info("第{}市", name);
                CityVO cityVO = new CityVO();
                String pinyin = PinyinUtil.toPinyin(name);
                cityVO.setPinYin(pinyin.substring(0, 1));
                cityVO.setName(name);
                cities.add(cityVO);
            });

            Map<String, Set<String>> groupedCityNames = cities.stream()
                    .collect(Collectors.groupingBy(CityVO::getPinYin,
                            Collectors.mapping(CityVO::getName, Collectors.toSet())));
            result = groupedCityNames.entrySet().stream()
                    .map(entry -> new SysDeptCityVO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

        }
        return result;
    }

    @DataPermission(enable = false)
    @PermitAll
    @Override
    public StoreDTO getStoreByStoreId(Long storeId) {
        StoreDTO returnObj = new StoreDTO();
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        if (ObjectUtils.isEmpty(systemStoreInfoDO)) {
            return null;
        }

        //支付商户号关系
        SysStoreExtendDO sysStoreExtendDO = sysStoreExtendService.getOne(
                new LambdaQueryWrapper<SysStoreExtendDO>()
                        .eq(SysStoreExtendDO::getStoreId, storeId)
        );

        //门店扩展信息
        List<SystemStoreExpensesDO> systemStoreExpensesDOS = systemStoreExpensesMapper.selectList(new LambdaQueryWrapper<SystemStoreExpensesDO>().eq(SystemStoreExpensesDO::getStoreId, storeId));

        returnObj.setExpensesList(BeanCopyUtils.copyBeanList(systemStoreExpensesDOS, StoreDTO.StoreExpensesVO.class));

        if (ObjectUtil.isNotEmpty(systemStoreInfoDO)) {
            BeanUtils.copyProperties(systemStoreInfoDO, returnObj);
            returnObj.setOrderStoreType(systemStoreInfoDO.getOrderType());
            returnObj.setProvince(systemStoreInfoDO.getStoreProvince());
            returnObj.setCity(systemStoreInfoDO.getStoreCity());
            returnObj.setArea(systemStoreInfoDO.getStoreDistrict());
            returnObj.setTerminalSn(
                    ObjectUtils.isEmpty(sysStoreExtendDO) ? "" : sysStoreExtendDO.getTerminalSn()
            );
            returnObj.setTerminalKey(
                    ObjectUtils.isEmpty(sysStoreExtendDO) ? "" : sysStoreExtendDO.getTerminalKey()
            );
        }
        return returnObj;
    }

    @Override
    public StoreDTO getStoreByDouyinStoreId(Long douyinStoreId) {
        StoreDTO returnObj = new StoreDTO();

        QueryWrapper<SystemStoreInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tiktok_id", douyinStoreId);
        queryWrapper.eq("deleted", 0);
        List<SystemStoreInfoDO> storeInfoDOList = systemStoreInfoMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(storeInfoDOList)) {
            return null;
        }

        SystemStoreInfoDO systemStoreInfoDO = storeInfoDOList.get(0);
        BeanUtils.copyProperties(systemStoreInfoDO, returnObj);
        return returnObj;
    }

    @Override
    public List<Long> selectByOrgStoreList(Long orgId) {
        Set<Long> orgIds = new HashSet<>();

        if (!org.springframework.util.StringUtils.isEmpty(orgId)) {
            orgIds = orgMapper.getChildIdListByStore(orgId);
            if (orgIds.isEmpty()) {
                Set<Long> businessIdList = new HashSet<>();
                businessIdList.add(orgId);
                List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
                orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
            }
            orgIds.add(orgId);
            LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SystemStoreInfoDO::getOrgId, orgIds)
                    .eq(SystemStoreInfoDO::getStoreSource, 0);
            List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);
            List<Long> collect = new ArrayList<>();
            if (systemStoreInfoDOS.size() > 0) {
                collect = systemStoreInfoDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
            }
            return collect;
        }
        return null;
    }

    @Override
    public PageResult<SystemStoreInfoDO> chooseStore(StorePageListReqVO pageReqVO) {

//        Set<SystemStoreInfoDO> byStoreList = getByStoreList(pageReqVO.getUserIds());
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getOrgIds())) {

            if (pageReqVO.getOrgIds().size() > 0) {
                Set<SystemStoreInfoDO> systemStoreSet = new HashSet<>();
                for (Long orgId : pageReqVO.getOrgIds()) {
                    Set<Long> orgListIds = orgMapper.getChildIdListByStore(orgId);


                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    systemStoreSet.addAll(systemStoreInfoDOS);
                }
                if (ObjectUtil.isNotEmpty(systemStoreSet)) {
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, systemStoreSet.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList()));
                } else {
                    PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                    result.setList(new ArrayList<>());
                    return result;
                }

            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getOpenStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getOpenStatus, pageReqVO.getOpenStatus());
        }
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getUserId())) {
            queryWrapper.eq(SystemStoreInfoDO::getUserId, pageReqVO.getUserId());
        }
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getStoreName())) {
            queryWrapper.like(SystemStoreInfoDO::getStoreName, pageReqVO.getStoreName());
        }

//        if(ObjectUtil.isNotEmpty(byStoreList)){
//            queryWrapper.in(SystemStoreInfoDO::getStoreId,byStoreList.stream().map(mm->mm.getStoreId()).collect(Collectors.toList()));
//        }

        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getStoreIds())) {
            if (pageReqVO.getStoreIds().size() > 0) {
                queryWrapper.notIn(SystemStoreInfoDO::getStoreId, pageReqVO.getStoreIds());
            }

        }

        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getTagId())) {
            if (pageReqVO.getTagId().size() > 0) {
                if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                    if (pageReqVO.getType().equals(2)) {
                        // type=2: 满足任意一个标签即可
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, pageReqVO.getTagId());
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                        List<Long> storeIds = systemStoreTagDOS.stream()
                                .map(SystemStoreTagDO::getStoreId)
                                .distinct()
                                .collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);

                    } else if (pageReqVO.getType().equals(1)) {
                        // type=1: 必须满足所有标签
                        List<Long> requiredTagIds = pageReqVO.getTagId();

                        // 查询包含任意一个传入标签的门店标签关系
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, requiredTagIds);
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            return new PageResult<>();
                        }

                        // 按门店ID分组，收集每个门店拥有的标签ID集合
                        Map<Long, Set<Long>> storeTagsMap = systemStoreTagDOS.stream()
                                .collect(Collectors.groupingBy(
                                        SystemStoreTagDO::getStoreId,
                                        Collectors.mapping(SystemStoreTagDO::getTagId, Collectors.toSet())
                                ));

                        // 筛选出拥有所有传入标签的门店ID
                        List<Long> storeIds = storeTagsMap.entrySet().stream()
                                .filter(entry -> {
                                    Set<Long> storeTagIds = entry.getValue();
                                    // 检查门店的标签集合是否包含所有要求的标签
                                    return storeTagIds.containsAll(requiredTagIds);
                                })
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        if (storeIds.isEmpty()) {
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                        //无标签
                    }else                 if(pageReqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                    }
                }else {
                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getTagId, pageReqVO.getTagId());
                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                    if (systemStoreTagDOS.isEmpty()) {
                        PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                        result.setList(new ArrayList<>());
                        return result;
                    }

                    List<Long> storeIds = systemStoreTagDOS.stream()
                            .map(SystemStoreTagDO::getStoreId)
                            .distinct()
                            .collect(Collectors.toList());
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                }

            }else {
                if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                    if(pageReqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                    }
                }
            }
        }else{
            if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                if(pageReqVO.getType().equals(3)){

                    List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                    List<Long> storeIdList = allStoreList.stream()
                            .map(StoreInfoDTO::getStoreId)
                            .collect(Collectors.toList());


                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                    List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                    Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                            .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                    List<StoreResVO> storeResVOS = allStoreList.stream()
                            .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                            .map(store -> {
                                StoreResVO vo = new StoreResVO();
                                BeanUtils.copyProperties(store, vo);
                                return vo;
                            })
                            .collect(Collectors.toList());

                    if(ObjectUtil.isNotEmpty(storeResVOS)){
                        List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                    }else{
                        PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                        result.setList(new ArrayList<>());
                        return result;
                    }

                }
            }

        }
        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.orderByDesc(SystemStoreInfoDO::getCreateTime);
//        queryWrapper.D
//        Page<StorePageResVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());


        PageParam page = new PageParam();
        page.setPageNo(pageReqVO.getPageNo());
        page.setPageSize(pageReqVO.getPageSize());
        PageResult<SystemStoreInfoDO> systemStoreInfoDOPageResult = systemStoreInfoMapper.selectPage(page, queryWrapper);
        List<SystemStoreInfoDO> list = systemStoreInfoDOPageResult.getList();
        if (!org.springframework.util.StringUtils.isEmpty(list)) {
            if (list.size() > 0) {
                for (SystemStoreInfoDO systemStoreInfoDO : list) {

                    if (!org.springframework.util.StringUtils.isEmpty(systemStoreInfoDO.getOrgId())) {
                        LambdaQueryWrapper<OrgDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                        lambdaQueryWrapper.eq(OrgDO::getId, systemStoreInfoDO.getOrgId());
                        OrgDO orgDO = orgMapper.selectOne(lambdaQueryWrapper);
                        if (orgDO != null) {
                            systemStoreInfoDO.setOrgName(orgDO.getName());
                        }

                    }

                }
            }
        }

        return systemStoreInfoDOPageResult;
    }


    @Override
    public PageResult<SystemStoreInfoDO> chooseStoreForTiktok(StorePageListReqVO pageReqVO) {

//        Set<SystemStoreInfoDO> byStoreList = getByStoreList(pageReqVO.getUserIds());
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.isNotNull(SystemStoreInfoDO::getTiktokId);
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getOrgIds())) {

            if (pageReqVO.getOrgIds().size() > 0) {
                Set<SystemStoreInfoDO> systemStoreSet = new HashSet<>();
                for (Long orgId : pageReqVO.getOrgIds()) {
                    Set<Long> orgListIds = orgMapper.getChildIdListByStore(orgId);


                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    systemStoreSet.addAll(systemStoreInfoDOS);
                }
                if (ObjectUtil.isNotEmpty(systemStoreSet)) {
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, systemStoreSet.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList()));
                }

            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getOpenStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getOpenStatus, pageReqVO.getOpenStatus());
        }
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getStoreStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, pageReqVO.getStoreStatus());
        }
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getUserId())) {
            queryWrapper.eq(SystemStoreInfoDO::getUserId, pageReqVO.getUserId());
        }
        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getStoreName())) {
            queryWrapper.like(SystemStoreInfoDO::getStoreName, pageReqVO.getStoreName());
        }

//        if(ObjectUtil.isNotEmpty(byStoreList)){
//            queryWrapper.in(SystemStoreInfoDO::getStoreId,byStoreList.stream().map(mm->mm.getStoreId()).collect(Collectors.toList()));
//        }

        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getStoreIds())) {
            if (pageReqVO.getStoreIds().size() > 0) {
                queryWrapper.notIn(SystemStoreInfoDO::getStoreId, pageReqVO.getStoreIds());
            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(pageReqVO.getTagId())) {
            if (pageReqVO.getTagId().size() > 0) {
                if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                    if (pageReqVO.getType().equals(2)) {
                        // type=2: 满足任意一个标签即可
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, pageReqVO.getTagId());
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                        List<Long> storeIds = systemStoreTagDOS.stream()
                                .map(SystemStoreTagDO::getStoreId)
                                .distinct()
                                .collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);

                    } else if (pageReqVO.getType().equals(1)) {
                        // type=1: 必须满足所有标签
                        List<Long> requiredTagIds = pageReqVO.getTagId();

                        // 查询包含任意一个传入标签的门店标签关系
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, requiredTagIds);
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            return new PageResult<>();
                        }

                        // 按门店ID分组，收集每个门店拥有的标签ID集合
                        Map<Long, Set<Long>> storeTagsMap = systemStoreTagDOS.stream()
                                .collect(Collectors.groupingBy(
                                        SystemStoreTagDO::getStoreId,
                                        Collectors.mapping(SystemStoreTagDO::getTagId, Collectors.toSet())
                                ));

                        // 筛选出拥有所有传入标签的门店ID
                        List<Long> storeIds = storeTagsMap.entrySet().stream()
                                .filter(entry -> {
                                    Set<Long> storeTagIds = entry.getValue();
                                    // 检查门店的标签集合是否包含所有要求的标签
                                    return storeTagIds.containsAll(requiredTagIds);
                                })
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        if (storeIds.isEmpty()) {
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                        //无标签
                    }else                 if(pageReqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                    }
                }else {
                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getTagId, pageReqVO.getTagId());
                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                    if (systemStoreTagDOS.isEmpty()) {
                        PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                        result.setList(new ArrayList<>());
                        return result;
                    }

                    List<Long> storeIds = systemStoreTagDOS.stream()
                            .map(SystemStoreTagDO::getStoreId)
                            .distinct()
                            .collect(Collectors.toList());
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                }

            }else {
                if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                    if(pageReqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                            result.setList(new ArrayList<>());
                            return result;
                        }

                    }
                }
            }
        }else{
            if(ObjectUtil.isNotEmpty(pageReqVO.getType())){
                if(pageReqVO.getType().equals(3)){

                    List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                    List<Long> storeIdList = allStoreList.stream()
                            .map(StoreInfoDTO::getStoreId)
                            .collect(Collectors.toList());


                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                    List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                    Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                            .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                    List<StoreResVO> storeResVOS = allStoreList.stream()
                            .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                            .map(store -> {
                                StoreResVO vo = new StoreResVO();
                                BeanUtils.copyProperties(store, vo);
                                return vo;
                            })
                            .collect(Collectors.toList());

                    if(ObjectUtil.isNotEmpty(storeResVOS)){
                        List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                    }else{
                        PageResult<SystemStoreInfoDO> result = new PageResult<SystemStoreInfoDO>();
                        result.setList(new ArrayList<>());
                        return result;
                    }

                }
            }

        }
        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.orderByDesc(SystemStoreInfoDO::getCreateTime);
//        queryWrapper.D
//        Page<StorePageResVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());


        PageParam page = new PageParam();
        page.setPageNo(pageReqVO.getPageNo());
        page.setPageSize(pageReqVO.getPageSize());
        PageResult<SystemStoreInfoDO> systemStoreInfoDOPageResult = systemStoreInfoMapper.selectPage(page, queryWrapper);
        List<SystemStoreInfoDO> list = systemStoreInfoDOPageResult.getList();
        if (!org.springframework.util.StringUtils.isEmpty(list)) {
            if (list.size() > 0) {
                for (SystemStoreInfoDO systemStoreInfoDO : list) {

                    if (!org.springframework.util.StringUtils.isEmpty(systemStoreInfoDO.getOrgId())) {
                        LambdaQueryWrapper<OrgDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                        lambdaQueryWrapper.eq(OrgDO::getId, systemStoreInfoDO.getOrgId());
                        OrgDO orgDO = orgMapper.selectOne(lambdaQueryWrapper);
                        if (orgDO != null) {
                            systemStoreInfoDO.setOrgName(orgDO.getName());
                        }

                    }

                }
            }
        }

        return systemStoreInfoDOPageResult;
    }

    @Override
    public StoreDeliveryDTO getStoreDelivery(Long storeId) {
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        return BeanUtils.toBean(systemStoreInfoDO, StoreDeliveryDTO.class);
    }

    @Override
    public List<StoreInfoDTO> getStoreIdsByOrgId(Long orgId) {
        List<StoreInfoDTO> storeInfoDTOS = new ArrayList<>();
        QueryWrapper<SystemStoreInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", orgId);
        queryWrapper.eq("store_source", 0);
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            for (SystemStoreInfoDO systemStoreInfoDO : list) {
                StoreInfoDTO storeInfoDTO = BeanUtils.toBean(systemStoreInfoDO, StoreInfoDTO.class);
                storeInfoDTOS.add(storeInfoDTO);
            }
        }


        return storeInfoDTOS;
    }

    @Override
    public Set<SystemStoreInfoDO> selectOrgStoreList(Long orgId) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        // 当前用户的组织ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);
        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return Set.of();
        }
        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        List<SystemStoreInfoDO> storeInfoDOS = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            // 店铺所在组织    上级置灰   下级置灰     本级绑定什么门店能看什么门店
            storeInfoDOS = systemStoreInfoService.selectStoreOrgInfos(storeIds);
            if (!org.springframework.util.StringUtils.isEmpty(storeInfoDOS)) {
                storeOrgIds = storeInfoDOS.stream().map(mm -> mm.getOrgId()).collect(Collectors.toList());
            }
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return Set.of();
        }
//        if(!unionIds.contains(orgId)){
//            return Set.of();
//        }

        Set<Long> orgListIds = new HashSet<>();
        Set<SystemStoreInfoDO> systemStoreSet = new HashSet<>();
        if (!org.springframework.util.StringUtils.isEmpty(orgId)) {
            if (orgIds.contains(orgId)) {
                orgListIds = orgMapper.getChildIdListByStore(orgId);


                LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);
                systemStoreSet.addAll(systemStoreInfoDOS);
                if (ObjectUtil.isNotEmpty(storeIds)) {
                    List<SystemStoreInfoDO> systemStoreInfoDOSTwo = systemStoreInfoMapper.selectByIds(storeIds);
                    systemStoreSet.addAll(systemStoreInfoDOSTwo);
                }
                return systemStoreSet;
            }
//            if (ObjectUtil.isEmpty(storeOrgIds)) {
//                orgListIds = orgMapper.getChildIdListByStore(orgId);
//
//
//                LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
//                queryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
//                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);
//                systemStoreSet.addAll(systemStoreInfoDOS);
//                if (ObjectUtil.isNotEmpty(storeIds)) {
//                    List<SystemStoreInfoDO> systemStoreInfoDOSTwo = systemStoreInfoMapper.selectByIds(storeIds);
//                    systemStoreSet.addAll(systemStoreInfoDOSTwo);
//                }
//                return systemStoreSet;
//            }else{
//                if(storeOrgIds.contains(orgId)){
//                    List<SystemStoreInfoDO> systemStoreInfoDOSTwo = systemStoreInfoMapper.selectByIds(storeIds);
//                    for (SystemStoreInfoDO systemStoreInfoDO : systemStoreInfoDOSTwo) {
//                        if(systemStoreInfoDO.getOrgId().equals(orgId)){
//                            systemStoreSet.add(systemStoreInfoDO);
//                        }
//
//                    }
//
//                }
//            }


//            if (ObjectUtil.isNotEmpty(storeOrgIds)) {
//                if(storeOrgIds.contains(orgId)){
//                    List<SystemStoreInfoDO> systemStoreInfoDOSTwo = systemStoreInfoMapper.selectByIds(storeIds);
//                    systemStoreSet.addAll(systemStoreInfoDOSTwo);
//                }
//
//            }

        }

        return Set.of();
    }

    @Override
    public List<StoreWecomConfigResDTO> selectByCouponStoreList(StoreWecomConfigReqDTO storeWecomConfigReqDTO) {
        //该城市下所有门店信息
        List<SystemStoreInfoDO> sysStoreInfoList = systemStoreInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>().likeIfPresent(SystemStoreInfoDO::getStoreCity, storeWecomConfigReqDTO.getCityName())
                        .eqIfPresent(SystemStoreInfoDO::getMiniproStatus, StoreStatusEnum.OPEN.getStatus())
                        .eqIfPresent(SystemStoreInfoDO::getStoreStatus, StoreStatusEnum.OPEN.getStatus())
                        .eqIfPresent(SystemStoreInfoDO::getStoreSource, 0)
                        .likeIfPresent(SystemStoreInfoDO::getStoreName, storeWecomConfigReqDTO.getStoreName())
                        .inIfPresent(SystemStoreInfoDO::getStoreId, storeWecomConfigReqDTO.getStoreIdList())
                        .eqIfPresent(SystemStoreInfoDO::getOpenStatus, StoreStatusEnum.OPEN.getStatus())
        );
        if (!CollectionUtils.isEmpty(sysStoreInfoList)) {
            Set<Long> storeIds = sysStoreInfoList.stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toSet());
            Map<Long, SystemStoreExpensesDO> storeWecomConfigResVOMap = systemStoreExpensesMapper.selectList(new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                    .in(SystemStoreExpensesDO::getStoreId, storeIds).eq(SystemStoreExpensesDO::getStoreExpensesType, 2)).stream().collect(Collectors.toMap(
                    SystemStoreExpensesDO::getStoreId,
                    Function.identity(),
                    (existing, replacement) -> existing // 重复时保留先出现的记录
            ));
            Map<Long, SystemStoreExpensesDO> storePackResVOMap = systemStoreExpensesMapper.selectList(new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                    .in(SystemStoreExpensesDO::getStoreId, storeIds).eq(SystemStoreExpensesDO::getStoreExpensesType, 0)).stream().collect(Collectors.toMap(
                    SystemStoreExpensesDO::getStoreId,
                    Function.identity(),
                    (existing, replacement) -> existing // 重复时保留先出现的记录
            ));
            sysStoreInfoList = sysStoreInfoList.stream().filter(e -> ObjectUtil.isNotEmpty(e.getStoreLongitude()) && ObjectUtil.isNotEmpty(e.getStoreLatitude())).collect(Collectors.toList());
            List<StoreWecomConfigResDTO> result = sysStoreInfoList.stream().map(sysStoreInfo -> {
                        SystemStoreExpensesDO storeWecomConfigResVO = storeWecomConfigResVOMap.get(sysStoreInfo.getStoreId());
                        SystemStoreExpensesDO storePackConfigResVO = storePackResVOMap.get(sysStoreInfo.getStoreId());

                        StoreWecomConfigResDTO vo = new StoreWecomConfigResDTO();
                        vo.setStoreName(sysStoreInfo.getStoreName());
                        vo.setStoreId(sysStoreInfo.getStoreId());
                        vo.setOpenStatus(isWithinBusinessHours(sysStoreInfo.getStoreHours()));
                        if (storeWecomConfigResVO != null) {
                            vo.setStoreAnnouncement(sysStoreInfo.getStoreAnnouncement());
                            vo.setAdditionaaCosts(storeWecomConfigResVO.getAdditionaaCosts());
                            vo.setMinimumPackageFee(storeWecomConfigResVO.getMinimumDeliveryFee());
                        }
                        if (storePackConfigResVO != null) {
                            vo.setPackCosts(storePackConfigResVO.getAdditionaaCosts());
                            vo.setMinimumDeliveryFee(storePackConfigResVO.getMinimumDeliveryFee());
                            vo.setStoreCalculationType(storePackConfigResVO.getStoreCalculationType());
                        }
                        vo.setStoreWithoutPayment(sysStoreInfo.getStoreWithoutPayment());
                        vo.setStorePayType(sysStoreInfo.getStorePayType());
                        vo.setStoreTakeaway(sysStoreInfo.getStoreTakeaway());
                        vo.setStoreHours(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreHours(), ""));
                        vo.setStoreAddress(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreAddress(), ""));
                        vo.setStoreAnnouncement(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreAnnouncement(), ""));
                        vo.setStorePhone(ObjectUtil.defaultIfNull(sysStoreInfo.getStorePhone(), ""));
                        vo.setDistance(calculateStoreDistance(storeWecomConfigReqDTO.getLongitude(), storeWecomConfigReqDTO.getLatitude(),
                                sysStoreInfo.getStoreLongitude(), sysStoreInfo.getStoreLatitude()));
                        vo.setLatitude(sysStoreInfo.getStoreLatitude());
                        vo.setLongitude(sysStoreInfo.getStoreLongitude());
                        vo.setIsPrompt(ObjectUtil.defaultIfNull(sysStoreInfo.getIsPrompt(), 0));
                        vo.setPromptText(ObjectUtil.defaultIfNull(sysStoreInfo.getPromptText(), ""));
                        return vo;
                    }).collect(Collectors.toList()).stream().sorted(Comparator.comparing((StoreWecomConfigResDTO vo) ->
                                    (vo.getStoreId() != null && !vo.getStoreId().equals(storeWecomConfigReqDTO.getStoreId())) ? 1 : 0)

                            .thenComparing(StoreWecomConfigResDTO::getDistance))
                    .collect(Collectors.toList());
            Map<Long, String> backgroundImages = storeBackgroundCacheService.getBackgroundImages(
                    result.stream().map(StoreWecomConfigResDTO::getStoreId).toList());
            result.forEach(item -> item.setStoreBackgroundImage(backgroundImages.get(item.getStoreId())));
            return result;
        }
        return Lists.newArrayList();
    }

    @Override
    public List<StoreInfoDTO> getStoresByName(String storeName) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(storeName)) {
            queryWrapper.like(SystemStoreInfoDO::getStoreName, storeName);
        }
        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.orderBy(true, false, SystemStoreInfoDO::getCreateTime);
        return BeanUtils.toBean(systemStoreInfoMapper.selectList(queryWrapper), StoreInfoDTO.class);
    }

    @Override
    public StoreWecomConfigResDTO getStoreById(Long storeId) {
        // 获取该城市下所有门店信息
        StoreWecomConfigResDTO vo = new StoreWecomConfigResDTO();
        SystemStoreInfoDO sysStoreInfo = systemStoreInfoMapper.selectById(storeId);

        if (sysStoreInfo != null) {
            // 提取查询门店费用配置的通用方法
            SystemStoreExpensesDO storeWecomConfigResVO = getStoreExpenses(storeId, 2);
            SystemStoreExpensesDO storePackConfigResVO = getStoreExpenses(storeId, 0);
            SystemStoreExpensesDO storeDeliveryConfigResVO = getStoreExpenses(storeId, 1);
            SystemStoreExpensesDO storeCampusDeliveryConfigResVO = getStoreExpenses(storeId, STORE_EXPENSE_TYPE_CAMPUS_DELIVERY);

            vo.setStoreName(sysStoreInfo.getStoreName());
            vo.setStoreId(sysStoreInfo.getStoreId());
            vo.setOpenStatus(sysStoreInfo.getOpenStatus());
            if (sysStoreInfo.getOpenStatus()==0){
                vo.setOpenStatus(isWithinBusinessHours(sysStoreInfo.getStoreHours()));
            }

            if (storeWecomConfigResVO != null) {
                vo.setStoreAnnouncement(sysStoreInfo.getStoreAnnouncement());
                vo.setAdditionaaCosts(storeWecomConfigResVO.getAdditionaaCosts());
                vo.setMinimumDeliveryFee(storeWecomConfigResVO.getMinimumDeliveryFee());
            }

            if (storePackConfigResVO != null) {
                vo.setPackCosts(storePackConfigResVO.getAdditionaaCosts());
                vo.setMinimumPackageFee(storePackConfigResVO.getMinimumDeliveryFee());
                vo.setStoreCalculationType(storePackConfigResVO.getStoreCalculationType());
            }

            if (storeDeliveryConfigResVO != null) {
                vo.setPackDeliveryCosts(storeDeliveryConfigResVO.getAdditionaaCosts());
                vo.setMinimumDeliveryPackFee(storeDeliveryConfigResVO.getMinimumDeliveryFee());
                vo.setStoreDeliveryCalculationType(storeDeliveryConfigResVO.getStoreCalculationType());
            }
            fillCampusDeliveryFields(vo, sysStoreInfo, storeCampusDeliveryConfigResVO);

            // 设置其他属性，使用ObjectUtil避免空指针
            vo.setStoreWithoutPayment(sysStoreInfo.getStoreWithoutPayment());
            vo.setStorePayType(sysStoreInfo.getStorePayType());
            vo.setTiktokId(Optional.ofNullable(sysStoreInfo.getTiktokId()).orElse(null));
            vo.setStoreTakeaway(sysStoreInfo.getStoreTakeaway());
            vo.setStoreHours(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreHours(), ""));
            vo.setStoreAddress(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreAddress(), ""));
            vo.setStoreAnnouncement(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreAnnouncement(), ""));
            vo.setStorePhone(ObjectUtil.defaultIfNull(sysStoreInfo.getStorePhone(), ""));
            vo.setDeliveryTime(ObjectUtil.defaultIfNull(sysStoreInfo.getDeliveryTime(), ""));
            vo.setStoreHours(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreHours(), ""));
            vo.setIsPrompt(ObjectUtil.defaultIfNull(sysStoreInfo.getIsPrompt(), 0));
            vo.setPromptText(ObjectUtil.defaultIfNull(sysStoreInfo.getPromptText(), ""));
            vo.setCityName(ObjectUtil.defaultIfNull(sysStoreInfo.getStoreCity(), ""));
            if(sysStoreInfo.getOpenStatus().equals(0)){
                vo.setOpenStatus(getOpenStatus(sysStoreInfo.getOpenStatus(),sysStoreInfo.getStoreHours()));
            }
            vo.setStoreBackgroundImage(storeBackgroundCacheService.getBackgroundImage(storeId));
        }
        return vo;

    }

    // 提取的查询门店费用配置的通用方法
    /**
     * 查询回显费用列表时兼容脏数据：重复类型取第一条，缺失类型补默认值。
     */
    private List<SystemStoreExpensesVO> normalizeStoreExpensesForQuery(List<SystemStoreExpensesDO> expensesList) {
        Map<Integer, SystemStoreExpensesDO> expenseMap = CollectionUtil.isEmpty(expensesList)
                ? Collections.emptyMap()
                : expensesList.stream()
                .filter(item -> item != null && item.getStoreExpensesType() != null)
                .collect(Collectors.toMap(
                        SystemStoreExpensesDO::getStoreExpensesType,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        List<Integer> typeList = List.of(
                StoreExpensesTypeEnum.CANTEEN_FOOD.getCode(),
                StoreExpensesTypeEnum.TAKEAWAY_PACKAGE.getCode(),
                StoreExpensesTypeEnum.TAKEAWAY_DELIVERY.getCode(),
                STORE_EXPENSE_TYPE_CAMPUS_DELIVERY);
        List<SystemStoreExpensesVO> result = new ArrayList<>();
        for (Integer type : typeList) {
            SystemStoreExpensesDO expense = expenseMap.get(type);
            SystemStoreExpensesVO vo = expense == null ? new SystemStoreExpensesVO() : BeanUtils.toBean(expense, SystemStoreExpensesVO.class);
            vo.setStoreExpensesType(type);
            if (vo.getMinimumDeliveryFee() == null) {
                vo.setMinimumDeliveryFee(BigDecimal.ZERO);
            }
            vo.setAdditionaaCosts(Objects.equals(type, STORE_EXPENSE_TYPE_CAMPUS_DELIVERY)
                    ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(vo.getAdditionaaCosts(), BigDecimal.ZERO));
            if (Objects.equals(type, STORE_EXPENSE_TYPE_CAMPUS_DELIVERY)) {
                vo.setStoreCalculationType(null);
            }
            result.add(vo);
        }
        return result;
    }

    private SystemStoreExpensesDO getStoreExpenses(Long storeId, Integer type) {
        LambdaQueryWrapperX<SystemStoreExpensesDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(SystemStoreExpensesDO::getStoreId, storeId)
                .eq(SystemStoreExpensesDO::getStoreExpensesType, type)
                .last("limit 1"); // 确保只返回一条记录

        List<SystemStoreExpensesDO> list = systemStoreExpensesMapper.selectList(queryWrapper);
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * 批量获取门店指定类型费用，重复数据保留第一条，兼容老数据脏数据。
     */
    private Map<Long, SystemStoreExpensesDO> getStoreExpensesMap(Collection<Long> storeIds, Integer type) {
        if (CollectionUtil.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }
        List<SystemStoreExpensesDO> expensesList = systemStoreExpensesMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                        .in(SystemStoreExpensesDO::getStoreId, storeIds)
                        .eq(SystemStoreExpensesDO::getStoreExpensesType, type));
        if (CollectionUtil.isEmpty(expensesList)) {
            return Collections.emptyMap();
        }
        return expensesList.stream()
                .filter(item -> item != null && item.getStoreId() != null)
                .collect(Collectors.toMap(
                        SystemStoreExpensesDO::getStoreId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 从费用列表中安全获取指定类型费用，缺失或重复都不抛错。
     */
    private SystemStoreExpensesDO getFirstExpenseByType(List<SystemStoreExpensesDO> expensesList, Integer type) {
        if (CollectionUtil.isEmpty(expensesList)) {
            return null;
        }
        return expensesList.stream()
                .filter(item -> item != null && Objects.equals(item.getStoreExpensesType(), type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 填充小程序门店列表/详情的校园配送字段。
     */
    private void fillCampusDeliveryFields(StoreWecomConfigResVO vo, SystemStoreInfoDO storeInfo, SystemStoreExpensesDO campusExpense) {
        vo.setCampusDeliveryStatus(ObjectUtil.defaultIfNull(storeInfo.getCampusDeliveryStatus(), STATUS_DISABLE));
        vo.setCampusDeliverySubsidy(ObjectUtil.defaultIfNull(storeInfo.getCampusDeliverySubsidy(), BigDecimal.ZERO));
        vo.setCampusMinimumDeliveryFee(campusExpense == null ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
        vo.setCampusDeliveryFee(BigDecimal.ZERO);
        vo.setCampusDeliveryCalculationType(null);
        if (campusExpense != null) {
            vo.setCampusMinimumDeliveryFee(ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
            vo.setCampusDeliveryFee(BigDecimal.ZERO);
            vo.setCampusDeliveryCalculationType(null);
        }
    }

    /**
     * 填充 API DTO 的校园配送字段。
     */
    private void fillCampusDeliveryFields(StoreWecomConfigResDTO vo, SystemStoreInfoDO storeInfo, SystemStoreExpensesDO campusExpense) {
        vo.setCampusDeliveryStatus(ObjectUtil.defaultIfNull(storeInfo.getCampusDeliveryStatus(), STATUS_DISABLE));
        vo.setCampusDeliverySubsidy(ObjectUtil.defaultIfNull(storeInfo.getCampusDeliverySubsidy(), BigDecimal.ZERO));
        vo.setCampusMinimumDeliveryFee(campusExpense == null ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
        vo.setCampusDeliveryFee(BigDecimal.ZERO);
        vo.setCampusDeliveryCalculationType(null);
        if (campusExpense != null) {
            vo.setCampusMinimumDeliveryFee(ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
            vo.setCampusDeliveryFee(BigDecimal.ZERO);
            vo.setCampusDeliveryCalculationType(null);
        }
    }

    /**
     * 填充用户门店列表的校园配送字段。
     */
    private void fillCampusDeliveryFields(StoreSimpleResVO vo, SystemStoreExpensesDO campusExpense) {
        vo.setCampusDeliveryStatus(ObjectUtil.defaultIfNull(vo.getCampusDeliveryStatus(), STATUS_DISABLE));
        vo.setCampusDeliverySubsidy(ObjectUtil.defaultIfNull(vo.getCampusDeliverySubsidy(), BigDecimal.ZERO));
        vo.setCampusMinimumDeliveryFee(campusExpense == null ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
        vo.setCampusDeliveryFee(BigDecimal.ZERO);
        vo.setCampusDeliveryCalculationType(null);
        if (campusExpense != null) {
            vo.setCampusMinimumDeliveryFee(ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
            vo.setCampusDeliveryFee(BigDecimal.ZERO);
            vo.setCampusDeliveryCalculationType(null);
        }
    }

    /**
     * 填充点餐机门店详情的校园配送字段。
     */
    private void fillCampusDeliveryFields(StoreInfoDCRespVo vo, SystemStoreExpensesDO campusExpense) {
        vo.setCampusDeliveryStatus(ObjectUtil.defaultIfNull(vo.getCampusDeliveryStatus(), STATUS_DISABLE));
        vo.setCampusDeliverySubsidy(ObjectUtil.defaultIfNull(vo.getCampusDeliverySubsidy(), BigDecimal.ZERO));
        vo.setCampusMinimumDeliveryFee(campusExpense == null ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
        vo.setCampusDeliveryFee(BigDecimal.ZERO);
        vo.setCampusDeliveryCalculationType(null);
        if (campusExpense != null) {
            vo.setCampusMinimumDeliveryFee(ObjectUtil.defaultIfNull(campusExpense.getMinimumDeliveryFee(), BigDecimal.ZERO));
            vo.setCampusDeliveryFee(BigDecimal.ZERO);
            vo.setCampusDeliveryCalculationType(null);
        }
    }

    @Override
    public List<StoreSimpleResVO> listSimple() {
        List<StoreSimpleResVO> list = new ArrayList<>();
        //查询deptIDs
        Set<Long> orgIds = new HashSet<>();
        Long userId = WebFrameworkUtils.getLoginUserId();
        //当前用户的机构ID
        List<Long> orgId = orgUserService.selectUserOrgIds(userId);
        orgIds.addAll(orgId);
        Set<Long> finalOrgIds = new HashSet<>();
        orgId.forEach(orgd -> {
            finalOrgIds.addAll(orgMapper.getChildIdListByStore(orgd).stream().collect(Collectors.toSet()));
        });
        Set<Long> businessIdList = new HashSet<>();
        List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
        orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
        orgIds.addAll(finalOrgIds);

        List<SystemStoreInfoDO> storeList = systemStoreInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                .in(SystemStoreInfoDO::getOrgId, orgIds)
                .eqIfPresent(SystemStoreInfoDO::getStoreSource, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0).eq(SystemStoreInfoDO::getStoreStatus, 0));

        list = BeanUtils.toBean(storeList, StoreSimpleResVO.class);
        return list;
    }

    @Override
    public List<StoreSimpleResVO> getStoreListByUser() {
        List<StoreSimpleResVO> list = new ArrayList<>();

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoMapper.selectStoreOrgIds(storeIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (com.alibaba.cloud.commons.lang.StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(com.alibaba.cloud.commons.lang.StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
//        allOrgSet.addAll(upOrgList);
        allOrgSet.addAll(orgList);

        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());


        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
        if (ObjectUtil.isNotEmpty(storeIds)) {
            List<SystemStoreInfoDO> listData = new ArrayList<>();
            for (Long storeId : storeIds) {
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                if (systemStoreInfoDO != null) {
                    Long orgId = systemStoreInfoDO.getOrgId();
                    // 判断orgId是否在collect中存在，如果存在则跳过，不向下执行
                    if (collect.contains(orgId)) {
                        continue;  // 跳过当前循环，不添加到listData中
                    }
                    listData.add(systemStoreInfoDO);
                }
            }
            if (ObjectUtil.isNotEmpty(listData)) {
                Map<Long, List<SystemStoreInfoDO>> listMap = listData.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                for (Long aLong : listMap.keySet()) {
                    if (!orgIds.contains(aLong)) {
                        storeMap.put(aLong, listMap.get(aLong));
                    }
                }
            }


        }
        // 加入上级节点
        for (Long l : storeMap.keySet()) {

            List<SystemStoreInfoDO> stores = storeMap.get(l);
            if (ObjectUtil.isNotEmpty(stores)) {
                for (SystemStoreInfoDO store : stores) {
                    StoreSimpleResVO storeSimpleResVO = new StoreSimpleResVO();
                    storeSimpleResVO.setStoreName(store.getStoreName());
                    storeSimpleResVO.setStoreStatus(store.getStoreStatus());
                    storeSimpleResVO.setStoreId(store.getStoreId());
                    storeSimpleResVO.setWarehouseId(store.getWarehouseId());
                    storeSimpleResVO.setIdentificationTemplate(store.getIdentificationTemplate());
                    list.add(storeSimpleResVO);
                }
            }
        }

        return list;
    }

    @Override
    public List<OrgStoreDTO> getOrgIdsByStoreIds(List<Long> storeIds) {
        return systemStoreInfoMapper.getOrgIdsByStoreIds(storeIds);
    }

    @Override
    public List<SystemStoreInfoDO> selectStoreOrgInfos(List<Long> storeIds) {

        return systemStoreInfoMapper.selectByIds(storeIds);
    }

    @Override
    public List<Long> selectUserAllOrgStoreIds(Long userId) {
        return systemStoreInfoMapper.selectUserAllOrgStoreIds(userId);
    }

    @Override
    public List<Long> getStoreIdsByAllOrgId(Long orgId) {
        return systemStoreInfoMapper.getStoreIdsByAllOrgId(orgId);
    }


    /**
     * 获取权限的门店数据
     *
     * @param userId
     * @return
     */
    public Set<SystemStoreInfoDO> getByStoreList(Long userId) {

        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return Set.of();
        }

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return Set.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);


        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (com.alibaba.cloud.commons.lang.StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(com.alibaba.cloud.commons.lang.StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }


        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
        allOrgSet.addAll(orgList);

        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());

        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoService.selectStoreByOrgIds(collect);
        Set<SystemStoreInfoDO> list = new HashSet<>();
        list.addAll(systemStoreInfoDOS);
        if (ObjectUtil.isNotEmpty(storeIds)) {
            LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
            List<SystemStoreInfoDO> systemStoreInfoDOSTwo = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
            list.addAll(systemStoreInfoDOSTwo);
        }
        return list;
    }

    /**
     * 解析营业时间字符串并判断当前时间是否在营业范围内
     *
     * @param hoursString 营业时间字符串，格式为"HH:mm-HH:mm"，例如"8:00-12:00"
     * @return 如果当前时间在营业范围内返回true，否则返回false
     */
    public static Integer isWithinBusinessHours(String hoursString) {
        if (hoursString == null || hoursString.isEmpty()) {
            return 0;
        }
        DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

        try {
            if (hoursString.contains(",")) {
                int isOpen = 1;
                String[] hours = hoursString.split(",");
                for (String hour : hours) {
                    if (isWithinBusinessHours(hour) == 0) {
                        isOpen = 0;
                        break;
                    }
                }
                return isOpen;
            }
            // 分割营业时间字符串
            String[] parts = hoursString.split("-");
            if (parts.length != 2) {
                return 0;
            }

            // 解析开始时间和结束时间
            LocalTime openTime = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
            LocalTime closeTime = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
            LocalTime currentTime = LocalTime.now();
            if (openTime.isBefore(closeTime)) {
                // 同一天内的营业时间，例如 9:00:00-18:00:00
                if (!currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime)) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                // 跨天的营业时间，例如 18:00:00-次日6:00:00
                if (currentTime.isAfter(openTime) || currentTime.isBefore(closeTime)) {
                    return 0;
                } else {
                    return 1;
                }
            }

        } catch (Exception e) {
            // 处理时间格式解析错误
            System.err.println("营业时间格式解析错误: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<StoreSelectVO> getAllStoreInfo(String storeName) {
        List<StoreSelectVO> returnList = new ArrayList<>();

        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(SystemStoreInfoDO::getDeleted, 0)
                .eq(SystemStoreInfoDO::getStoreSource, 0);

        if (StringUtils.isNotBlank(storeName)) {
            // 判断是否全是数字
            if (storeName.matches("^\\d+$")) {
                // 按 storeId 查询
                queryWrapper.eq(SystemStoreInfoDO::getStoreId, Long.parseLong(storeName));
            } else {
                // 按 storeName 模糊查询
                queryWrapper.like(SystemStoreInfoDO::getStoreName, storeName);
            }
        }

        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(systemStoreInfoDOS)) {
            return returnList;
        }

        List<BusinessDTO> businessDTOS = businessService.listAll();
        if (CollectionUtils.isEmpty(businessDTOS)) {
            return returnList;
        }

        List<SysStoreExtendDO> extendDOList = sysStoreExtendService.list();
        Set<Long> storeIds = extendDOList.stream().map(SysStoreExtendDO::getStoreId).collect(Collectors.toSet());

        Map<Long, String> businessMap = businessDTOS.stream().collect(Collectors.toMap(BusinessDTO::getId, BusinessDTO::getName, (key1, key2) -> key1));

        for (SystemStoreInfoDO systemStoreInfoDO : systemStoreInfoDOS) {
            if (ObjectUtils.isEmpty(systemStoreInfoDO.getBusinessId()) || ObjectUtils.isEmpty(businessMap.get(systemStoreInfoDO.getBusinessId()))) {
                continue;
            }
            StoreSelectVO storeSelectVO = new StoreSelectVO();
            storeSelectVO.setStoreId(systemStoreInfoDO.getStoreId());
            storeSelectVO.setStoreName(systemStoreInfoDO.getStoreName());
            storeSelectVO.setBusinessId(systemStoreInfoDO.getBusinessId());
            storeSelectVO.setBusinessName(businessMap.get(systemStoreInfoDO.getBusinessId()));

            if (storeIds.contains(systemStoreInfoDO.getStoreId())) {
                continue;
            }

            returnList.add(storeSelectVO);
        }
        return returnList;
    }

    @Override
    public List<SystemStoreInfoDO> selectStoreByOrgIdsTwo(Set<Long> orgIds) {
//        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
//                .eq(SystemStoreInfoDO::getStoreStatus, 0)
//                .eq(SystemStoreInfoDO::getOpenStatus, 0)
//                .eq(SystemStoreInfoDO::getStoreSource, 0);
//
//        // Only add orgId condition if the set is not empty
//        if (collect != null && !collect.isEmpty()) {
//            queryWrapper.in(SystemStoreInfoDO::getOrgId, collect);
//        }
//        return systemStoreInfoMapper.selectList(queryWrapper);

        // 分批查询，每批50个orgId
        int batchSize = 50;
        List<Long> orgIdList = new ArrayList<>(orgIds);
        List<SystemStoreInfoDO> result = new ArrayList<>();

        for (int i = 0; i < orgIdList.size(); i += batchSize) {
            List<Long> batch = orgIdList.subList(i, Math.min(i + batchSize, orgIdList.size()));
            LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                    .select(
                            SystemStoreInfoDO::getStoreId,
                            SystemStoreInfoDO::getStoreName,
                            SystemStoreInfoDO::getStoreCity,
                            SystemStoreInfoDO::getOrgId
                    )
                    .in(SystemStoreInfoDO::getOrgId, batch)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0)
                    .eq(SystemStoreInfoDO::getStoreSource, 0);
            result.addAll(systemStoreInfoMapper.selectList(queryWrapper));
        }
        return result;
    }

    /**
     * 同步初始化省市区
     */
    @Override
    @Transactional
    public void syncLocation() {

        Flowable.just("trigger")
                .doOnNext(s -> {
                    // 可选：做一些预处理
                })
                .observeOn(Schedulers.io())
                .subscribe(
                        s -> printReceipt(),  // 调用实际方法
                        throwable -> {
                            // 错误处理
                        }
                );
    }

    private void printReceipt() {

        List<SystemStoreInfoDO> storeList = systemStoreInfoMapper.selectAllStoreList();
        if (storeList == null || storeList.isEmpty()) {
            log.info("没有需要同步位置的门店");
            return;
        }

        log.info("开始同步门店位置信息，共 {} 家门店", storeList.size());

        int successCount = 0;
        int failCount = 0;

        for (SystemStoreInfoDO store : storeList) {
            try {
                String oldCity = store.getStoreCity();
                // 跳过经纬度为空的门店
                if (store.getStoreLongitude() == null || store.getStoreLatitude() == null) {
                    log.warn("门店ID {} 经纬度为空，跳过位置同步", store.getStoreId());
                    failCount++;
                    continue;
                }

                // 调用地图API获取位置信息并更新门店对象
                boolean success = fillLocationInfo(store);

                if (success) {
                    // 更新数据库
                    systemStoreInfoMapper.updateById(store);
                    storeCityListCacheService.refreshStoreAfterCommit(store.getStoreId(),
                            oldCity, store.getStoreCity(), false);
                    successCount++;
                    log.info("门店ID {} 位置信息更新成功: {}", store.getStoreId(), store.getStoreAddress());
                } else {
                    failCount++;
                    log.error("门店ID {} 位置信息更新失败", store.getStoreId());
                }
            } catch (Exception e) {
                failCount++;
                log.error("处理门店ID {} 位置信息时发生异常", store.getStoreId(), e);
            }
        }

        log.info("位置同步完成，成功: {}, 失败: {}", successCount, failCount);
    }

    private boolean fillLocationInfo(SystemStoreInfoDO store) {
        String lon = store.getStoreLongitude().toString();
        String lat = store.getStoreLatitude().toString();

        String url = TENCENT_MAP_API_URL
                + "?location=" + lat + "," + lon
                + "&key=" + API_KEY
                + "&get_poi=1";

        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("HTTP请求失败，状态码: {}", response.code());
                return false;
            }

            String responseBody = response.body().string();
            JSONObject jsonObject = JSONObject.parseObject(responseBody);

            String status = jsonObject.getString("status");
            if ("0".equals(status)) {
                JSONObject resultObj = jsonObject.getJSONObject("result");
                JSONObject addressComponent = resultObj.getJSONObject("address_component");

                String province = addressComponent.getString("province");
                String city = addressComponent.getString("city");
                String district = addressComponent.getString("district");
                String streetNumber = addressComponent.getString("street_number");

                // 填充门店对象
                store.setStoreProvince(province);
                store.setStoreCity(city);
                store.setStoreDistrict(district);

                // 构建完整地址
                String fullAddress = String.format("%s%s%s%s",
                        province, city, district, streetNumber);
                store.setStoreAddress(fullAddress);

                return true;
            } else {
                String message = jsonObject.getString("message");
                log.error("API返回错误: {}", message);
                return false;
            }
        } catch (IOException e) {
            log.error("IO异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void fixStoreExpenses() {
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoMapper.selectList();
        List<Long> refreshedStoreIds = new ArrayList<>();
        for (SystemStoreInfoDO storeInfoDO : storeInfoDOS) {
            List<SystemStoreExpensesDO> systemStoreExpensesDOS = null;
            try {
                String packageSetting = storeInfoDO.getPackageSetting();
                if (ObjectUtil.isEmpty(packageSetting)) {
                    continue;
                }

                JSONObject jsonObject = JSONObject.parseObject(packageSetting);


                systemStoreExpensesDOS = new ArrayList<>();

                if (jsonObject.get("type") == null || jsonObject.get("value") == null) {
                    continue;
                }

                SystemStoreExpensesDO systemStoreExpensesDO1 = SystemStoreExpensesDO.builder()
                        .storeId(storeInfoDO.getStoreId())
                        .storeExpensesType(0)
                        .minimumDeliveryFee(BigDecimal.ZERO)
                        .additionaaCosts(BigDecimal.ZERO)
                        .storeCalculationType(0)
                        .build();

                systemStoreExpensesDOS.add(systemStoreExpensesDO1);

                //商品
                if (Integer.parseInt(jsonObject.get("type").toString()) == 1) {
                    SystemStoreExpensesDO systemStoreExpensesDO = SystemStoreExpensesDO.builder()
                            .storeId(storeInfoDO.getStoreId())
                            .storeExpensesType(1)
                            .minimumDeliveryFee(new BigDecimal(jsonObject.get("value").toString()))
                            .additionaaCosts(BigDecimal.ZERO)
                            .storeCalculationType(0)
                            .build();
                    systemStoreExpensesDOS.add(systemStoreExpensesDO);
                }

                //订单
                if (Integer.parseInt(jsonObject.get("type").toString()) == 2) {
                    SystemStoreExpensesDO systemStoreExpensesDO = SystemStoreExpensesDO.builder()
                            .storeId(storeInfoDO.getStoreId())
                            .storeExpensesType(1)
                            .minimumDeliveryFee(BigDecimal.ZERO)
                            .additionaaCosts(new BigDecimal(jsonObject.get("value").toString()))
                            .storeCalculationType(1)
                            .build();
                    systemStoreExpensesDOS.add(systemStoreExpensesDO);
                }

                SystemStoreExpensesDO systemStoreExpensesDO2 = SystemStoreExpensesDO.builder()
                        .storeId(storeInfoDO.getStoreId())
                        .storeExpensesType(2)
                        .minimumDeliveryFee(storeInfoDO.getMinimumDeliveryFee())
                        .additionaaCosts(storeInfoDO.getDeliveryFee())
                        .storeCalculationType(0)
                        .build();

                systemStoreExpensesDOS.add(systemStoreExpensesDO2);
                systemStoreExpensesMapper.insertBatch(systemStoreExpensesDOS);
                refreshedStoreIds.add(storeInfoDO.getStoreId());

            } catch (Exception e) {
                log.info(">>> 修复门店运费配置失败 {}", storeInfoDO.getStoreId());
            }

        }
        storeCityListCacheService.refreshStoresAfterCommit(refreshedStoreIds);

    }

    @Override
    public List<SystemStoreInfoDO> selectOperationStoreByOrgIds(Set<Long> orgIds) {
        // 分批查询，每批50个orgId
        int batchSize = 50;
        List<Long> orgIdList = new ArrayList<>(orgIds);
        List<SystemStoreInfoDO> result = new ArrayList<>();

        for (int i = 0; i < orgIdList.size(); i += batchSize) {
            List<Long> batch = orgIdList.subList(i, Math.min(i + batchSize, orgIdList.size()));
            LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                    .select(
                            SystemStoreInfoDO::getStoreId,
                            SystemStoreInfoDO::getStoreName,
                            SystemStoreInfoDO::getStoreCity,
                            SystemStoreInfoDO::getOrgId,
                            SystemStoreInfoDO::getIdentificationTemplate,
                            SystemStoreInfoDO::getIsAllProduct,
                            SystemStoreInfoDO::getIsAllCommdity,
                            SystemStoreInfoDO::getIsSameLine,
                            SystemStoreInfoDO::getWarehouseId,
                            SystemStoreInfoDO::getWarehouseName,
                            SystemStoreInfoDO::getProjectId,
                            SystemStoreInfoDO::getProjectCode,
                            SystemStoreInfoDO::getProjectName,
                            SystemStoreInfoDO::getDeliveryLineId,
                            SystemStoreInfoDO::getDeliveryLineName
                    )
                    .in(SystemStoreInfoDO::getOrgId, batch)
                    .eq(SystemStoreInfoDO::getStoreSource, 0)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0);
            result.addAll(systemStoreInfoMapper.selectList(queryWrapper));
        }
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(null);
        return result;
    }

    @DataPermission(enable = false)
    @Override
    public List<StoreSimpleResVO> getMaterialStoreList(String name, Long storeId, Long wo) {
        List<StoreSimpleResVO> list = new ArrayList<>();
        LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(name)) {
            lambdaQueryWrapper.like(SystemStoreInfoDO::getStoreName, name);
        }
        if (ObjectUtil.isNotEmpty(storeId)) {
            lambdaQueryWrapper.ne(SystemStoreInfoDO::getStoreId, storeId);
        }
        if (ObjectUtil.isNotEmpty(wo)) {
            lambdaQueryWrapper.eq(SystemStoreInfoDO::getWarehouseId, wo);
        }
//        lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
        lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(systemStoreInfoDOS)) {
            list = BeanCopyUtils.copyBeanList(systemStoreInfoDOS, StoreSimpleResVO.class);

        }

        return list;
    }

    /**
     * 递归查询当前组织及上级组织的负责人（BusinessUserDO）
     *
     * @param orgId 当前组织ID
     * @return 找到的负责人，无则返回null
     */
    private AdminUserDO findResponsibleBusinessUser(Long orgId) {
        if (orgId == null) {
            return null; // 组织ID为空，终止查询
        }

        // 1. 查询当前组织的负责人（OrgUserDO type=1 表示负责人）
        List<OrgUserDO> orgUserDOS = userOrgMapper.selectList(
                new LambdaQueryWrapperX<OrgUserDO>()
                        .eq(OrgUserDO::getOrgId, orgId)
                        .eq(OrgUserDO::getType, 1) // 假设type=1是负责人类型
        );

        // 2. 如果当前组织有负责人，查询对应的BusinessUserDO
        if (CollectionUtils.isNotEmpty(orgUserDOS)) {
            OrgUserDO orgUserDO = orgUserDOS.get(0); // 取第一个负责人
            List<AdminUserDO> adminUserDOS = adminUserMapper.selectList(
                    new LambdaQueryWrapperX<AdminUserDO>()
                            .eq(AdminUserDO::getId, orgUserDO.getUserId())
                            .eq(AdminUserDO::getDeleted, 0) // 未删除
            );
            if (CollectionUtils.isNotEmpty(adminUserDOS)) {
                return adminUserDOS.get(0); // 返回找到的负责人
            }
        }

        // 3. 本级无负责人，查询上级组织
        OrgDO currentOrg = orgMapper.selectById(orgId);
        if (currentOrg == null) {
            return null; // 组织不存在，终止查询
        }
        Long parentOrgId = currentOrg.getParentId(); // 假设OrgDO有parentId字段表示上级组织ID
        return findResponsibleBusinessUser(parentOrgId); // 递归查询上级
    }

    @Override
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_UPDATE_SUB_TYPE_DC, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_UPDATE_SUCCESS)
    public void updateStoreByDC(StoreSaveReqVO storeUserSaveReqVO) {
        SystemStoreInfoDO systemStoreInfoOldDO = systemStoreInfoMapper.selectById(storeUserSaveReqVO.getStoreId());
        if (storeUserSaveReqVO.getStoreStatus().equals(StoreStatusEnum.SHUTDOWN.getStatus()) && storeUserSaveReqVO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus()) && systemStoreInfoOldDO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus())) {
            throw exception(STORE_STOP_EXISTS);
        }
        SystemStoreInfoDO systemStoreInfoDO = BeanUtils.toBean(storeUserSaveReqVO, SystemStoreInfoDO.class);


        // 批量插入配送范围和费用信息
        validateStoreExpensesForUpdate(storeUserSaveReqVO);
        insertBatchData(storeUserSaveReqVO, systemStoreInfoDO.getStoreId(), false);

        systemStoreInfoMapper.updateById(systemStoreInfoDO);


        //创建交换机
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(storeUserSaveReqVO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", storeUserSaveReqVO);
        this.createExchange(systemStoreInfoDO.getStoreId());
        refreshStoreBusinessStatusCache(systemStoreInfoDO);
        storeCityListCacheService.refreshStoreAfterCommit(systemStoreInfoDO.getStoreId(),
                systemStoreInfoOldDO.getStoreCity(), systemStoreInfoDO.getStoreCity(), false);

    }

    @Override
    public List<Long> getStoreIdsByWarehouseId(Long warehouseId) {

        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(SystemStoreInfoDO::getWarehouseId,
                warehouseId);

        if (org.springframework.util.CollectionUtils.isEmpty(systemStoreInfoDOS)) {
            return List.of();
        }

        return systemStoreInfoDOS.stream().map(SystemStoreInfoDO::getStoreId).toList();
    }


    @Override
    public List<SystemStoreInfoDO> selectAllStore(StorePageListReqVO reqVO) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getOrgIds())) {

            if (reqVO.getOrgIds().size() > 0) {
                Set<SystemStoreInfoDO> systemStoreSet = new HashSet<>();
                for (Long orgId : reqVO.getOrgIds()) {
                    Set<Long> orgListIds = orgMapper.getChildIdListByStore(orgId);


                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    systemStoreSet.addAll(systemStoreInfoDOS);
                }
                if (ObjectUtil.isNotEmpty(systemStoreSet)) {
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, systemStoreSet.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList()));
                } else {
                    return new ArrayList<>();
                }

            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getOpenStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getOpenStatus, reqVO.getOpenStatus());
        }

        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getStoreName())) {
            queryWrapper.like(SystemStoreInfoDO::getStoreName, reqVO.getStoreName());
        }


        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getStoreIds())) {
            if (reqVO.getStoreIds().size() > 0) {
                queryWrapper.notIn(SystemStoreInfoDO::getStoreId, reqVO.getStoreIds());
            }

        }


//        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getTagId())) {
//            if (reqVO.getTagId().size() > 0) {
//                LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
//                wrapper.in(SystemStoreTagDO::getTagId, reqVO.getTagId());
//                List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);
//                if (systemStoreTagDOS.size() > 0) {
//                    List<Long> collect = systemStoreTagDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
//                    queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);
//                }
//                if (systemStoreTagDOS.size() == 0) {
//                    return new ArrayList<>();
//                }
//            }
//
//        }
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getTagId())) {
            if (reqVO.getTagId().size() > 0) {
                if(ObjectUtil.isNotEmpty(reqVO.getType())){
                    if (reqVO.getType().equals(2)) {
                        // type=2: 满足任意一个标签即可
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, reqVO.getTagId());
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            return new ArrayList<>();
                        }

                        List<Long> storeIds = systemStoreTagDOS.stream()
                                .map(SystemStoreTagDO::getStoreId)
                                .distinct()
                                .collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);

                    } else if (reqVO.getType().equals(1)) {
                        // type=1: 必须满足所有标签
                        List<Long> requiredTagIds = reqVO.getTagId();

                        // 查询包含任意一个传入标签的门店标签关系
                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getTagId, requiredTagIds);
                        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                        if (systemStoreTagDOS.isEmpty()) {
                            return new ArrayList<>();
                        }

                        // 按门店ID分组，收集每个门店拥有的标签ID集合
                        Map<Long, Set<Long>> storeTagsMap = systemStoreTagDOS.stream()
                                .collect(Collectors.groupingBy(
                                        SystemStoreTagDO::getStoreId,
                                        Collectors.mapping(SystemStoreTagDO::getTagId, Collectors.toSet())
                                ));

                        // 筛选出拥有所有传入标签的门店ID
                        List<Long> storeIds = storeTagsMap.entrySet().stream()
                                .filter(entry -> {
                                    Set<Long> storeTagIds = entry.getValue();
                                    // 检查门店的标签集合是否包含所有要求的标签
                                    return storeTagIds.containsAll(requiredTagIds);
                                })
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        if (storeIds.isEmpty()) {
                            return new ArrayList<>();
                        }

                        queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                        //无标签
                    }else   if(reqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            return new ArrayList<>();
                        }

                    }
                }else {
                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getTagId, reqVO.getTagId());
                    List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);

                    if (systemStoreTagDOS.isEmpty()) {
                        return new ArrayList<>();
                    }

                    List<Long> storeIds = systemStoreTagDOS.stream()
                            .map(SystemStoreTagDO::getStoreId)
                            .distinct()
                            .collect(Collectors.toList());
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
                }

            }else {
                if(ObjectUtil.isNotEmpty(reqVO.getType())){
                    if(reqVO.getType().equals(3)){

                        List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                        List<Long> storeIdList = allStoreList.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());


                        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                        wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                        List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                        Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                                .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                        List<StoreResVO> storeResVOS = allStoreList.stream()
                                .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                                .map(store -> {
                                    StoreResVO vo = new StoreResVO();
                                    BeanUtils.copyProperties(store, vo);
                                    return vo;
                                })
                                .collect(Collectors.toList());

                        if(ObjectUtil.isNotEmpty(storeResVOS)){
                            List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                        }else{
                            return new ArrayList<>();
                        }

                    }
                }
            }
        }else {
            if (ObjectUtil.isNotEmpty(reqVO.getType())) {
                if (reqVO.getType().equals(3)) {

                    List<StoreInfoDTO> allStoreList = systemStoreInfoService.getAllStoreList();

                    List<Long> storeIdList = allStoreList.stream()
                            .map(StoreInfoDTO::getStoreId)
                            .collect(Collectors.toList());


                    LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SystemStoreTagDO::getStoreId, storeIdList);
                    List<SystemStoreTagDO> allTags = systemStoreTagMapper.selectList(wrapper);


                    Map<Long, List<SystemStoreTagDO>> tagsByStoreId = allTags.stream()
                            .collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId));


                    List<StoreResVO> storeResVOS = allStoreList.stream()
                            .filter(store -> !tagsByStoreId.containsKey(store.getStoreId()))
                            .map(store -> {
                                StoreResVO vo = new StoreResVO();
                                BeanUtils.copyProperties(store, vo);
                                return vo;
                            })
                            .collect(Collectors.toList());

                    if (ObjectUtil.isNotEmpty(storeResVOS)) {
                        List<Long> collect = storeResVOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                        queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);

                    } else {
                        return new ArrayList<>();
                    }

                }
            }
        }



        queryWrapper.orderByDesc(SystemStoreInfoDO::getCreateTime);


        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
//        if (!org.springframework.util.StringUtils.isEmpty(list)) {
//            if (list.size() > 0) {
//                for (SystemStoreInfoDO systemStoreInfoDO : list) {
//
//                    if (!org.springframework.util.StringUtils.isEmpty(systemStoreInfoDO.getOrgId())) {
//                        LambdaQueryWrapper<OrgDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//                        lambdaQueryWrapper.eq(OrgDO::getId, systemStoreInfoDO.getOrgId());
//                        OrgDO orgDO = orgMapper.selectOne(lambdaQueryWrapper);
//                        if (orgDO != null) {
//                            systemStoreInfoDO.setOrgName(orgDO.getName());
//                        }
//
//                    }
//
//                }
//            }
//        }

        return list;
    }

    @Override
    public List<SystemStoreInfoDO> selectDouyinAllStore(StorePageListReqVO reqVO) {
        //        Set<SystemStoreInfoDO> byStoreList = getByStoreList(pageReqVO.getUserIds());
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.isNotNull(SystemStoreInfoDO::getTiktokId);
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getOrgIds())) {

            if (reqVO.getOrgIds().size() > 0) {
                Set<SystemStoreInfoDO> systemStoreSet = new HashSet<>();
                for (Long orgId : reqVO.getOrgIds()) {
                    Set<Long> orgListIds = orgMapper.getChildIdListByStore(orgId);


                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.in(SystemStoreInfoDO::getOrgId, orgListIds);
                    List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                    systemStoreSet.addAll(systemStoreInfoDOS);
                }
                if (ObjectUtil.isNotEmpty(systemStoreSet)) {
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, systemStoreSet.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList()));
                }

            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getOpenStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getOpenStatus, reqVO.getOpenStatus());
        }
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getStoreStatus())) {
            queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, reqVO.getStoreStatus());
        }
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getUserId())) {
            queryWrapper.eq(SystemStoreInfoDO::getUserId, reqVO.getUserId());
        }
        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getStoreName())) {
            queryWrapper.like(SystemStoreInfoDO::getStoreName, reqVO.getStoreName());
        }

//        if(ObjectUtil.isNotEmpty(byStoreList)){
//            queryWrapper.in(SystemStoreInfoDO::getStoreId,byStoreList.stream().map(mm->mm.getStoreId()).collect(Collectors.toList()));
//        }

        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getStoreIds())) {
            if (reqVO.getStoreIds().size() > 0) {
                queryWrapper.notIn(SystemStoreInfoDO::getStoreId, reqVO.getStoreIds());
            }

        }


        if (!org.springframework.util.StringUtils.isEmpty(reqVO.getTagId())) {
            if (reqVO.getTagId().size() > 0) {
                LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(SystemStoreTagDO::getTagId, reqVO.getTagId());
                List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(wrapper);
                if (systemStoreTagDOS.size() > 0) {
                    List<Long> collect = systemStoreTagDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                    queryWrapper.in(SystemStoreInfoDO::getStoreId, collect);
                }
                if (systemStoreTagDOS.size() == 0) {
                    return new ArrayList<>();
                }
            }

        }
        queryWrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        queryWrapper.orderByDesc(SystemStoreInfoDO::getCreateTime);

        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);

        return list;
    }

    @Override
    public List<StoreSimpleResDto> getStoreListByChannelId(String channelType, List<String> channelIds) {
        List<StoreSimpleResDto> list = new ArrayList<>();
        LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ChannelType.ELE_ME.getCode().equals(channelType)) {
            lambdaQueryWrapper.in(SystemStoreInfoDO::getHungryId, channelIds);
        }
        if (ChannelType.SAN_KUAI.getCode().equals(channelType)) {
            lambdaQueryWrapper.in(SystemStoreInfoDO::getMeituanId, channelIds);
        }
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(systemStoreInfoDOS)) {
            list = BeanCopyUtils.copyBeanList(systemStoreInfoDOS, StoreSimpleResDto.class);
        }
        return list;
    }
    @Override
    public StoreSimpleResVO getCheckOutId(int type,String outId,String storeId) {
        StoreSimpleResVO storeSimpleResVO = new StoreSimpleResVO();
        LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(storeId)) {
            lambdaQueryWrapper.ne(SystemStoreInfoDO::getStoreId, storeId);
        }
        switch (type) {
            case 1:
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getTiktokId, outId);
                break;
            case 2:
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getMeituanId, outId);
                break;
            case 3:
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getHungryId, outId);
                break;
        }
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
        storeSimpleResVO = BeanCopyUtils.copyBean(systemStoreInfoDO, StoreSimpleResVO.class);
        return storeSimpleResVO;
    }
    public List<StoreResVO> getStoreListByUserId(Long userId) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemStoreInfoDO::getUserId, userId);
        queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        // 只查询hbgc的店 写死
        queryWrapper.eq(SystemStoreInfoDO::getBusinessId, 10);
        //queryWrapper.eq(SystemStoreInfoDO::getUseStatus, 0);
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(queryWrapper);
        return BeanUtils.toBean(list, StoreResVO.class);
    }

    @DataPermission(enable = false)
    @Override
    public List<SystemStoreInfoDO> getStoreDOListByUserId(Long userId) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemStoreInfoDO::getUserId, userId);
        queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        queryWrapper.eq(SystemStoreInfoDO::getDeleted, 0);
        return systemStoreInfoMapper.selectList(queryWrapper);
    }

    @Override
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_UPDATE_SUB_TYPE_BOSS, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_UPDATE_SUCCESS)
    public void updateStoreByBoss(StoreSaveReqVO storeUserSaveReqVO) {
        SystemStoreInfoDO systemStoreInfoOldDO = systemStoreInfoMapper.selectById(storeUserSaveReqVO.getStoreId());
        if (storeUserSaveReqVO.getStoreStatus().equals(StoreStatusEnum.SHUTDOWN.getStatus()) && storeUserSaveReqVO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus()) && systemStoreInfoOldDO.getOpenStatus().equals(StoreStatusEnum.OPEN.getStatus())) {
            throw exception(STORE_STOP_EXISTS);
        }
        SystemStoreInfoDO systemStoreInfoDO = BeanUtils.toBean(storeUserSaveReqVO, SystemStoreInfoDO.class);


        // 批量插入配送范围和费用信息
        validateStoreExpensesForUpdate(storeUserSaveReqVO);
        insertBatchData(storeUserSaveReqVO, systemStoreInfoDO.getStoreId(), false);

        systemStoreInfoMapper.updateById(systemStoreInfoDO);


        //创建交换机
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(storeUserSaveReqVO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", storeUserSaveReqVO);
        this.createExchange(systemStoreInfoDO.getStoreId());
        refreshStoreBusinessStatusCache(systemStoreInfoDO);
        storeCityListCacheService.refreshStoreAfterCommit(systemStoreInfoDO.getStoreId(),
                systemStoreInfoOldDO.getStoreCity(), systemStoreInfoDO.getStoreCity(), false);

    }

    @Override
    public Boolean isTag(Long storeId, Long tagId) {
        LambdaQueryWrapper<SystemStoreTagDO> doLambdaQueryWrapper = new LambdaQueryWrapper<>();
        doLambdaQueryWrapper.eq(SystemStoreTagDO::getStoreId,storeId);
        doLambdaQueryWrapper.eq(SystemStoreTagDO::getTagId,tagId);
        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(doLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(systemStoreTagDOS)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public List<StoreInfoDTO> selectAdvertisingStoreList(List<Long> tagList) {
        LambdaQueryWrapper<SystemStoreTagDO> doLambdaQueryWrapper = new LambdaQueryWrapper<>();
        doLambdaQueryWrapper.in(SystemStoreTagDO::getTagId,tagList);
        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(doLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(systemStoreTagDOS)){
            List<Long> ids = systemStoreTagDOS.stream()
                    .map(mm -> mm.getStoreId())
                    .distinct()  // 去重
                    .collect(Collectors.toList());
            LambdaQueryWrapper<SystemStoreInfoDO> systemStoreInfoDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            systemStoreInfoDOLambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId,ids);
            systemStoreInfoDOLambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreStatus,0);
            systemStoreInfoDOLambdaQueryWrapper.eq(SystemStoreInfoDO::getOpenStatus,0);
            List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(systemStoreInfoDOLambdaQueryWrapper);
            if(ObjectUtil.isNotEmpty(list)){
                List<StoreInfoDTO> storeInfoDTOS = BeanCopyUtils.copyBeanList(list, StoreInfoDTO.class);
                return storeInfoDTOS;
            }

        }
        return new ArrayList<>();
    }
    /**
     * 根据当前登入人获取门店
     */
    @Override
    public List<StoreSimpleResVO> storeGylListByUser() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SystemStoreInfoDO> storeInfoList = systemStoreInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .eq(SystemStoreInfoDO::getUserId, userId)
                        .eq(SystemStoreInfoDO::getUseStatus, 1)
                        .eq(SystemStoreInfoDO::getDeleted, 0)
        );
        List<StoreSimpleResVO> result = BeanUtils.toBean(storeInfoList, StoreSimpleResVO.class);
        Map<Long, SystemStoreExpensesDO> campusDeliveryExpenseMap = getStoreExpensesMap(
                storeInfoList.stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toSet()),
                STORE_EXPENSE_TYPE_CAMPUS_DELIVERY);
        result.forEach(item -> fillCampusDeliveryFields(item, campusDeliveryExpenseMap.get(item.getStoreId())));
        Map<Long, String> backgroundImages = storeBackgroundCacheService.getBackgroundImages(
                result.stream().map(StoreSimpleResVO::getStoreId).toList());
        result.forEach(item -> item.setStoreBackgroundImage(backgroundImages.get(item.getStoreId())));
        return result;
    }
    @Override
    @LogRecord(type = SYSTEM_STORE_TYPE, subType = SYSTEM_STORE_UPDATE_SUB_TYPE_BOSS, bizNo = "{{#store.storeId}}",
            success = SYSTEM_STORE_UPDATE_SUCCESS)
    public void updateStoreStateByBoss(StoreUpdateStateReqVO storeUserSaveReqVO) {
        SystemStoreInfoDO systemStoreInfoOldDO = systemStoreInfoMapper.selectById(storeUserSaveReqVO.getStoreId());
        if (ObjectUtil.isEmpty(systemStoreInfoOldDO)) {
            throw exception(STORE_NOT_EXISTS);
        }
        validateTakeawayConfigByStore(systemStoreInfoOldDO);
        systemStoreInfoOldDO.setOpenStatus(storeUserSaveReqVO.getOpenStatus());
        systemStoreInfoMapper.updateById(systemStoreInfoOldDO);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(systemStoreInfoOldDO, StoreSaveReqVO.class));
        LogRecordContext.putVariable("store", systemStoreInfoOldDO);
        this.createExchange(systemStoreInfoOldDO.getStoreId());
        refreshStoreBusinessStatusCache(systemStoreInfoOldDO);
        storeCityListCacheService.refreshStoreAfterCommit(systemStoreInfoOldDO.getStoreId(),
                systemStoreInfoOldDO.getStoreCity(), systemStoreInfoOldDO.getStoreCity(), false);

    }

    @Override
    public StoreBusinessStatusRespVO getStoreBusinessStatus(Long storeId) {
        StoreBusinessStatusCache cache = getStoreBusinessStatusCache(storeId);
        List<String> storeHoursList = splitTimePeriods(cache.getStoreHours());
        List<String> deliveryTimeList = splitTimePeriods(cache.getDeliveryTime());

        boolean storeOpen = Objects.equals(cache.getStoreStatus(), StoreStatusEnum.OPEN.getStatus())
                && Objects.equals(cache.getOpenStatus(), StoreStatusEnum.OPEN.getStatus())
                && isNowInPeriods(cache.getStoreHours(), true);
        boolean takeawayOpen = storeOpen
                && Objects.equals(cache.getStoreTakeaway(), StoreStatusEnum.OPEN.getStatus())
                && isNowInPeriods(cache.getDeliveryTime(), true);
        boolean errandOpen = storeOpen
                && Objects.equals(cache.getCampusDeliveryStatus(), STATUS_ENABLE);

        StoreBusinessStatusRespVO respVO = new StoreBusinessStatusRespVO();
        respVO.setStoreId(storeId);
        respVO.setStoreOpen(storeOpen);
        respVO.setStoreHoursList(storeHoursList);
        respVO.setTakeawayOpen(takeawayOpen);
        respVO.setDeliveryTimeList(deliveryTimeList);
        respVO.setErrandOpen(errandOpen);
        respVO.setCampusDeliveryStatus(cache.getCampusDeliveryStatus());
        return respVO;
    }

    private StoreBusinessStatusCache getStoreBusinessStatusCache(Long storeId) {
        String key = formatStoreBusinessStatusCacheKey(storeId);
        StoreBusinessStatusCache cache = RedissonUtils.getCacheObject(key);
        if (cache != null && cache.getCampusDeliveryStatus() != null) {
            return cache;
        }
        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectById(storeId);
        if (storeInfoDO == null) {
            throw exception(STORE_NOT_EXISTS);
        }
        return refreshStoreBusinessStatusCache(storeInfoDO);
    }

    private StoreBusinessStatusCache refreshStoreBusinessStatusCache(SystemStoreInfoDO storeInfoDO) {
        StoreBusinessStatusCache cache = new StoreBusinessStatusCache();
        cache.setStoreId(storeInfoDO.getStoreId());
        cache.setStoreStatus(storeInfoDO.getStoreStatus());
        cache.setOpenStatus(storeInfoDO.getOpenStatus());
        cache.setStoreHours(storeInfoDO.getStoreHours());
        cache.setStoreTakeaway(storeInfoDO.getStoreTakeaway());
        cache.setDeliveryTime(storeInfoDO.getDeliveryTime());
        cache.setCampusDeliveryStatus(ObjectUtil.defaultIfNull(storeInfoDO.getCampusDeliveryStatus(), STATUS_DISABLE));
        RedissonUtils.setCacheObject(formatStoreBusinessStatusCacheKey(storeInfoDO.getStoreId()), cache);
        return cache;
    }

    private String formatStoreBusinessStatusCacheKey(Long storeId) {
        return String.format(STORE_BUSINESS_STATUS_CACHE_KEY, storeId);
    }

    private List<String> splitTimePeriods(String periods) {
        if (periods == null || periods.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(periods.replaceAll("\\s+", "").split(","))
                .filter(period -> period != null && !period.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private boolean isNowInPeriods(String periods, boolean emptyDefault) {
        List<String> timePeriods = splitTimePeriods(periods);
        if (CollectionUtils.isEmpty(timePeriods)) {
            return emptyDefault;
        }
        LocalTime currentTime = LocalTime.now();
        for (String period : timePeriods) {
            String[] timeParts = period.split("-");
            if (timeParts.length != 2) {
                continue;
            }
            try {
                LocalTime startTime = LocalTime.parse(timeParts[0], STORE_TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(timeParts[1], STORE_TIME_FORMATTER);
                if (isTimeInPeriod(currentTime, startTime, endTime)) {
                    return true;
                }
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

    private boolean isTimeInPeriod(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        }
        return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
    }

    @Data
    private static class StoreBusinessStatusCache implements Serializable {
        @Serial
        private static final long serialVersionUID = 7145487892158175585L;
        private Long storeId;
        private Integer storeStatus;
        private Integer openStatus;
        private String storeHours;
        private Integer storeTakeaway;
        private String deliveryTime;
        private Integer campusDeliveryStatus;
    }

    private void validateTakeawayConfig(Integer storeTakeaway,
                                        List<SystemStoreDeliveryScopeVO> systemStoreDeliveryScopeList) {
        if (!Objects.equals(storeTakeaway, StoreStatusEnum.OPEN.getStatus())) {
            return;
        }
        if (CollectionUtil.isEmpty(systemStoreDeliveryScopeList)) {
            throw exception(STORE_DELIVERY_SCOPE_EMPTY);
        }
    }

    private void validateTakeawayConfigByStore(SystemStoreInfoDO storeInfoDO) {
        if (storeInfoDO == null) {
            return;
        }
        List<SystemStoreExpensesDO> expensesList = systemStoreExpensesMapper.selectList(
                new LambdaQueryWrapper<SystemStoreExpensesDO>()
                        .eq(SystemStoreExpensesDO::getStoreId, storeInfoDO.getStoreId()));
        List<SystemStoreExpensesVO> expenseVOList = BeanUtils.toBean(expensesList, SystemStoreExpensesVO.class);
        if (CollectionUtil.isNotEmpty(expenseVOList)) {
            validateStoreExpenses(expenseVOList, false);
        }
        if (!Objects.equals(storeInfoDO.getStoreTakeaway(), StoreStatusEnum.OPEN.getStatus())) {
            return;
        }
        List<SystemStoreDeliveryScopeDO> deliveryScopeList = systemStoreDeliveryScopeMapper.selectList(
                new LambdaQueryWrapper<SystemStoreDeliveryScopeDO>()
                        .eq(SystemStoreDeliveryScopeDO::getStoreId, storeInfoDO.getStoreId()));
        if (CollectionUtil.isEmpty(deliveryScopeList)) {
            throw exception(STORE_DELIVERY_SCOPE_EMPTY);
        }
    }

    private void fillCampusDeliveryDefaults(StoreSaveReqVO storeSaveReqVO) {
        if (storeSaveReqVO.getCampusDeliveryStatus() == null) {
            storeSaveReqVO.setCampusDeliveryStatus(STATUS_DISABLE);
        }
        if (storeSaveReqVO.getCampusDeliverySubsidy() == null) {
            storeSaveReqVO.setCampusDeliverySubsidy(BigDecimal.ZERO);
        }
    }

    private void validateCampusDeliveryConfig(StoreSaveReqVO storeSaveReqVO) {
        if (Objects.equals(storeSaveReqVO.getStoreWithoutPayment(), STATUS_ENABLE)
                && Objects.equals(storeSaveReqVO.getCampusDeliveryStatus(), STATUS_ENABLE)) {
            throw exception(STORE_CAMPUS_DELIVERY_WITHOUT_PAYMENT_CONFLICT);
        }
    }

    private void validateStoreExpensesForCreate(StoreSaveReqVO storeSaveReqVO) {
        List<SystemStoreExpensesVO> systemStoreExpensesList = deduplicateStoreExpenses(storeSaveReqVO.getSystemStoreExpensesList());
        validateStoreExpenses(systemStoreExpensesList, true);

        SystemStoreExpensesVO campusDeliveryExpense = getCampusDeliveryExpense(systemStoreExpensesList);
        if (Objects.equals(storeSaveReqVO.getCampusDeliveryStatus(), STATUS_ENABLE)
                && campusDeliveryExpense == null) {
            throw exception(STORE_CAMPUS_DELIVERY_EXPENSE_EMPTY);
        }
        if (campusDeliveryExpense == null) {
            return;
        }
        BigDecimal minimumDeliveryFee = campusDeliveryExpense.getMinimumDeliveryFee() == null
                ? BigDecimal.ZERO : campusDeliveryExpense.getMinimumDeliveryFee();
        BigDecimal campusDeliverySubsidy = storeSaveReqVO.getCampusDeliverySubsidy() == null
                ? BigDecimal.ZERO : storeSaveReqVO.getCampusDeliverySubsidy();
        if (campusDeliverySubsidy.compareTo(new BigDecimal("20")) > 0) {
            throw exception(STORE_CAMPUS_DELIVERY_SUBSIDY_INVALID);
        }
        if (minimumDeliveryFee.compareTo(campusDeliverySubsidy) < 0) {
            throw exception(STORE_CAMPUS_DELIVERY_FEE_INVALID);
        }
    }

    private void validateStoreExpensesForUpdate(StoreSaveReqVO storeSaveReqVO) {
        validateCampusDeliverySubsidy(storeSaveReqVO);
        List<SystemStoreExpensesVO> systemStoreExpensesList = deduplicateStoreExpenses(storeSaveReqVO.getSystemStoreExpensesList());
        if (CollectionUtil.isEmpty(systemStoreExpensesList)) {
            return;
        }
        validateStoreExpenses(systemStoreExpensesList, false);
        SystemStoreExpensesVO campusDeliveryExpense = getCampusDeliveryExpense(systemStoreExpensesList);
        if (campusDeliveryExpense == null && storeSaveReqVO.getStoreId() != null) {
            campusDeliveryExpense = BeanUtils.toBean(getStoreExpenses(storeSaveReqVO.getStoreId(), STORE_EXPENSE_TYPE_CAMPUS_DELIVERY), SystemStoreExpensesVO.class);
        }
        validateCampusDeliveryFee(storeSaveReqVO, campusDeliveryExpense);
    }

    private void validateCampusDeliverySubsidy(StoreSaveReqVO storeSaveReqVO) {
        BigDecimal campusDeliverySubsidy = storeSaveReqVO.getCampusDeliverySubsidy() == null
                ? BigDecimal.ZERO : storeSaveReqVO.getCampusDeliverySubsidy();
        if (campusDeliverySubsidy.compareTo(new BigDecimal("20")) > 0) {
            throw exception(STORE_CAMPUS_DELIVERY_SUBSIDY_INVALID);
        }
    }

    private void validateCampusDeliveryFee(StoreSaveReqVO storeSaveReqVO, SystemStoreExpensesVO campusDeliveryExpense) {
        if (campusDeliveryExpense == null) {
            return;
        }
        BigDecimal minimumDeliveryFee = campusDeliveryExpense.getMinimumDeliveryFee() == null
                ? BigDecimal.ZERO : campusDeliveryExpense.getMinimumDeliveryFee();
        BigDecimal campusDeliverySubsidy = storeSaveReqVO.getCampusDeliverySubsidy() == null
                ? BigDecimal.ZERO : storeSaveReqVO.getCampusDeliverySubsidy();
        if (minimumDeliveryFee.compareTo(campusDeliverySubsidy) < 0) {
            throw exception(STORE_CAMPUS_DELIVERY_FEE_INVALID);
        }
    }

    /**
     * 费用类型重复时保留最后一条，保证校验和保存使用同一份数据。
     */
    private List<SystemStoreExpensesVO> deduplicateStoreExpenses(List<SystemStoreExpensesVO> systemStoreExpensesList) {
        if (CollectionUtil.isEmpty(systemStoreExpensesList)) {
            return Collections.emptyList();
        }
        List<SystemStoreExpensesVO> result = new ArrayList<>();
        Map<Integer, SystemStoreExpensesVO> expenseMap = new LinkedHashMap<>();
        for (SystemStoreExpensesVO item : systemStoreExpensesList) {
            if (item == null) {
                continue;
            }
            Integer type = item.getStoreExpensesType();
            if (type == null) {
                result.add(item);
                continue;
            }
            expenseMap.remove(type);
            expenseMap.put(type, item);
        }
        result.addAll(expenseMap.values());
        return result;
    }

    private void validateStoreExpenses(List<SystemStoreExpensesVO> systemStoreExpensesList, boolean requireAllTypes) {
        if (CollectionUtil.isEmpty(systemStoreExpensesList)) {
            throw exception(STORE_DELIVERY_EXPENSE_INVALID);
        }
        boolean hasNullType = systemStoreExpensesList.stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> item.getStoreExpensesType() == null);
        if (hasNullType) {
            throw exception(STORE_DELIVERY_EXPENSE_INVALID);
        }
        Set<Integer> typeSet = systemStoreExpensesList.stream()
                .filter(Objects::nonNull)
                .map(SystemStoreExpensesVO::getStoreExpensesType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        boolean hasInvalidType = typeSet.stream()
                .anyMatch(type -> !REQUIRED_STORE_EXPENSE_TYPES.contains(type)
                        && type != STORE_EXPENSE_TYPE_CAMPUS_DELIVERY);
        if (hasInvalidType) {
            throw exception(STORE_DELIVERY_EXPENSE_INVALID);
        }
        if (requireAllTypes && (!typeSet.containsAll(REQUIRED_STORE_EXPENSE_TYPES)
                || !typeSet.contains(STORE_EXPENSE_TYPE_CAMPUS_DELIVERY))) {
            throw exception(STORE_DELIVERY_EXPENSE_INVALID);
        }
    }

    private SystemStoreExpensesVO getCampusDeliveryExpense(List<SystemStoreExpensesVO> systemStoreExpensesList) {
        if (CollectionUtil.isEmpty(systemStoreExpensesList)) {
            return null;
        }
        return systemStoreExpensesList.stream()
                .filter(Objects::nonNull)
                .filter(item -> Objects.equals(item.getStoreExpensesType(), STORE_EXPENSE_TYPE_CAMPUS_DELIVERY))
                .findFirst()
                .orElse(null);
    }
    public PageResult<StoreInfoDTO> getStoreInfoByStoreIds(List<Long> storeIds, PageParam pageParam) {
        LambdaQueryWrapper<SystemStoreInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SystemStoreInfoDO::getStoreId,
                SystemStoreInfoDO::getStoreName);
        wrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
        PageResult<SystemStoreInfoDO> systemStoreInfoDOPageResult = systemStoreInfoMapper.selectPage(pageParam, wrapper);

        return BeanUtils.toBean(systemStoreInfoDOPageResult, StoreInfoDTO.class);
    }
    /**
     * 查询老板助手可访问的门店列表。
     *
     * <p>组织关系和门店关系各查询一次，避免重复访问关系表；普通门店和供应链门店
     * 分开查询，以保持原有数据权限和供应链筛选规则。</p>
     */
    @Override
    public List<StoreSimpleResVO> storeListByBoss() {
        long startNanos = System.nanoTime();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        List<OrgUserDO> orgRelations = userOrgMapper.selectList(
                new LambdaQueryWrapperX<OrgUserDO>()
                        .eq(OrgUserDO::getUserId, userId)
                        .eq(OrgUserDO::getDeleted, 0));
        Set<Long> userOrgIds = orgRelations.stream()
                .map(OrgUserDO::getOrgId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Integer> orgVisibilityMap = orgRelations.stream()
                .filter(relation -> relation.getOrgId() != null)
                .collect(Collectors.toMap(
                        OrgUserDO::getOrgId,
                        OrgUserDO::getVisible,
                        (existing, replacement) -> existing));
        long orgRelationMillis = elapsedMillis(startNanos);

        OrganizationHierarchy hierarchy = buildBossOrganizationHierarchy(userOrgIds);
        long hierarchyMillis = elapsedMillis(startNanos) - orgRelationMillis;

        List<StoreUserDO> storeRelations = storeUserMapper.selectList(
                new LambdaQueryWrapperX<StoreUserDO>()
                        .eq(StoreUserDO::getUserId, userId)
                        .eq(StoreUserDO::getDeleted, 0));
        Set<Long> userStoreIds = storeRelations.stream()
                .filter(relation -> Objects.equals(relation.getVisible(), 1))
                .map(StoreUserDO::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Integer> storeVisibilityMap = storeRelations.stream()
                .filter(relation -> relation.getStoreId() != null)
                .collect(Collectors.toMap(
                        StoreUserDO::getStoreId,
                        StoreUserDO::getVisible,
                        (existing, replacement) -> existing));
        long storeRelationMillis = elapsedMillis(startNanos) - orgRelationMillis - hierarchyMillis;

        List<SystemStoreInfoDO> normalStores = systemStoreInfoMapper.selectBossStoreList(
                hierarchy.getAllOrgIds(), userStoreIds);
        long normalStoreMillis = elapsedMillis(startNanos)
                - orgRelationMillis - hierarchyMillis - storeRelationMillis;

        Long businessId = BusinessContextHolder.getBusinessId();
        List<SystemStoreInfoDO> supplyStores = systemStoreInfoMapper
                .selectBossSupplyStoreList(userId, businessId);
        long supplyStoreMillis = elapsedMillis(startNanos)
                - orgRelationMillis - hierarchyMillis - storeRelationMillis - normalStoreMillis;

        Map<Long, SystemStoreInfoDO> deduplicatedStores = new LinkedHashMap<>(
                Math.max(16, normalStores.size() + supplyStores.size()));
        normalStores.forEach(store -> deduplicatedStores.putIfAbsent(store.getStoreId(), store));
        supplyStores.forEach(store -> deduplicatedStores.putIfAbsent(store.getStoreId(), store));

        List<StoreSimpleResVO> result = BeanUtils.toBean(
                new ArrayList<>(deduplicatedStores.values()), StoreSimpleResVO.class);
        for (StoreSimpleResVO vo : result) {
            Long storeId = vo.getStoreId();
            Long orgId = vo.getOrgId();
            if (storeVisibilityMap.containsKey(storeId)) {
                vo.setVisible(storeVisibilityMap.get(storeId));
                continue;
            }
            Integer visibility = hierarchy.getEffectiveVisibility(orgId, orgVisibilityMap);
            vo.setVisible(visibility != null ? visibility : 0);
        }

        long totalMillis = elapsedMillis(startNanos);
        logBossStoreListTiming(totalMillis, orgRelationMillis, hierarchyMillis, storeRelationMillis,
                normalStoreMillis, supplyStoreMillis, userOrgIds.size(), storeRelations.size(), result.size());
        return result;
    }

    /**
     * 一次查询用户直属组织及全部子组织，并构建可见性计算所需的层级结构。
     */
    private OrganizationHierarchy buildBossOrganizationHierarchy(Set<Long> userOrgIds) {
        if (CollectionUtils.isEmpty(userOrgIds)) {
            return new OrganizationHierarchy(Collections.emptyMap(), Collections.emptySet());
        }
        Map<Long, OrgDO> orgMap = orgMapper.selectSelfAndChildrenByIds(userOrgIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OrgDO::getId,
                        Function.identity(),
                        (existing, replacement) -> existing));
        return new OrganizationHierarchy(orgMap, userOrgIds);
    }

    /**
     * 记录老板助手门店列表各阶段耗时，慢请求使用警告日志方便线上定位。
     */
    private void logBossStoreListTiming(long totalMillis, long orgRelationMillis, long hierarchyMillis,
                                        long storeRelationMillis, long normalStoreMillis,
                                        long supplyStoreMillis, int orgRelationCount,
                                        int storeRelationCount, int resultCount) {
        String message = "老板助手门店列表查询耗时：总耗时={}ms，组织关系={}ms，组织层级={}ms，"
                + "门店关系={}ms，普通门店={}ms，供应链门店={}ms，关联组织数={}，"
                + "门店关系数={}，返回门店数={}";
        if (totalMillis >= BOSS_STORE_LIST_SLOW_LOG_MILLIS) {
            log.warn(message, totalMillis, orgRelationMillis, hierarchyMillis, storeRelationMillis,
                    normalStoreMillis, supplyStoreMillis, orgRelationCount, storeRelationCount, resultCount);
        } else if (log.isDebugEnabled()) {
            log.debug(message, totalMillis, orgRelationMillis, hierarchyMillis, storeRelationMillis,
                    normalStoreMillis, supplyStoreMillis, orgRelationCount, storeRelationCount, resultCount);
        }
    }

    /**
     * 计算从请求开始到当前时间的毫秒数。
     */
    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    /**
     * @param storeHours 营业时间（英文格式），示例："10:00-11:00, 13:00-14:00"
     * @return 0=正常营业，1=休息
     */
    public static int getOpenStatus(int openStatus,String storeHours) {
        DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 处理空值/空字符串
        if (storeHours == null || storeHours.trim().isEmpty()) {
            return openStatus;
        }

        String cleanHours = storeHours.replaceAll("\\s+", "").trim();
        // 按英文逗号分割多个营业时间段
        List<String> timePeriods = Arrays.asList(cleanHours.split(","));

        LocalTime currentTime = LocalTime.now();


        boolean isInAnyPeriod = false;

        for (String period : timePeriods) {
            // 按减号分割开始/结束时间
            String[] timeParts = period.split("-");
            if (timeParts.length != 2) {
                continue;
            }

            String startStr = timeParts[0];
            String endStr = timeParts[1];

            try {
                LocalTime startTime = LocalTime.parse(startStr, TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(endStr, TIME_FORMATTER);

                if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                    // 正常时间段：10:00-11:00 → currentTime 在 [start, end] 之间
                    if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                        isInAnyPeriod = true;
                        break;
                    }
                } else {
                    // 跨天时间段：22:00-02:00 → currentTime ≥ start 或 ≤ end
                    if (!currentTime.isBefore(startTime) || !currentTime.isAfter(endTime)) {
                        isInAnyPeriod = true;
                        break;
                    }
                }
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        if (!isInAnyPeriod) {
            openStatus = 1;
        }

        return openStatus;
    }

    public  void creatLog (SystemStoreInfoDO systemStoreInfoDO){
        //添加记录
        SystemStoreStatusLogDO systemStoreStatusLogDO = new SystemStoreStatusLogDO();
        systemStoreStatusLogDO.setStoreId(systemStoreInfoDO.getStoreId());
        systemStoreStatusLogDO.setStoreStatus(systemStoreInfoDO.getStoreStatus());
        systemStoreStatusLogMapper.insert(systemStoreStatusLogDO);
    }

    @DataPermission(enable = false)
    @Override
    public List<StoreInfoDTO> listStoresByIds(List<Long> storeIds) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getStoreId, storeIds)
//                .eq(SystemStoreInfoDO::getStoreStatus, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0);
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);
        return BeanUtils.toBean(storeInfoDOS, StoreInfoDTO.class);
    }

    @DataPermission(enable = false)
    @Override
    public List<StoreStatusLogDTO> listClosedLogs(List<Long> storeIds, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        LambdaQueryWrapper<SystemStoreStatusLogDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreStatusLogDO.class)
                .in(SystemStoreStatusLogDO::getStoreId, storeIds)
                .eq(SystemStoreStatusLogDO::getStoreStatus, 1)
                .eq(SystemStoreStatusLogDO::getDeleted, 0)
                .between(SystemStoreStatusLogDO::getCreateTime, startInclusive, endExclusive);
        List<SystemStoreStatusLogDO> storeInfoDOS = systemStoreStatusLogMapper.selectList(queryWrapper);
        return BeanUtils.toBean(storeInfoDOS, StoreStatusLogDTO.class);
    }

    @DataPermission(enable = false)
    @Override
    public List<StoreInfoDTO> listStoresByWarehouse(Long warehouseId, List<Long> storeIds) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getStoreId, storeIds)
                .eq(SystemStoreInfoDO::getWarehouseId, warehouseId)
                .eq(SystemStoreInfoDO::getStoreStatus, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0);
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoMapper.selectList(queryWrapper);
        return BeanUtils.toBean(storeInfoDOS, StoreInfoDTO.class);
    }

    @Override
    public Map<Long, List<StoreInfoDTO>> getStoreIdsByTagIds(List<Long> ids) {
        Map<Long, List<StoreInfoDTO>> result = new HashMap<>();

        LambdaQueryWrapper<SystemStoreTagDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreTagDO.class)
                .in(SystemStoreTagDO::getTagId, ids);
        List<SystemStoreTagDO> systemStoreTagDOS = systemStoreTagMapper.selectList(queryWrapper);
        if (ObjectUtil.isEmpty(systemStoreTagDOS)) {
            for (Long id : ids) {
                result.put(id, new ArrayList<>());
            }
            return result;
        }

        List<Long> storeIds = systemStoreTagDOS.stream().map(SystemStoreTagDO::getStoreId).distinct().toList();
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoMapper.selectByIds(storeIds);
        List<StoreInfoDTO> storeInfoDTOS = BeanUtils.toBean(storeInfoDOS, StoreInfoDTO.class);
        for (Long tagId : ids) {
            List<Long> storeIdsByTagId = systemStoreTagDOS.stream().filter(mm -> mm.getTagId().equals(tagId))
                    .map(SystemStoreTagDO::getStoreId)
                    .distinct()
                    .toList();
            List<StoreInfoDTO> storeInfos = storeInfoDTOS.stream().filter(mm -> storeIdsByTagId.contains(mm.getStoreId()))
                    .toList();
            result.put(tagId, storeInfos);
        }
        return result;
    }

    @Override
    public List<StoreLettersRespVO> getByLetterStoreList() {
        return null;
    }

    /**
     * 获取门店列表（支持模糊查询和指定门店ID）
     */
    public List<StoreLettersRespVO> getByLetterStoreList(StoreQueryDTO queryDTO) {
        List<StoreLettersRespVO> list = new ArrayList<>();

        if(queryDTO!=null){
            // 如果指定了 storeIds，优先使用指定门店列表
            if (CollectionUtil.isNotEmpty(queryDTO.getStoreIds())) {
                // 直接根据 storeIds 查询门店
                for (Long storeId : queryDTO.getStoreIds()) {
                    LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                    // 添加模糊查询条件
                    if (StringUtils.isNotBlank(queryDTO.getStoreName())) {
                        lambdaQueryWrapper.like(SystemStoreInfoDO::getStoreName, queryDTO.getStoreName());
                    }
                    SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                    if (systemStoreInfoDO != null) {
                        StoreLettersRespVO storeLettersRespVO = new StoreLettersRespVO();
                        storeLettersRespVO.setStoreName(systemStoreInfoDO.getStoreName());
                        storeLettersRespVO.setStoreId(systemStoreInfoDO.getStoreId());
                        storeLettersRespVO.setWarehouseId(systemStoreInfoDO.getWarehouseId());
                        list.add(storeLettersRespVO);
                    }
                }
                return list;
            }
        }


        // 原有逻辑：根据用户权限查询门店
        Long userId = WebFrameworkUtils.getLoginUserId();
        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        List<Long> userStoreIds = storeUserService.selectUserStoreIdsTwo(userId);

        // ... 后续保持原有逻辑不变
        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userStoreIds)) {
            storeOrgIds = systemStoreInfoMapper.selectStoreOrgIds(userStoreIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
        allOrgSet.addAll(orgList);

        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());

        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectByLetterStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));

        if (ObjectUtil.isNotEmpty(userStoreIds)) {
            List<SystemStoreInfoDO> listData = new ArrayList<>();
            for (Long storeId : userStoreIds) {
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId, storeId);
                // 添加模糊查询条件
                if(queryDTO!=null){
                    if (StringUtils.isNotBlank(queryDTO.getStoreName())) {
                        lambdaQueryWrapper.like(SystemStoreInfoDO::getStoreName, queryDTO.getStoreName());
                    }
                }

                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                if (systemStoreInfoDO != null) {
                    Long orgId = systemStoreInfoDO.getOrgId();
                    // 判断orgId是否在collect中存在，如果存在则跳过，不向下执行
                    if (collect.contains(orgId)) {
                        continue;  // 跳过当前循环，不添加到listData中
                    }
                    listData.add(systemStoreInfoDO);
                }
            }
            if (ObjectUtil.isNotEmpty(listData)) {
                Map<Long, List<SystemStoreInfoDO>> listMap = listData.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                for (Long aLong : listMap.keySet()) {
                    if (!orgIds.contains(aLong)) {
                        storeMap.put(aLong, listMap.get(aLong));
                    }
                }
            }
        }

         // 获取模糊查询条件
        String storeNameFilter = (queryDTO != null) ? queryDTO.getStoreName() : null;

        // 加入上级节点
        for (Long l : storeMap.keySet()) {
            List<SystemStoreInfoDO> stores = storeMap.get(l);
            if (ObjectUtil.isNotEmpty(stores)) {
                for (SystemStoreInfoDO store : stores) {
                    // 应用模糊查询过滤（修改这里）
                    if (StringUtils.isBlank(storeNameFilter) ||
                            store.getStoreName().contains(storeNameFilter)) {
                        StoreLettersRespVO storeLettersRespVO = new StoreLettersRespVO();
                        storeLettersRespVO.setStoreName(store.getStoreName());
                        storeLettersRespVO.setStoreId(store.getStoreId());
                        storeLettersRespVO.setWarehouseId(store.getWarehouseId());
                        list.add(storeLettersRespVO);
                    }
                }
            }
        }

        return list;
    }

    /**
     * 获取带字母索引的门店列表
     */
    public StoreListWithIndexVO getByLetterStoreListWithIndex(StoreQueryDTO queryDTO) {
        // 获取原始门店列表（支持模糊查询）
        List<StoreLettersRespVO> storeList = getByLetterStoreList(queryDTO);

        if (CollectionUtils.isEmpty(storeList)) {
            StoreListWithIndexVO emptyResult = new StoreListWithIndexVO();
            emptyResult.setLetters(new ArrayList<>());
            emptyResult.setGroupedStores(new LinkedHashMap<>());
            emptyResult.setStoreList(new ArrayList<>());
            return emptyResult;
        }

        // 为每个门店计算拼音首字母
        for (StoreLettersRespVO store : storeList) {
            String firstLetter = com.htyoudao.youdao.module.system.util.store.PinyinUtil.getStoreFirstLetter(store.getStoreName());
            store.setFirstLetter(firstLetter);
        }

        // 对 storeList 按字母排序
        storeList.sort(Comparator
                .comparing(StoreLettersRespVO::getFirstLetter, (a, b) -> {
                    // 自定义排序：A-Z 正常排序，# 排最后
                    if (a.equals("#") && !b.equals("#")) {
                        return 1;  // # 在后面
                    } else if (!a.equals("#") && b.equals("#")) {
                        return -1; // # 在后面
                    } else {
                        return a.compareTo(b);
                    }
                })
                .thenComparing(StoreLettersRespVO::getStoreName, Comparator.nullsLast(String::compareTo))
        );

        // 按字母分组（使用LinkedHashMap保持插入顺序）
        Map<String, List<StoreLettersRespVO>> groupedStores = storeList.stream()
                .collect(Collectors.groupingBy(
                        StoreLettersRespVO::getFirstLetter,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    // 每组内按门店名称排序
                                    list.sort(Comparator.comparing(StoreLettersRespVO::getStoreName,
                                            Comparator.nullsLast(String::compareTo)));
                                    return list;
                                }
                        )
                ));

        // 生成字母索引列表（A-Z）
        List<String> letters = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            String letter = String.valueOf(c);
            if (groupedStores.containsKey(letter)) {
                letters.add(letter);
            }
        }
        // 添加#分组（数字或特殊字符开头的）
        if (groupedStores.containsKey("#")) {
            letters.add("#");
        }

        // 构建返回结果
        StoreListWithIndexVO result = new StoreListWithIndexVO();
        result.setLetters(letters);
        result.setGroupedStores(groupedStores);
        result.setStoreList(storeList);

        return result;
    }

    public List<Long> getStoreIdsByUser() {
        List<StoreSimpleResVO> storeSimpleResVOS = this.getStoreListByUser();
        List<Long> storeIds = storeSimpleResVOS.stream()
                .filter(vo -> vo.getStoreStatus() != null && vo.getStoreStatus() == 0) // 过滤状态等于 1 的
                .map(StoreSimpleResVO::getStoreId)// 只提取 storeId 字段
                .distinct()
                .collect(Collectors.toList());
        return storeIds;
    }

    public List<StoreItemVO> getNearbyStores(String longitude, String latitude) {
        if(ObjectUtil.isEmpty(longitude) || ObjectUtil.isEmpty(latitude)){
            throw exception(STORE_NEAR_NO_POSITION);
        }
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        double[] doubles = CoordinateTransformUtil.wgs84ToGcj02(lon, lat);
        List<StoreSimpleResVO> storeSimpleResVOS = this.getStoreListByUser();
        List<Long> storeIds = storeSimpleResVOS.stream()
                .filter(vo -> vo.getStoreStatus() != null && vo.getStoreStatus() == 0) // 过滤状态等于 0 的
                .map(StoreSimpleResVO::getStoreId)// 只提取 storeId 字段
                .distinct()
                .collect(Collectors.toList());

        String geo = appMapApi.geocoderV3(longitude, latitude);
        JSONObject jsonObject = JSONObject.parseObject(geo);
        String adInfo = jsonObject.getString("ad_info");
        JSONObject jsonObject1 = JSONObject.parseObject(adInfo);
        String city = jsonObject1.getString("city");

        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .eq(SystemStoreInfoDO::getStoreCity, city)
                .in(SystemStoreInfoDO::getStoreId, storeIds);
        queryWrapper.select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getStoreName, SystemStoreInfoDO::getStoreLongitude, SystemStoreInfoDO::getStoreLatitude);
        List<SystemStoreInfoDO> systemStoreInfos = systemStoreInfoMapper.selectList(queryWrapper);
         lon = doubles[0];
         lat = doubles[1];
        double finalLon = lon;
        double finalLat = lat;
        List<StoreItemVO> list = systemStoreInfos.stream()
                .filter(sysStoreInfo -> {
                    // 1. 计算距离，仅用于判断是否在 1 公里内
                    double distance = calculateStoreDistance(finalLon, finalLat,
                            sysStoreInfo.getStoreLongitude(), sysStoreInfo.getStoreLatitude());
                    return distance <= 1.0;
                })
                .map(sysStoreInfo -> {
                    // 2. 只有距离 <= 1.0 的才会走到这里
                    StoreItemVO storeItemVO = new StoreItemVO();
                    storeItemVO.setStoreId(sysStoreInfo.getStoreId());
                    storeItemVO.setStoreName(sysStoreInfo.getStoreName());
                    return storeItemVO;
                })
                .toList(); // 或者 .collect(Collectors.toList())
        return list;
    }

    @Override
    public Boolean judgeDistance(String longitude, String latitude, Long storeId, double distance) {
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        double[] doubles = CoordinateTransformUtil.wgs84ToGcj02(lon, lat);
        lon = doubles[0];
        lat = doubles[1];
        return calculateStoreDistance(lon, lat,
                systemStoreInfoDO.getStoreLongitude(), systemStoreInfoDO.getStoreLatitude()) <= distance;
    }

    @Override
    public StoreBasicInfoRespVO getStoreBasicInfoById(Long storeId) {
        StoreBasicInfoRespVO storeBasicInfoRespVO = new StoreBasicInfoRespVO();
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
        storeBasicInfoRespVO.setStoreName(systemStoreInfoDO.getStoreName());
        storeBasicInfoRespVO.setStoreId(systemStoreInfoDO.getStoreId());
        storeBasicInfoRespVO.setStoreLeaderName(systemStoreInfoDO.getStoreLeader());
        storeBasicInfoRespVO.setStoreLeaderPhone(systemStoreInfoDO.getStoreLeaderPhone());
        storeBasicInfoRespVO.setStoreLeaderId(systemStoreInfoDO.getUserId());
        if(ObjectUtil.isNotEmpty(systemStoreInfoDO.getOrgId())){
            storeBasicInfoRespVO.setOrgId(systemStoreInfoDO.getOrgId());
            OrgDO orgDO = orgMapper.selectById(systemStoreInfoDO.getOrgId());
            storeBasicInfoRespVO.setOrgName(orgDO.getName());
            Long orgLeaderId = orgUserService.getOrgLeaderIdByOrgId(systemStoreInfoDO.getOrgId());
            if(ObjectUtil.isNotEmpty(orgLeaderId)){
                AdminUserDO adminUserDO = userMapper.selectById(orgLeaderId);
                if(ObjectUtil.isNotEmpty(adminUserDO)){
                    storeBasicInfoRespVO.setOrgLeaderId(adminUserDO.getId());
                    storeBasicInfoRespVO.setOrgLeaderName(adminUserDO.getNickname());
                    storeBasicInfoRespVO.setOrgLeaderPhone(adminUserDO.getMobile());
                }
            }
        }
        return storeBasicInfoRespVO;
    }
}

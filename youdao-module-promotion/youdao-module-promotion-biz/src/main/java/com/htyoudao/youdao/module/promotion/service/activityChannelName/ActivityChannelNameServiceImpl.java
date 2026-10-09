package com.htyoudao.youdao.module.promotion.service.activityChannelName;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.ChannelEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.ActivityChannelNamePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.ActivityChannelNameSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName.ActivityChannelNameDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityChannelName.ActivityChannelNameMapper;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.wechat.ShortUrlService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.ACTIVITY_CHANNEL_NAME_DUPLICATE;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.DEFAULT_CHANNEL_CANNOT_CHANGE_STATUS;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;

@Service
@Validated
@Slf4j
public class ActivityChannelNameServiceImpl implements ActivityChannelNameService {

    @Resource
    private ActivityChannelNameMapper activityChannelNameMapper;

    @Resource
    private ActivityChannelService activityChannelService;

    @Resource
    private ShortUrlService shortUrlService;

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_CHANNEL_NAME_TYPE,
            subType = PROMOTION_ACTIVITY_CHANNEL_NAME_CREATE_TYPE,
            bizNo = "{{#channel.id}}",
            success = PROMOTION_ACTIVITY_CHANNEL_NAME_CREATE_SUCCESS)
    public Long create(ActivityChannelNameSaveReqVO reqVO) {
        checkNameUnique(null, reqVO.getName());
        ActivityChannelNameDO dataObject = BeanUtils.toBean(reqVO, ActivityChannelNameDO.class);
        dataObject.setId(null); // 避免前端误传
        if (ObjectUtil.isEmpty(dataObject.getIsDefault())) {
            dataObject.setIsDefault(0);
        }
        activityChannelNameMapper.insert(dataObject);
        LogRecordContext.putVariable("channel", dataObject);
        return dataObject.getId();
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_CHANNEL_NAME_TYPE,
            subType = PROMOTION_ACTIVITY_CHANNEL_NAME_UPDATE_TYPE,
            bizNo = "{{#id}}",
            success = PROMOTION_ACTIVITY_CHANNEL_NAME_UPDATE_SUCCESS)
    public void update(ActivityChannelNameSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        checkNameUnique(reqVO.getId(), reqVO.getName());
        ActivityChannelNameDO old = activityChannelNameMapper.selectById(reqVO.getId());
        ActivityChannelNameDO dataObject = BeanUtils.toBean(reqVO, ActivityChannelNameDO.class);
        LogRecordContext.putVariable("channel", dataObject);
        LogRecordContext.putVariable("id", reqVO.getId());
        LogRecordContext.putVariable("oldName", old != null ? old.getName() : null);
        LogRecordContext.putVariable("newName", dataObject.getName());
        activityChannelNameMapper.updateById(dataObject);

        activityChannelService.updateNameByChannelId(reqVO.getId(), reqVO.getName());
    }

    /**
     * 校验渠道名称不重名。
     *
     * @param excludeId 排除的 id（更新时传当前记录 id，创建时传 null）
     * @param name      渠道名称
     */
    private void checkNameUnique(Long excludeId, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        LambdaQueryWrapper<ActivityChannelNameDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityChannelNameDO::getName, name.trim());
        if (excludeId != null) {
            wrapper.ne(ActivityChannelNameDO::getId, excludeId);
        }
        if (activityChannelNameMapper.selectCount(wrapper) > 0) {
            throw exception(ACTIVITY_CHANNEL_NAME_DUPLICATE);
        }
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_CHANNEL_NAME_TYPE,
            subType = PROMOTION_ACTIVITY_CHANNEL_NAME_STATUS_TYPE,
            bizNo = "{{#id}}",
            success = PROMOTION_ACTIVITY_CHANNEL_NAME_STATUS_SUCCESS)
    public void updateStatus(Long id, Integer isEnable) {
        ActivityChannelNameDO channel = activityChannelNameMapper.selectById(id);
        if (channel != null) {
            channel.setIsEnable(isEnable);
            LogRecordContext.putVariable("channel", channel);
            if (channel.getIsDefault()==1){
                throw new ServiceException(DEFAULT_CHANNEL_CANNOT_CHANGE_STATUS);
            }
        } else {
            ActivityChannelNameDO fallback = new ActivityChannelNameDO();
            fallback.setId(id);
            fallback.setIsEnable(isEnable);
            LogRecordContext.putVariable("channel", fallback);
        }
        LambdaUpdateWrapper<ActivityChannelNameDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ActivityChannelNameDO::getId, id);
        wrapper.set(ActivityChannelNameDO::getIsEnable, isEnable);
        activityChannelNameMapper.update(wrapper);

       //List<ActivityChannelDO> activityChannelDOList =  activityChannelService.selectByChannelId(id);
       List<ActivityChannelDO> activityChannelDOList =  activityChannelService.updateStatusByChannelId(id,isEnable);

       if (ObjectUtil.isNotEmpty(activityChannelDOList)){
           List<String> linkUrls = activityChannelDOList.stream()
                   .map(ActivityChannelDO::getLinkUrl)
                   .filter(Objects::nonNull)
                   .toList();
           List<String> shortCodes = extractShlinkShortCodes(linkUrls);
           if (ObjectUtil.isEmpty(shortCodes)) {
               return;
           }

           // 关闭=过期；开启=取消过期（兼容：非 0/1 直接跳过）
           if (Integer.valueOf(0).equals(isEnable)) {
               ShortUrlService.BatchUpdateResult result = shortUrlService.batchExpireShortUrls(shortCodes);
               if (ObjectUtil.isNotEmpty(result.getFailed())) {
                   log.warn("批量过期短链部分失败，channelId={}, successCount={}, failCount={}, failed={}",
                           id, result.getSuccess().size(), result.getFailed().size(), result.getFailed());
               }
           } else if (Integer.valueOf(1).equals(isEnable)) {
               ShortUrlService.BatchUpdateResult result = shortUrlService.batchUnexpireShortUrls(shortCodes);
               if (ObjectUtil.isNotEmpty(result.getFailed())) {
                   log.warn("批量取消过期短链部分失败，channelId={}, successCount={}, failCount={}, failed={}",
                           id, result.getSuccess().size(), result.getFailed().size(), result.getFailed());
               }
           }
       }

    }

    /**
     * 从 linkUrl 中解析出 Shlink shortCode。
     *
     * 兼容策略：
     * - 只接受 http/https 的绝对短链（避免把社群链接、sortPath 之类误当成 shortCode）
     * - 取 URL path 的最后一段作为 shortCode
     */
    private List<String> extractShlinkShortCodes(List<String> linkUrls) {
        List<String> shortCodes = new ArrayList<>();
        if (ObjectUtil.isEmpty(linkUrls)) {
            return shortCodes;
        }
        for (String linkUrl : linkUrls) {
            if (linkUrl == null || linkUrl.isBlank()) {
                continue;
            }
            try {
                URI uri = URI.create(linkUrl.trim());
                String scheme = uri.getScheme();
                if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                    continue; // 非绝对 URL，直接跳过（兼容）
                }
                String path = uri.getPath();
                if (path == null || path.isBlank()) {
                    continue;
                }
                // 去掉末尾 /
                String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                int idx = normalized.lastIndexOf('/');
                String code = idx >= 0 ? normalized.substring(idx + 1) : normalized;
                if (code == null || code.isBlank()) {
                    continue;
                }
                shortCodes.add(code);
            } catch (Exception ignore) {
                // 解析失败：兼容跳过
            }
        }
        return shortCodes.stream().distinct().toList();
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITY_CHANNEL_NAME_TYPE,
            subType = PROMOTION_ACTIVITY_CHANNEL_NAME_DELETE_TYPE,
            bizNo = "{{#id}}",
            success = PROMOTION_ACTIVITY_CHANNEL_NAME_DELETE_SUCCESS)
    public void delete(Long id) {
        ActivityChannelNameDO channel = activityChannelNameMapper.selectById(id);
        if (channel != null) {
            LogRecordContext.putVariable("channel", channel);
        } else {
            ActivityChannelNameDO fallback = new ActivityChannelNameDO();
            fallback.setId(id);
            LogRecordContext.putVariable("channel", fallback);
        }
        activityChannelNameMapper.deleteById(id);

        activityChannelService.deleteByChannelId(id);

    }

    @Override
    public ActivityChannelNameDO get(Long id) {
        return activityChannelNameMapper.selectById(id);
    }

    @Override
    public PageResult<ActivityChannelNameDO> page(ActivityChannelNamePageReqVO reqVO) {
        return activityChannelNameMapper.selectPage(reqVO);
    }

    @Override
    public List<ActivityChannelNameDataRespVO> getChannelList() {
        List<ActivityChannelNameDataRespVO> list = new ArrayList<>();
        //查出来默认的
        LambdaQueryWrapper<ActivityChannelNameDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ActivityChannelNameDO::getIsDefault,1);
        lambdaQueryWrapper.eq(ActivityChannelNameDO::getIsEnable,1);
        List<ActivityChannelNameDO> activityChannelNameDOS = activityChannelNameMapper.selectList(lambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(activityChannelNameDOS)){
            for (ActivityChannelNameDO activityChannelNameDO : activityChannelNameDOS) {
                ActivityChannelNameDataRespVO activityChannelNameDataRespVO = new ActivityChannelNameDataRespVO();
                activityChannelNameDataRespVO.setName(activityChannelNameDO.getName());
                activityChannelNameDataRespVO.setId(activityChannelNameDO.getId());
                list.add(activityChannelNameDataRespVO);
            }

        }
        //增加小程序
        ActivityChannelNameDataRespVO channelNameDataRespVO = new ActivityChannelNameDataRespVO();
        channelNameDataRespVO.setName(ChannelEnum.USER_MINI_PROGRAM.getDescription());
        Long userMiniProgramId = ChannelEnum.USER_MINI_PROGRAM.getId();
        channelNameDataRespVO.setId(userMiniProgramId);
        list.add(channelNameDataRespVO);


        //增加点餐机
        ActivityChannelNameDataRespVO nameDataRespVO = new ActivityChannelNameDataRespVO();
        nameDataRespVO.setName(ChannelEnum.ORDERING_MACHINE.getDescription());
        Long orderingMachineId = ChannelEnum.ORDERING_MACHINE.getId();
        nameDataRespVO.setId(orderingMachineId);
        list.add(nameDataRespVO);

        LambdaQueryWrapper<ActivityChannelNameDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityChannelNameDO::getIsDefault,0);
        wrapper.eq(ActivityChannelNameDO::getIsEnable,1);
        List<ActivityChannelNameDO> doList = activityChannelNameMapper.selectList(wrapper);
        if(ObjectUtil.isNotEmpty(doList)){
            for (ActivityChannelNameDO activityChannelNameDO : doList) {
                ActivityChannelNameDataRespVO dataRespVO = new ActivityChannelNameDataRespVO();
                dataRespVO.setName(activityChannelNameDO.getName());
                dataRespVO.setId(activityChannelNameDO.getId());
                list.add(dataRespVO);
            }

        }

        return list;
    }

    @Override
    public Map<Long, String> selectChannelNameMap() {
        Map<Long, String> channelNameMap = new HashMap<>(16);
        List<ActivityChannelNameDO> channelNames = activityChannelNameMapper.selectList();
        if(CollectionUtils.isEmpty(channelNames)){
            channelNameMap.put(ChannelEnum.USER_MINI_PROGRAM.getId(),ChannelEnum.USER_MINI_PROGRAM.getDescription());
            channelNameMap.put(ChannelEnum.ORDERING_MACHINE.getId(),ChannelEnum.ORDERING_MACHINE.getDescription());
            return channelNameMap;
        }
        channelNameMap = channelNames.stream().collect(Collectors.toMap(ActivityChannelNameDO::getId, ActivityChannelNameDO::getName));
        channelNameMap.put(ChannelEnum.USER_MINI_PROGRAM.getId(),ChannelEnum.USER_MINI_PROGRAM.getDescription());
        channelNameMap.put(ChannelEnum.ORDERING_MACHINE.getId(),ChannelEnum.ORDERING_MACHINE.getDescription());
        return channelNameMap;
    }
}


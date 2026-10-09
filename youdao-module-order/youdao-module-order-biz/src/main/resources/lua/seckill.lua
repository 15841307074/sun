-- 秒杀 Lua 脚本（多商品 + 排队机制 + 活动限购）
-- KEYS:

-- 1: 并发计数 Key               seckill:concurrent:{activityId}:{sessionId}
-- 2: 排队队列 Key               seckill:queue:{activityId}:{sessionId}
-- 3: 门店订单数限购 Key          seckill:store:buy:{activityId}:{sessionId}:{storeId}
-- 后续 KEYS 动态：每个商品2个Key
--    stockKey                   seckill:stock:{storeId}:{activityId}:{productId}:{sessionId}
--    userBuyKey                 seckill:user:buy:{activityId}:{sessionId}:{productId}:{userId}

-- ARGV:
-- 1: TTL 到活动结束（秒）
-- 2: 当前时间 H
-- 3: 场次开始时间 H 也是场次ID
-- 4: 备用
-- 5: 最大并发数
-- 6: 并发锁过期时间（秒）
-- 7: 等待人数阈值
-- 8: 当前用户ID
-- 9: 当前门店ID
-- 10: 商品数量 N
-- 11: commodity ID 数组(JSON)
-- 12: 对应购买数量数组(JSON)
-- 13: 秒杀活动信息
-- 14: 商品信息

-- 读取活动信息
local activityJson = ARGV[13];
if not activityJson then
    return {1005010001, "当前门店未参加本次活动，请更换门店"}
end
local activity = cjson.decode(activityJson)
if not activity or tonumber(activity.isEnabled or 0) ~= 1 then
    return {1005010002, "活动已结束，下次记得早点来哦~"}
end

-- 场次校验
local nowHour = tonumber(ARGV[2])      -- 当前时间

-- 解析活动中的场次信息
local activityTimes = activity.activitySeckillTimeRespVOS
if not activityTimes or #activityTimes == 0 then
    return {1005010009, "活动场次配置异常，请稍后再试"}
end

local validSessionFound = false
for i = 1, #activityTimes do
    local session = activityTimes[i]
    if tostring(session.times) == tostring(ARGV[3]) then  -- 匹配当前场次
        validSessionFound = true
        local startHour = tonumber(session.startTime)
        local endHour = tonumber(session.endTime)
        if nowHour < startHour or nowHour >= endHour then
            return {1005010010, "当前场次未开始或已结束"}
        end
        break
    end
end

if not validSessionFound then
    return {1005010011, "无效的场次信息，请刷新活动后重试"}
end


-- 并发控制 + 排队机制
local concurrentKey  = KEYS[1]
local queueKey       = KEYS[2]
local maxConcurrent  = tonumber(ARGV[5])
local concurrentTTL  = tonumber(ARGV[6])
local waitThreshold  = tonumber(ARGV[7])
local userId         = tostring(ARGV[8])
local storeId        = tostring(ARGV[9])

local currentConcurrent = tonumber(redis.call("GET", concurrentKey) or "0")
if currentConcurrent >= maxConcurrent then
    local inQueue = redis.call("LPOS", queueKey, userId)
    if not inQueue then
        redis.call("RPUSH", queueKey, userId)
        redis.call("EXPIRE", queueKey, concurrentTTL)
        inQueue = redis.call("LLEN", queueKey) - 1
    end
    if inQueue >= waitThreshold then
        return {1005010004, "前方拥堵，请稍后重试~"}
    else
        return {1005010005, "排队中，前面还有 " .. inQueue .. " 人"}
    end
end
redis.call("INCR", concurrentKey)
redis.call("EXPIRE", concurrentKey, concurrentTTL)

-- 解析 commodity 与数量
local N = tonumber(ARGV[10])
local commodityIds = cjson.decode(ARGV[11])
local buyCounts = cjson.decode(ARGV[12])
if not commodityIds or not buyCounts or #commodityIds ~= N or #buyCounts ~= N then
    return {1005003001, "商品参数错误"}
end

-- 获取门店限购
local storeLimit = tonumber(activity.storeLimitCount or 0)
if storeLimit > 0 then
    -- 门店限购只校验一次
    local storeBuyKey = KEYS[3]
    local storeBuyCount = tonumber(redis.call("GET", storeBuyKey) or "0")
    if storeBuyCount >= storeLimit then
        return {1005010006, "当前门店活动名额已满，请更换门店下单"}
    end
end

-- 批量校验库存 & 单品限购
for i = 1, N do
    local commodityId = tostring(commodityIds[i])
    local buyCount = tonumber(buyCounts[i]) or 0

    -- 找到商品
    local limitPerItem = 0
    local commodityName = ""
--     local commodityFound = false
    local commoditieJson = ARGV[14]
    local commodities = cjson.decode(commoditieJson)

    for j = 1, #commodities do
        if tostring(commodities[j].commodityId or 0) == commodityId then
            limitPerItem = tonumber(commodities[j].limitBuyNumber or 0)
            commodityName = tostring(commodities[j].spuName)
--             commodityFound = true
            break
        end
    end

--     if not commodityFound then
--         return {1005003011, "商品 [" .. commodityName .. "] 已下架,请刷新后再试"}
--     end

    -- KEYS 顺序：从第4个开始，每个商品2个key
    local baseIndex = 3 + (i - 1) * 2
    local stockKey = KEYS[baseIndex + 1]
    local userBuyKey = KEYS[baseIndex + 2]

    -- 用户单品限购
    if limitPerItem > 0 then
        local userBuyCount = tonumber(redis.call("GET", userBuyKey) or "0")
        if userBuyCount + buyCount > limitPerItem then
            return {1005010007, "商品 [" .. commodityName .. "] 最多可购买 " .. limitPerItem .. " 份,请重新下单"}
        end
    end

    -- 库存校验
    local stock = tonumber(redis.call("GET", stockKey) or "0")
    if stock < buyCount then
        return {1005010008, "商品 [" .. commodityName .. "] 库存不足,请重新下单"}
    end
end


-- 扣减库存 & 更新购买记录
for i = 1, N do
    local buyCount = tonumber(buyCounts[i])
    local baseIndex = 3 + (i - 1) * 2
    local stockKey = KEYS[baseIndex + 1]
    local userBuyKey = KEYS[baseIndex + 2]

    redis.call("DECRBY", stockKey, buyCount)
    redis.call("INCRBY", userBuyKey, buyCount)
    redis.call("EXPIRE", userBuyKey, tonumber(ARGV[1]))
    redis.call("EXPIRE", stockKey, tonumber(ARGV[1]))
end

-- 门店总下单次数 +1（只加一次，不管买多少个品）
if storeLimit > 0 then
    local storeBuyKey = KEYS[3]
    redis.call("INCRBY", storeBuyKey, 1)
    redis.call("EXPIRE", storeBuyKey, tonumber(ARGV[1]))
end

-- 从队列中移除
redis.call("LREM", queueKey, 0, userId)

return {0, "OK"}

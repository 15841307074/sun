package com.htyoudao.youdao.module.member.dal.redis;


/**
 * member Redis Key 枚举类
 *
 * @author 0090
 */
public interface RedisKeyConstants {

    /**
     * 用户地址
     * KEY 格式：member_address:{username}
     * VALUE 数据格式 String
     */
    String MEMBER_ADDRESS_NAMESPACE = "member_address";

    /**
     * 迈云接口错误次数，KEY：member:map:maiyun:errors:{接口名}。
     * Hash field 为错误码（timeout 表示请求超时），value 为累计次数，不设置过期时间。
     */
    String MEMBER_MAP_MAIYUN_ERRORS = "member:map:maiyun:errors:";

}

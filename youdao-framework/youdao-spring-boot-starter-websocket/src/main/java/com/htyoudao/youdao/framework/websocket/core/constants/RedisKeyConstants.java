package com.htyoudao.youdao.framework.websocket.core.constants;

/**
 * <p>
 * 框架级别的Redis Key常量， 业务模块的KEY写在各自的模块中的RedisKeyConstants
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-13
 */
public interface RedisKeyConstants {

    /**
     * im-server最大id,从0开始递增
     */
    String IM_MAX_SERVER_ID = "im:max_server_id";
    /**
     * 用户ID所连接的IM-server的ID
     */
    String IM_USER_SERVER_ID = "im:user:server_id";

    /**
     * 缓存群聊信息
     */
    String IM_CACHE_GROUP =  "im:cache:group";
    /**
     * 缓存群聊成员id
     */
    String IM_CACHE_GROUP_MEMBER_ID = "im:cache:group_member_ids";
    /**
     * 群消息读取位置
     */
    String IM_GROUP_READ_POSITION = "im:read:group:position";
}

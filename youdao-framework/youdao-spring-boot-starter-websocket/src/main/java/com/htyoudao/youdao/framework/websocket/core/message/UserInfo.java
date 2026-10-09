package com.htyoudao.youdao.framework.websocket.core.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户终端类型
     */
    private Integer terminal;


}

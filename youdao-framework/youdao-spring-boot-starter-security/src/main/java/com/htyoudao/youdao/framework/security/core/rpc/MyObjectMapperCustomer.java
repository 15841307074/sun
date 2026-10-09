package com.htyoudao.youdao.framework.security.core.rpc;

import com.htyoudao.youdao.framework.security.core.LoginUser;
import org.apache.dubbo.spring.security.jackson.ObjectMapperCodec;
import org.apache.dubbo.spring.security.jackson.ObjectMapperCodecCustomer;

/**
 * details
 *
 * @author liuzhaowang
 */
public class MyObjectMapperCustomer implements ObjectMapperCodecCustomer {
    @Override
    public void customize(ObjectMapperCodec objectMapperCodec) {
        objectMapperCodec.configureMapper(objectMapper -> objectMapper.addMixIn(LoginUser.class, LoginUserMixin.class));
    }
}

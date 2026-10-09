package com.htyoudao.youdao.framework.common.util;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HttpUtil {

    public static int getPort(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //没有request 默认为 8080
        if(ObjectUtil.isEmpty(requestAttributes)){
            return 8080;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        int serverPort = request.getServerPort();
        return serverPort;
    }

    public static String getUrl(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String localAddr = request.getLocalAddr();
        int serverPort = request.getServerPort();
        return "http://"+localAddr +":"+ serverPort;
    }
}

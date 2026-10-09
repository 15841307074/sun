package com.htyoudao.youdao.framework.web.core.util;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class RequestUtil {
    
    public static String getRequestBody(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            return "";
        }
        
        return stringBuilder.toString();
    }
    
}
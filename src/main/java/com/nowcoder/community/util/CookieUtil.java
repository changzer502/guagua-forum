package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author changzer
 * @date 2023/2/4
 * @apiNote
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request,String key) {
        if (StringUtils.isBlank(key) || request == null){
            throw new IllegalArgumentException("参数为空");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies){
                if (cookie.getName().equals(key)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

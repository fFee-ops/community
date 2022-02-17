package com.sl.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 *
 *
 * /8 19:21
 */
public class CookieUtil {
    /*
    * 从request里面取cookie，key值的cookie
    * */
    public static String getCookieValue(HttpServletRequest request,String name){
        //参数空值判断
        if(request==null || name==null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    //得到cookie的数组
    //先判断cookie数组是否为空
    //遍历cookie，找对应的key

}

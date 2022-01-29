package com.sl.community.controller.interceptor;

import com.sl.community.annotation.LoginRequired;
import com.sl.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/9 11:23
 */
@Component  //拦截器
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的对象是不是方法
        if(handler instanceof HandlerMethod){
            //是，先转型，以便后面获取信息
          HandlerMethod handlerMethod=(HandlerMethod)handler;
            //取方法上的注解
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //判定取得的注解是否为空,判断当前用户是否登录
            if(loginRequired!=null && hostHolder.getUser()==null){
                //重定向到登录页面，路径+login
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }

        }

        return true;
    }

}

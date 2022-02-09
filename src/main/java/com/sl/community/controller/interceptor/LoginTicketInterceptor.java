package com.sl.community.controller.interceptor;

import com.sl.community.entity.LoginTicket;
import com.sl.community.entity.User;
import com.sl.community.service.UserService;
import com.sl.community.util.CookieUtil;
import com.sl.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 *
 *
 * @date 2021/12/8 19:18
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {//拦截器要继承接口

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //cookie通过request取过来
        String ticket = CookieUtil.getCookieValue(request, "ticket");
        //判断凭证是否为空
        if(ticket!=null){
            //凭证不为空，去查凭证
            LoginTicket loginTicket = userService.findTicket(ticket);
            //先判断凭证是否为空、有效，1是无效，是否过期
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //凭证都符合，根据凭证查用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中将查到的用户存起来，方便后续使用，多线程并发访问没问题，线程的隔离，封装
                /*在请求中线程一直在，服务器做出响应后，请求线程消失*/
                hostHolder.setUser(user);

                //构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication=new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

            }
        }

        return true;
    }

    /*
    * 用user，模版引擎要用，在模版引擎之前将user存在model里面
    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //从线程中得到user
        User user = hostHolder.getUser();
        //空值判断
        if(user!=null && modelAndView!=null){
            //将用户存到model里
            modelAndView.addObject("loginUser",user);
        }

    }

    /*模版执行完之后，将user,security清除*/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        //SecurityContextHolder.clearContext();//请求结束，将保存权限的清除掉

    }
}

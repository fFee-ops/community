package com.sl.community.config;

import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略静态资源的拦截
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //配置授权（哪些路径必须登录才能访问，拥有什么身份能访问）
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow",
                        "/block",
                        "/unblock"
                )
                .hasAnyAuthority(
                    //任意一个权限都可访问
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()//除了这些请求之外的其他请求都可以直接访问
                .and().csrf().disable();//禁用csrf
        //权限不够时
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登陆时怎么处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        //考虑请求的方式（普通请求返回页面，异步请求返回json字符串）
                        //看请求消息头的某一个值
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            //response获得字符流向前台输出内容
                            PrintWriter writer=response.getWriter();
                            //没有权限返回403状态码
                            writer.write(CommunityUtil.getJSIONString(403,"你还没有登陆！"));
                        }else {
                            response.sendRedirect(request.getContextPath()+"/login");
                        }

                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //权限不足时怎么处理（匿名实现接口）
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        //返回json和html分开处理
                        //看请求消息头的某一个值
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            //response获得字符流向前台输出内容
                            PrintWriter writer=response.getWriter();
                            //没有权限返回403状态码
                            writer.write(CommunityUtil.getJSIONString(403,"你没有权限！"));
                        }else {
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });

        //自动拦截/logout路径（security底层时通过filter拦截，在controller之前，这样我们自己写的退出就失效了）
        //覆盖默认逻辑，执行自己的推出代码
        http.logout().logoutUrl("/securitylogout");//将security默认拦截的路径改为这个


    }
}

package com.sl.community.controller.advice;

import com.sl.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/13 9:33
 */
@ControllerAdvice(annotations = Controller.class)  //只有带有Controller注解的会被异常处理
public class ExceptionAdvice {

    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //记录日志
        logger.error("服务器异常:"+e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            //每一个对象记录了一条错误信息
            logger.error(element.toString());
        }

        //给浏览器一个响应，浏览器访问服务器可能希望返回页面，或者是异步请求希望返回json
        //需要判断请求是普通请求还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        //返回值等于xml，表示为异步请求（异步希望返回xml，普通希望返回http）
        if("XMLHttpRequest".equals(xRequestedWith)){
            //设置格式为字符串自己转为json
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJSIONString(1,"服务器异常！"));

        }else{
            //普通请求，重定向到错误页面
            response.sendRedirect(request.getContextPath()+"/error");
        }

    }
}

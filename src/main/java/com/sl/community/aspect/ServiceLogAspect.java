package com.sl.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *
 * @date 2021/12/13 15:23
 */
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger= LoggerFactory.getLogger(ServiceLogAspect.class);

    //声明切点
    @Pointcut("execution(* com.sl.community.service.*.*(..))")
    public void pointcut(){
    }

    //前置通知
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户（id）XXX在某时刻访问了XX方法
        //获得request，在获取ip
        ServletRequestAttributes attributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        //加了消费者之后，直接是生产者调用service，不是controller调用，那么对象为空
        if(attributes==null){
            return; //空就直接返回，不去调用了，不记录日志了
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //获得访问的方法，类名+方法名
        String target=joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }

}

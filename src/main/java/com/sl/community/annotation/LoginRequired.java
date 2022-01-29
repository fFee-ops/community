package com.sl.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/9 11:18
 */
@Target(ElementType.METHOD)//在方法上有效
@Retention(RetentionPolicy.RUNTIME) //在程序运行时有效
public @interface LoginRequired {

}

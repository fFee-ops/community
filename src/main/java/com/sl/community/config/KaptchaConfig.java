package com.sl.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/8 11:42
 */
@Configuration
public class KaptchaConfig {
    //实例化工具的接口，交给spring管理
    @Bean
    public Producer KaptchaProducer(){
        //配置类配置属性
        Properties properties=new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ZXCVBNMASDFGHJKLQWERTYUIOP");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        //干扰，防破解，
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");


        //接口的实现类
        DefaultKaptcha kaptcha=new DefaultKaptcha();
        Config config=new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }



}

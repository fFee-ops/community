package com.sl.community.controller;

import com.google.code.kaptcha.Producer;
import com.sl.community.entity.User;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @date 2021/12/7 17:02
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger=LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /*表头上的注册按钮，跳转到注册页面*/
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }



    /*注册功能，表单接收，post请求*/
    //页面传入的值和user属性匹配，直接注入
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        //业务层实现注册返回结果
        Map<String, Object> map = userService.register(user);
        //map为空表示注册成功
        if(map==null || map.isEmpty()){
            //注册成功后跳转到中间页面去，而不是登录页面，因为还要激活
            //设置提示信息，并跳转到中间页面
            model.addAttribute("msg","您的账号已经注册成功，我们已发送激活邮件，请及时激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //注册失败，回到注册页面，携带提示信息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }

    }

    //激活
    //https://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String Toactivation(Model model,
                             @PathVariable("userId")int userId,
                             @PathVariable("code")String code){
        //调用业务层去激活
        int res = userService.activation(userId, code);
        //查看返回结果与接口中的常量对比
        if(res==ACTIVATION_SUCCESS){
            //激活成功到中间页面，再到登录页面
            model.addAttribute("msg","您的账号已经成功激活！");
            model.addAttribute("target","/login");
        }else if(res==ACTIVATION_REPEAT){
            //重复激活，去首页
            model.addAttribute("msg","不可重复激活！");
            model.addAttribute("target","/index");
        }else{
            //激活失败也去首页
            model.addAttribute("msg","激活失败！");
            model.addAttribute("target","/index");
        }

        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/* HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        /*//服务端要记住验证码，存在session里
        session.setAttribute("kaptcha",text);*/
        /*
        * 重构，将验证码存到redis里面
        * */
        String kaptchaOwner = CommunityUtil.generateUUID();//生成随机字符串
        //凭证需要发给客户端（浏览器），在cookie中保存，并设置过期时间和生效路径
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);

        //添加到response里面
        response.addCookie(cookie);
        //将验证码存到redis里面
        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //图片直接输出给浏览器（人工输出）
        //先声明返回的是什么格式
        response.setContentType("image/png");
        //获得输出流
        try {
            //使用工具输出，不用去手动关闭输出流，spring会管理
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            // 错误时输出日志
            logger.error("响应验证码失败："+e.getMessage());
        }


    }

    /*登录功能：之前的验证码在session中，登陆成功需要客户端cookie保存ticket，所以有response*/
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(Model model,String username,String password,
                        String code,boolean remember,@CookieValue("kaptchaOwner")String kaptchaOwner,
                        /*HttpSession session*/HttpServletResponse response){
      //先判断验证码是否正确
        //先取出验证码，取出的验证码、传入的验证码为空，验证码是否相等，不区分大小写
        //从redis里面去验证码需要key，key需要随机凭证，所以加上注解从cookie中取出凭证@CookieValue
        String kaptcha=null;
        //判断key是否有效（不为空没有过期）
        if(!StringUtils.isBlank(kaptchaOwner)){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isBlank(code)|| StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";//错误回到登录页面，提示信息
        }

        // 验证账号密码
        /*选择超时时间：勾选记住我，存储的时间长，不勾选时间短，定义常量时间*/
        int time=remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调用业务层登录功能
        Map<String, Object> map = userService.login(username, password, time);

        //当map里面有ticket时，说明登陆成功，重定向到首页。
        if(map.containsKey("ticket")){
            // 客户端cookie存ticket
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            //设置cookie的生效路径，（在配置文件中写了，注入进来使用）
            cookie.setPath(contextPath);
            // 设置cookie的有效时间
            cookie.setMaxAge(time);
            //将cookie发送给浏览器
            response.addCookie(cookie);

            return "redirect:/index";
        }else {
            // 错误返回登录页面，将错误的消息带给登录页面，向model里面存信息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            return "/site/login";
        }

    }

    /*退出*/
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        //清理认证后保存的权限
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    /**
     * 跳转到忘记密码页面
     */

    @RequestMapping("/forget")
    public String forget(){
        return "site/forget";
    }

}

package com.sl.community;

import com.sl.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 *
 *
 * /7 15:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;//模版引擎

    @Test
    public void testMail(){
        mailClient.sendMail("2636389523@qq.com","Test1","hello");

    }

    @Test
    public void testHtmlMail(){
        //生成动态网页
        Context context=new Context();
        context.setVariable("username","haohao");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        //发送邮件
        mailClient.sendMail("2636389523@qq.com","Test2",content);
    }
}

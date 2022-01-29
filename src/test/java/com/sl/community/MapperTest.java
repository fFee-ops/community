package com.sl.community;

import com.sl.community.entity.DiscussPost;
import com.sl.community.entity.LoginTicket;
import com.sl.community.entity.Message;
import com.sl.community.entity.User;
import com.sl.community.dao.DiscussPostMapper;
import com.sl.community.dao.LoginTicketMapper;
import com.sl.community.dao.MessageMapper;
import com.sl.community.dao.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/6 17:49
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;


    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(11);
        System.out.println(user);

        user=userMapper.selectByName("nowcoder11");
        System.out.println(user);

        user=userMapper.selectByEmail("nowcoder11@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsert(){
        User user=new User();
        user.setUsername("hhh");
        user.setPassword("111222");
        user.setCreateTime(new Date());
        user.setHeaderUrl("http://images.nowcoder.com/head/10.png");

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate(){
        int rows = userMapper.updateHeader(151, "http://images.nowcoder.com/head/100.png");
        System.out.println(rows);

        rows= userMapper.updatePassword(151, "000");
        System.out.println(rows);

        rows=userMapper.updateStatus(151,1);
        System.out.println(rows);
    }

    @Test
    public void testDiscussport(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10,0);
        for (DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }

        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setStatus(0);
        loginTicket.setTicket("qwe");
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);
    }

    @Test
    public void testUpdateLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("qwe");
        System.out.println(loginTicket);

        int i = loginTicketMapper.updateStatus("qwe", 1);
        System.out.println(i);
    }

    @Test
    public void testMessage(){
        List<Message> messages = messageMapper.selectConversation(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);

        List<Message> list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        int q = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(q);


    }
}

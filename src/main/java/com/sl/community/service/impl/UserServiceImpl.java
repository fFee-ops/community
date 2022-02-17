package com.sl.community.service.impl;

import com.sl.community.entity.LoginTicket;
import com.sl.community.entity.User;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.dao.UserMapper;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.MailClient;
import com.sl.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 *
 *
 * /6 20:44
 */
@Service
public class UserServiceImpl implements CommunityConstant, UserService {

    @Autowired
    private UserMapper userMapper;

    /*注册的时候需要发送邮件，注入邮件客户端和模版引擎*/
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    /*  @Autowired
      private LoginTicketMapper loginTicketMapper;
  */
    @Autowired
    private RedisTemplate redisTemplate;

    /*激活码需要域名和项目名，注入进来*/
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        //先从redis里面取，如果没有再初始化
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


    /*注册功能，返回的内容封装起来*/
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //判断user里面的属性：账号和密码是否为空,isBlank()判断null，“”，“ ”三种情况
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        /*验证账号，邮箱是否已经存在*/
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已存在");
            return map;
        }

        /*注册用户*/
        //生成随机字符串对密码加密,要5位
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //随机字符+真密码=注册数据库里的密码
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //普通用户，状态为未激活
        user.setType(0);
        user.setStatus(0);
        //设置激活码,随机生成的字符
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置随机头像0-1000  https://images.nowcoder.com/head/1t.png
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /*
         * 发送激活邮件 html邮件  模版 带连接，
         * */

        //创建对象携带变量，带上email，
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //设置url域名+项目名+激活功能+id+激活码
        //https://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //利用模版引擎生成邮件的内容
        String content = templateEngine.process("/mail/activation", context);

        //邮件客户端发送邮件
        mailClient.sendMail(user.getEmail(), "激活帐号", content);

        return map;


    }

    /*激活方法：激活链接里面包含用户id和激活码，将其作为参数。
     * */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);

        //先判断状态是否为1，及重复激活
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }


        //判断激活码是否相等，激活码和传入的一样
        if (code.equals(user.getActivationCode())) {
            //修改用户的状态
            userMapper.updateStatus(user.getId(), 1);
            //更新用户信息后，删除user的缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            //激活码不相等就激活失败
            return ACTIVATION_FAILED;
        }

    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值判断
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //输入值合法性判断
        /*先根据帐号去查询，无就返回错误，再判断账号是否被激活，再判断密码是否正确，*/
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        //验证通过，生成登录凭证，设置凭证信息，存入到数据库中
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0); //0有效
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        //将凭证存入到redis里面
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);


        //将生成的登录的凭证，存到map中
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    /*退出登录，修改状态1 表示无效*/
    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);//将状态改为1，表示删除
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findTicket(String ticket) {
        //LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        //从redis里面取出来
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeaderUrl(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        //更新用户信息，删除user的缓存
        clearCache(userId);
        return rows;

    }

    /*修改密码*/
    public void updatePassword(int userId, String password) {
        userMapper.updatePassword(userId, password);
    }

    /*根据用户名查找用户*/
    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    //1.优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        return user;

    }

    //2.取不到时初始化缓存数据
    private User initCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(userKey, user);
        return user;
    }

    //3.数据变更时清除数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    //获得用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = userMapper.selectById(userId);
        //角色集合
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;

    }

}

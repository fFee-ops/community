package com.sl.community.service;

import com.sl.community.entity.LoginTicket;
import com.sl.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yazai
 * Date: 18:21 2022/1/24
 * Description:
 */
public interface UserService {
    User findUserById(int id);
    Map<String,Object> register(User user);
    int activation(int userId,String code);
    Map<String,Object> login(String username,String password,int expiredSeconds);
    void logout(String ticket);
    LoginTicket findTicket(String ticket);
    int updateHeaderUrl(int userId,String headerUrl);
    void updatePassword(int userId,String password);
    User findUserByName(String name);
    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}

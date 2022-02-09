package com.sl.community.util;

import com.sl.community.entity.User;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @date 2021/12/8 19:34
 */

/*持有用户的信息，多线程隔离，用于代替session对象*/
@Component
public class HostHolder {

    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    //清理
    public void clear(){
        users.remove();
    }
}

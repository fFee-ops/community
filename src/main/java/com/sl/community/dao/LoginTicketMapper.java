package com.sl.community.dao;

import com.sl.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 *
 *
 * /8 14:59
 */
@Mapper
@Deprecated  //废弃了，不推荐使用
public interface LoginTicketMapper {

    /***声明主键自动生成，生成的值注入给属性***/
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired )",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select * from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(String ticket,int status);

}

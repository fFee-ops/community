package com.sl.community.dao;

import com.sl.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 *
 *
 * /6 17:02
 */
@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id,int status);

    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);
}

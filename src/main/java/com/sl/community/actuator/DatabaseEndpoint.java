package com.sl.community.actuator;

import com.sl.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Endpoint(id = "database")//交给Spring管理，@Endpoint给端点一个id名，以后便于访问。
public class DatabaseEndpoint {
    private static final Logger logger= LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation//@ReadOperation：通过get请求来访问
    public String checkConnection(){
        try(
                Connection connection = dataSource.getConnection()
        ){
            return CommunityUtil.getJSIONString(0,"获取链接成功");
        }catch (Exception e){
            logger.error("连接数据库失败："+e.getMessage());
            return CommunityUtil.getJSIONString(1,"获取连接失败");
        }
    }

}

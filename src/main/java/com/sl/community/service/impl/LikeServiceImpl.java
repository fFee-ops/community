package com.sl.community.service.impl;

import com.sl.community.service.LikeService;
import com.sl.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 *
 *
 * /13 20:40
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞(因为里面涉及两个事务数据操作，所以需要redis的编程式事务)
    //userId是点赞人的id ， entityUserId是作者（被点赞人）的id
    public void like(int userId,int entityType,int entityId,int entityUserId){
     /*   //拼接出key
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        //判断当前用户有没有点过赞（userid在不在set集合里）
        boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        //已经点过赞了，取消赞（将userid在set集合中remove）
        if(isMember){
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else {//没点过，添加数据
            redisTemplate.opsForSet().add(entityLikeKey,userId);

        }*/
       //重构
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //拼接出key
                String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey=RedisKeyUtil.getUserLikeKey(entityUserId);

                //将查询操作放在事务之外（redis在事务之间的命令不会立马被执行而是放在队列里，在事务提交之后再统一执行）
                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                operations.multi();

                //执行两次修改的操作
                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }



                return operations.exec(); //执行事务
            }
        });
    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        //得到key
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        //在set集合中查看key对应有几个value
        return redisTemplate.opsForSet().size(entityLikeKey);

    }

    //查询某人对某实体的点赞状态,返回整数更具有扩展性，以后还可以开发踩帖子的功能
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        //拼接出key
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        //判断value集合中是否有该userid
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1:0;
    }

    //查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);
        Integer count=(Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null ? 0 : count.intValue();//做一下判断，因为对象可能为null
    }

}

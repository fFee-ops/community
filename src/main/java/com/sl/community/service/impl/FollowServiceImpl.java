package com.sl.community.service.impl;

import com.sl.community.entity.User;
import com.sl.community.service.FollowService;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/14 16:24
 */
@Service
public class FollowServiceImpl implements CommunityConstant, FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注, 需要存关注的目标和粉丝，有两次数据库访问，需要事务管理
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //构造要关注目标的key、粉丝的key
                String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey=RedisKeyUtil.getFollowerKey(entityType,entityId);

                //开启事务
                operations.multi();

                //添加数据
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());//我关注了某个实体id，和我关注的时间
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());//我粉丝的id和他关注我的时间


                return operations.exec();//提交事务
            }
        });
    }

    //取消关注
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //构造要关注目标的key、粉丝的key
                String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey=RedisKeyUtil.getFollowerKey(entityType,entityId);

                //开启事务
                operations.multi();

                //删除数据
                operations.opsForZSet().remove(followeeKey,entityId);//删除key中对应的v
                operations.opsForZSet().remove(followerKey,userId);


                return operations.exec();//提交事务
            }
        });
    }

    //查询关注的实体的数量（谁的某个类别的关注数量，将关注的帖子和用户分开）
    public long findFolloweeCount(int userId,int entityType){
        //构造key
        String followeeCount=RedisKeyUtil.getFolloweeKey(userId,entityType);
        //统计数量
        return redisTemplate.opsForZSet().zCard(followeeCount);
    }

    //查询实体的粉丝数量
    public long findFollowerCount(int entityType,int entityId){
        String followerCount=RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerCount);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        //获得key
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,entityType);
        //查询一下该key的分数，如果是空，那就是没关注，不为空就是关注了
       return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    //查询某个用户关注的人(目标，偶像),
    // 返回的是user对象和关注的时间，多个因素用map封装一下。 分页显示，实体类型确认为用户
    public List<Map<String,Object>>  findFollowees(int userId, int offset,int limit){
        //构建目标key
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        //得到目标（偶像）列表id
        Set<Integer> targetId = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if(targetId==null){
            //判断空值
            return null;
        }
        List<Map<String,Object>>  list=new ArrayList<>();
        //遍历set集合找到对应的用户
        for (Integer id : targetId) {
            Map<String,Object> map=new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followTime",new Date(score.longValue()));
            //将map集合添加到list集合中
            list.add(map);

        }

        return list;
    }

    //查询某用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        //构建key
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //遍历redis
        Set<Integer> fansId = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        //判断空值
        if(fansId==null){
            return null;
        }

        List<Map<String,Object>> list=new ArrayList<>();
        for (Integer id : fansId) {
            Map<String,Object> map=new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }
}

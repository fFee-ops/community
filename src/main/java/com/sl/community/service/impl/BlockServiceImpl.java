package com.sl.community.service.impl;

import com.sl.community.service.BlockService;
import com.sl.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * Created by yazai
 * Date: 13:33 2022/2/8
 * Description:
 */
@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void block(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //构造要拉黑目标的key
                String blockingKey = RedisKeyUtil.getBlockingKey(userId, entityType);
                //黑粉key
                String blockedKey = RedisKeyUtil.getBlockedKey(entityType, entityId);

                //开启事务
                operations.multi();

                //添加数据
                operations.opsForZSet().add(blockingKey, entityId, System.currentTimeMillis());//我拉黑了某个实体id，和我拉黑的时间
                operations.opsForZSet().add(blockedKey, userId, System.currentTimeMillis());//我黑粉的id和他拉黑我的时间


                return operations.exec();//提交事务
            }
        });
    }

    @Override
    public void unblock(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //构造要拉黑目标的key
                String blockingKey = RedisKeyUtil.getBlockingKey(userId, entityType);
                //黑粉key
                String blockedKey = RedisKeyUtil.getBlockedKey(entityType, entityId);

                //开启事务
                operations.multi();

                //删除数据
                operations.opsForZSet().remove(blockingKey, entityId);//删除key中对应的v
                operations.opsForZSet().remove(blockedKey, userId);

                return operations.exec();//提交事务
            }
        });
    }

    @Override
    /**
     * 查询当前用户(userId)是否拉黑了该实体用户(entityId)
     */
    public boolean hasBlocked(int userId, int entityType, int entityId) {
        //获得key
        String blockingKey = RedisKeyUtil.getBlockingKey(userId, entityType);
        //查询一下该key的分数，如果是空，那就是没拉黑，不为空就是拉黑了
        return redisTemplate.opsForZSet().score(blockingKey, entityId) != null;
    }
}

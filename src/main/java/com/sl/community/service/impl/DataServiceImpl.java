package com.sl.community.service.impl;

import com.sl.community.util.RedisKeyUtil;
import com.sl.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    //格式化日期
    private SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");

    //统计数据（1、记录数据 2、能够查询到）记、查

    /*
    * 统计UV
    * */
    //1、将指定IP计入UV
    public void recordUV(String ip){
        //先得到key
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);//存进redis
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        //先判断日期是否为空
        if(start==null || end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //将该范围内每一天的key合并整理到一个集合里
        List<String> keyList=new ArrayList<>();
        //利用calendar对日期做运算
        Calendar calendar=Calendar.getInstance();//实例化抽象类对象
        calendar.setTime(start);

        //对时间参数进行合法判断
        if(start.after(end)){
            throw new IllegalArgumentException("请输入正确的时间段！");
        }

        //时间<=end才循环
        while (!calendar.getTime().after(end)){
            //得到key
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            //将key加到集合里
            keyList.add(key);
            //calendar加一天
            calendar.add(Calendar.DATE,1);
        }

        //合并这些数据，存放合并后的值
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));//得到合并后的key
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());//合并存到redis

        //返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);


    }

    /*
    * 统计DAU
    * */

    //统计单日的dau
    public void recordDAU(int userId){
        //得到key
        String rediskey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        //存入redis
        redisTemplate.opsForValue().setBit(rediskey,userId,true);
    }

    //统计某个区间的dau(在该区间内某一天登录了就算是活跃，所以要用or运算)
    public long  calculateDAU(Date start,Date end){
        //先判断日期是否为空
        if(start==null || end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //将该范围内每一天的key合并整理到一个集合里
        //bitmap运算需要数组，所以list集合里面存byte数组
        List<byte[]> keyList=new ArrayList<>();
        //利用calendar对日期做运算
        Calendar calendar=Calendar.getInstance();//实例化抽象类对象
        calendar.setTime(start);

        //时间<=end才循环
        while (!calendar.getTime().after(end)){
            //得到key
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            //将key加到集合里
            keyList.add(key.getBytes());
            //calendar加一天
            calendar.add(Calendar.DATE,1);
        }


        //将合并的or运算结果存入redis
         return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //得到合并的key
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }

}

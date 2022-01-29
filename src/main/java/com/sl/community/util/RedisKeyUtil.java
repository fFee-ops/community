package com.sl.community.util;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/13 20:30
 */
public class RedisKeyUtil {
    //拼接key的常量
    private static final String SPLIT=":";

    //定义（帖子和评论统称为）实体的前缀
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    //某个用户收到的赞的前缀
     private static final String PREFIX_USER_LIKE="like:user";
     //关注目标（偶像）前缀
    private static final String PREFIX_FOLLOWEE="followee";
    //粉丝前缀
    private static final String PREFIX_FOLLOWER="follower";
    //验证码key的前缀
    private static final String PREFIX_KAPTCHA="kaptcha";
    //凭证key的前缀
    private static final String PREFIX_TICKET="ticket";
    //用户信息key前缀
    private static final String PREFIX_USER="user";
    //统计uv的key前缀
    private static final String PREFIX_UV="uv";
    //统计dau的key前缀
    private static final String PREFIX_DAU="dau";
    //帖子分数key的前缀
    private static final String PREFIX_POST="post";


    //某个实体的赞（静态方法，传入一些参数拼接，生成完整的key）
    //like:entity:entityType:entityId  -> set(userId) value是一个set集合，存的是点赞用户的id
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户收到的赞的key
    //like:user:userId  -> int
    public static String getUserLikeKey(int userId){
        return  PREFIX_USER_LIKE + SPLIT + userId;
    }

    //产生某个用户关注的实体 (目标人、帖子)的key
    //key是粉丝id和关注类型       存的是我关注的实体的id和关注时间
    //followee:userId(谁关注的):entityType   ->  zset(entityId,now)  value是以时间为指标的有序集合
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT +entityType;
    }

    //某个实体（帖子，题目）拥有粉丝的key
    //key是关注的类型和关注的id       存的是关注我的粉丝的id和关注时间
    //follower:entityType:entityId  -> zset(userId(粉丝的id),now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //拼接验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT +owner;
    }

    //生成凭证的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //生成user的key
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    //统计单日的uv
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT +date;
    }

    //统计某个区间的uv
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV +SPLIT +startDate +SPLIT +endDate;
    }

    //统计单日的dau
    public static String getDAUKey(String date){
        return PREFIX_DAU +SPLIT+date;
    }

    //统计某个区间的dau
    public static String getDAUKey(String startKey,String endKey){
        return PREFIX_DAU + SPLIT +startKey +SPLIT +endKey;
    }

    //帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT +"score";
    }
}

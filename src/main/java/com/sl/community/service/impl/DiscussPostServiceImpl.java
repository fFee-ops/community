package com.sl.community.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sl.community.dao.DiscussPostMapper;
import com.sl.community.entity.DiscussPost;
import com.sl.community.service.DiscussPostService;
import com.sl.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/6 20:40
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    private static final Logger logger= LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;


    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        //只缓存热门帖子，只缓存首页，首页用户没登陆，userId为0，缓存一页数据，key就有offset和limit有关。
        if(userId==0 && orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        //缓存的是帖子列表，当用户查询自己的帖子时传入userId，这个时候是不走缓存的。当userId为0，才走缓存。
        if(userId==0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //初始化热门帖子、帖子总数缓存
    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key==null || key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        //解析数据
                        String[] params = key.split(":");
                        //判断解析数据（切割得到的是不是两个）
                        if(params==null || params.length!=2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        //有了参数，查数据（缓存）
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }

                });

        //初始化帖子总数缓存
        postRowsCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /*发布帖子*/
    public int addDiscussPort(DiscussPost discussPost){
        //空值判断
        if(discussPost==null){
            throw new IllegalArgumentException("内容不能为空！");
        }
        // 对帖子的标题和内容，转移HTML标识
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //对标题和内容过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        //插入帖子
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /*查看帖子详情*/
    public DiscussPost findDiscussDetail(int id){
        return discussPostMapper.selectDiscussById(id);
    }

    /*跟新帖子的评论数量*/
    public int updateDiscussCount(int discussId,int commentCount){
        return discussPostMapper.updateCommentCount(discussId,commentCount);
    }

    public int updateDiscussType(int id,int type){
        return discussPostMapper.updateDiscussType(id, type);
    }

    public int updateDiscussStatus(int id,int status){
        return discussPostMapper.updateDiscussStatus(id, status);
    }

    public int updateDiscussScore(int postId,double score){
        return discussPostMapper.updateDiscussScore(postId,score);
    }

}

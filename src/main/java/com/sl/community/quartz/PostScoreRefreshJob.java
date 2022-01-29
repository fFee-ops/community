package com.sl.community.quartz;


import com.sl.community.entity.DiscussPost;
import com.sl.community.util.CommunityConstant;
import com.sl.community.service.impl.DiscussPostServiceImpl;
import com.sl.community.service.ElasticsearchService;
import com.sl.community.service.LikeService;
import com.sl.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;
    static {
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化失败！",e);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //从redis里面取值（先得到key），每一个key都要算一下，反复的操作，所以用BoundSetOperation
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations=redisTemplate.boundSetOps(redisKey);
        //先判断一下缓存中有没有数据，没有变化就不做操作
        if(operations.size()==0){
            logger.info("[任务取消] 没有要刷新的帖子！");
            return;
        }
        //使用日志记录时间中间过程
        logger.info("[任务开始] 正在刷新帖子分数： "+operations.size());
        while (operations.size()>0){//只要redis里面有数据就算
            //集合中弹出一个值
            this.refresh((Integer)operations.pop());
        }

        logger.info("[任务结束]：帖子分数刷新结束！");
    }

    private void refresh(int postId) {
        //先将贴子查出来
        DiscussPost post = discussPostService.findDiscussDetail(postId);
        //空值判断（帖子被人点赞，但是后来被管理删除）
        if(post==null){
            logger.error("帖子不存在: id= "+ postId);//日志记录错误提示
            return;
        }


        //计算帖子分值（加精-1、评论数、点赞数）
        boolean wonderful = post.getStatus() == 1;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        //先求权重
        double w=(wonderful? 75 : 0) + commentCount*10 + likeCount * 2;
        //分数=帖子权重+距离天数
        //为了不得到负数，在权重和1之间取最大值。将时间得到的毫秒在换算为天
        double score=Math.log10(Math.max(w,1)+
                (post.getCreateTime().getTime()-epoch.getTime())/(1000 * 3600 * 24));

        //更新帖子的分数
         discussPostService.updateDiscussScore(postId, score);
        //同步搜索对应帖子的数据（先重设帖子的分数，再保存到es）
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}

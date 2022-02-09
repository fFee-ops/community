package com.sl.community.controller;

import com.sl.community.entity.Comment;
import com.sl.community.entity.DiscussPost;
import com.sl.community.entity.Event;
import com.sl.community.event.EventProducer;
import com.sl.community.service.CommentService;
import com.sl.community.service.impl.DiscussPostServiceImpl;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.HostHolder;
import com.sl.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 *
 *
 * @date 2021/12/12 10:01
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    //添加评论
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //封装触发评论事件，链接评论帖子/评论的详情
        Event event=new Event()
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId)
                .setTopic(TOPIC_COMMENT);
        /*
        * 判断评论的是实体是帖子还是评论，将其存到实体的作者里面
           不管是什么根据实体id找到目标
           * */
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussDetail(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target = commentService.findComment(comment.getEntityId());
            System.out.println(target.getUserId()+target.getTargetId());
            event.setEntityUserId(comment.getTargetId());
        }

        eventProducer.fireEvent(event);

        //评论之后会修改帖子的评论数量，相当于修改了帖子，触发修改es里面帖子事件
        //判断一下是评论的帖子还是评论的回复，只有前者才会触发事件
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //发帖之后，触发发帖事件，异步将新发布的帖子添加到es里
            event=new Event();
            event.setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            //评论帖子后面计算分数，并将被评论的帖子存到redis里面
            String redisKey = RedisKeyUtil.getPostScoreKey();  //得到key
            redisTemplate.opsForSet().add(redisKey,discussPostId);//将贴子存到set集合中

        }

        return "redirect:/discuss/detail/"+discussPostId;
    }
}

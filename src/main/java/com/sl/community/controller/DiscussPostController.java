package com.sl.community.controller;

import com.sl.community.entity.*;
import com.sl.community.event.EventProducer;
import com.sl.community.service.CommentService;
import com.sl.community.service.LikeService;
import com.sl.community.service.UserService;
import com.sl.community.service.impl.DiscussPostServiceImpl;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.HostHolder;
import com.sl.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String insertDiscuss(String title, String content) {
        /* 页面只需要传入标题和内容即可
         * 先获取用户，看是否登录，返回信息提示（异步请求，json）*/
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSIONString(403, "您还没有登录，无法发布！");
        }

        //生成帖子对象，并赋值
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        // 非转发帖子
        discussPost.setIsForward(0);
        // 非转发帖子 默认用-1填充字段
        discussPost.setOriginalUserId(-1);
        // 帖子可见性默认是公开的，也就是0
        discussPost.setVisible(0);
        discussPostService.addDiscussPort(discussPost);

        //发帖之后，触发发帖事件，异步将新发布的帖子添加到es里
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        //在新添加的帖子后面也计算分数，并将其存到redis里面
        String redisKey = RedisKeyUtil.getPostScoreKey();  //得到key
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());//将贴子存到set集合中


        //异步json返回成功信息提示
        return CommunityUtil.getJSIONString(0, "发布成功！");

    }

    /*查看帖子详情数据*/
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String selectDiscussDetail(Model model, @PathVariable("discussPostId") int id, Page page) {
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussDetail(id);
        model.addAttribute("post", discussPost);

        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        //查询点赞有关的信息
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        //点赞状态(判断一下用户是否登录，没有登陆赞数为0)
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息
        page.setRows(discussPost.getCommentCount());
        page.setPath("/discuss/detail/" + id);
        page.setLimit(5);

        /*
         * 评论：给帖子的评论
         * 回复：给评论的评论
         * */
        //得到当前帖子的所有评论，评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST,
                discussPost.getId(), page.getoffset(), page.getLimit());
        //遍历集合，存到map集合中，封装要展示的数据，得到评论的user对象
        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                //VO添加评论
                commentVo.put("comment", comment);
                //VO添加作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //查询评论点赞有关的信息
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态(判断一下用户是否登录，没有登陆赞数为0)
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //得到评论的评论（回复）,多个是集合,不分页了
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment replay : replyList) {
                        //回复的Vo
                        Map<String, Object> replayVo = new HashMap<>();
                        //添加回复
                        replayVo.put("reply", replay);
                        //添加作者
                        replayVo.put("user", userService.findUserById(replay.getUserId()));


                        //回复的时候可以针对某个回复再回复，需要考虑target，回复目标
                        User target = replay.getTargetId() == 0 ? null : userService.findUserById(replay.getTargetId());
                        replayVo.put("target", target);

                        //查询回复点赞有关的信息
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, replay.getId());
                        replayVo.put("likeCount", likeCount);
                        //点赞状态(判断一下用户是否登录，没有登陆赞数为0)
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, replay.getId());
                        replayVo.put("likeStatus", likeStatus);

                        //将map集合撞到list集合里
                        replyVoList.add(replayVo);

                    }
                }
                //将回复list集合添加到评论列表里
                commentVo.put("replys", replyVoList);
                //将回复数量添加到评论列表里
                int replayCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replayCount);

                //将每一个评论列表添加到评论显示集合中
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";

    }

    //置顶,异步请求，局部刷新，所以需要加上@ResponseBody,因为需要传参数，所以用post
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateDiscussType(id, 1);

        //将帖子同步到es，触发一下发帖事件，之后再返回一个提示信息
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSIONString(0);
    }

    //加精,异步请求，局部刷新，所以需要加上@ResponseBody,因为需要传参数，所以用post
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateDiscussStatus(id, 1);//0普通，1加精，2删除

        //将帖子同步到es，触发一下发帖事件，之后再返回一个提示信息
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //加精后面也计算分数，并将其存到redis里面
        String redisKey = RedisKeyUtil.getPostScoreKey();  //得到key
        redisTemplate.opsForSet().add(redisKey, id);//将贴子存到set集合中

        return CommunityUtil.getJSIONString(0);
    }

    //删除,异步请求，局部刷新，所以需要加上@ResponseBody,因为需要传参数，所以用post
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateDiscussStatus(id, 2);//0普通，1加精，2删除


        //将贴子再es中删除，触发删帖事件
        Event event = new Event();
        event.setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSIONString(0);
    }

    @RequestMapping(path = "/forward", method = RequestMethod.POST)
    @ResponseBody
    public String forwardDiscuss(String title, String content, int originalUserId) {
        /* 页面只需要传入标题和内容即可
         * 先获取用户，看是否登录，返回信息提示（异步请求，json）*/
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSIONString(403, "您还没有登录，无法转发帖子！");
        }

        //生成帖子对象，并赋值
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        // 是转载的帖子
        discussPost.setIsForward(1);
        // 原作者userId
        discussPost.setOriginalUserId(originalUserId);

        discussPostService.addDiscussPort(discussPost);

        //发帖之后，触发发帖事件，异步将新发布的帖子添加到es里
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        //在新添加的帖子后面也计算分数，并将其存到redis里面
        String redisKey = RedisKeyUtil.getPostScoreKey();  //得到key
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());//将贴子存到set集合中


        //异步json返回成功信息提示
        return CommunityUtil.getJSIONString(0, "转发成功！");

    }

    @RequestMapping(path = "/visible", method = RequestMethod.POST)
    @ResponseBody
    public String visibleDiscuss(int postId, int visible) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSIONString(403, "您还没有登录，无法进行操作！");
        }
        /*
        取反：前端传过来visible=0，证明现在帖子是可见的，用户想让帖子变为不可见，即让visible=1；
        反之同理；
         */
        if (visible == 0) {
            visible = 1;
        } else {
            visible = 0;
        }
        discussPostService.visibleDiscuss(postId, visible);

        return CommunityUtil.getJSIONString(0, "修改帖子状态成功！");
    }

}

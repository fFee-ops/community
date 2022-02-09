package com.sl.community.controller;

import com.sl.community.entity.Event;
import com.sl.community.entity.Page;
import com.sl.community.entity.User;
import com.sl.community.event.EventProducer;
import com.sl.community.service.BlockService;
import com.sl.community.service.FollowService;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.HostHolder;
import com.sl.community.annotation.LoginRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @date 2021/12/14 16:38
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    //异步请求,提交数据post,将要关注的实体传进来
    //关注
    @LoginRequired
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        if(user==null){
            throw new RuntimeException("登录之后才能关注");
        }

        followService.follow(user.getId(),entityType,entityId);

        //关注事件的触发,链接到关注人的页面
        Event event=new Event();
        event.setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())//事件触发者
                .setEntityType(entityType)
                .setEntityUserId(entityId);//只能关注人，作者一定是实体的id

        eventProducer.fireEvent(event);
        //给页面返回信息
        return CommunityUtil.getJSIONString(0,"已关注！");

    }

    //取消关注
    @LoginRequired
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        if(user==null){
            throw new RuntimeException("登录之后才能关注");
        }

        followService.unfollow(user.getId(),entityType,entityId);

        //给页面返回信息
        return CommunityUtil.getJSIONString(0,"已取消关注！");

    }

    //查看用户A关注的人,分页查询(Page),往页面上传数据
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model  model){
        //获得用户
        User user = userService.findUserById(userId);
        //判断用户A是否存在
        if (user==null){
            throw new RuntimeException("用户不存在！");
        }
        //为了页面显示用户信息，将用户存起来
        model.addAttribute("user",user);
        //设置分页信息
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        //查询关注的人
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getoffset(),page.getLimit());

        //判断当前用户对某用户A关注列表里用户B的关注状态
        if(userList!=null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                 map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }

    //查询关注用户A的粉丝
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model  model){
        //获得用户
        User user = userService.findUserById(userId);
        //判断用户A是否存在
        if (user==null){
            throw new RuntimeException("用户不存在！");
        }
        //为了页面显示用户信息，将用户存起来
        model.addAttribute("user",user);
        //设置分页信息
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        //查询粉丝
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getoffset(),page.getLimit());

        //判断当前用户对某用户A粉丝列表里用户B的关注状态
        if(userList!=null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
                boolean hasBlocked = blockService.hasBlocked(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
                map.put("hasBlocked",hasBlocked);
                System.out.println("用户："+u.getUsername()+"【被拉黑状态】："+hasBlocked);
            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }



    //判断当前用户对某用户是否已关注
    private boolean hasFollowed(int userId){
        if (hostHolder.getUser()==null){
            return false;//未登录一定没有关注
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}

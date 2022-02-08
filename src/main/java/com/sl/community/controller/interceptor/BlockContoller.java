package com.sl.community.controller.interceptor;

import com.sl.community.annotation.LoginRequired;
import com.sl.community.entity.Event;
import com.sl.community.entity.User;
import com.sl.community.event.EventProducer;
import com.sl.community.service.BlockService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yazai
 * Date: 13:25 2022/2/8
 * Description:
 */
@Controller
public class BlockContoller implements CommunityConstant {
    @Autowired
    private BlockService blockService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    // 拉黑
    @LoginRequired
    @RequestMapping(path = "/block", method = RequestMethod.POST)
    @ResponseBody
    public String block(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("登录之后才能拉黑");
        }

        blockService.block(user.getId(), entityType, entityId);

        //关注事件的触发,链接到关注人的页面
        Event event = new Event();
        event.setTopic(TOPIC_BLOCK)
                .setUserId(user.getId())//事件触发者
                .setEntityType(entityType)
                .setEntityUserId(entityId);//只能拉黑人，作者一定是实体的id

        eventProducer.fireEvent(event);
        //给页面返回信息
        return CommunityUtil.getJSIONString(0, "拉黑成功！");
    }


    // 取消拉黑
    @LoginRequired
    @RequestMapping(path = "/unblock", method = RequestMethod.POST)
    @ResponseBody
    public String unblock(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        blockService.unblock(user.getId(), entityType, entityId);

        //给页面返回信息
        return CommunityUtil.getJSIONString(0, "已取消拉黑！");

    }

    //判断当前用户对某用户是否已拉黑
    private boolean hasBlocked(int userId){
        if (hostHolder.getUser()==null){
            return false;//未登录默认未拉黑
        }
        return blockService.hasBlocked(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}

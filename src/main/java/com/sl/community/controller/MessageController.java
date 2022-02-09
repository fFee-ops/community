package com.sl.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.sl.community.entity.Message;
import com.sl.community.entity.Page;
import com.sl.community.entity.User;
import com.sl.community.service.MessageService;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 *
 *
 * @date 2021/12/12 15:33
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        //获得当前用户
        User user = hostHolder.getUser();

        //设置分页的信息，限制，路径，总条数
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));


        //得到会话列表集合
        List<Message> messageList = messageService.findConversation(user.getId(),
                page.getoffset(), page.getLimit());

        //将多个数据都存在map里面
        List<Map<String,Object>> conversatinList =new ArrayList<>();

        //遍历会话集合构建map集合
        if(messageList!=null){
            for (Message message : messageList) {
                Map<String,Object> map=new HashMap<>();
                //map里面存message，每个会话包含几条消息，未读消息数量
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findUnreadCount(user.getId(),message.getConversationId()));
                //得到目标对象并将其存到map集合中：头像显示的会话中另外一个用户的，不是当前用户的
                int target= user.getId()==message.getFromId() ? message.getToId() : message.getFromId();
                //将map结合存到List集合中
                map.put("target",userService.findUserById(target));

                conversatinList.add(map);
            }
        }


        //将list集合存到model中
        model.addAttribute("conversations",conversatinList);
        //查询用户整个的未读消息数量
        int letterUnreadCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    //查看会话的详情
    @RequestMapping(path = "/letter/detail/{conservationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conservationId")String conservationId,
                                  Model model,Page page){
        //对分页信息设置限制，路径，总行数
        page.setLimit(5);
        page.setPath("/letter/detail/"+conservationId);
        page.setRows(messageService.findLetterCount(conservationId));
        //对回话进行分页查询，得到私信列表集合
        List<Message> letters = messageService.findLetters(conservationId, page.getoffset(), page.getLimit());

        //将多个数据都存在map里面
        //map里面存私信，from用户（显示的是发件人from用户的信息）
        List<Map<String,Object>> letterList=new ArrayList<>();
        if(letters!=null){
            for (Message letter : letters) {
                Map<String,Object> map=new HashMap<>();
                map.put("letter",letter);
                map.put("fromUser",userService.findUserById(letter.getFromId()));
                //将map结合存到List集合中
                letterList.add(map);
            }


        }

        //将list集合存到model中
        model.addAttribute("letters",letterList);

        //将目标用户存在model中.目标人（来自谁的），调用方法
        model.addAttribute("target",getLetterTarget(conservationId));

        //调用方法将未读消息变成已读
        List<Integer> ids = getLetterIds(letters);
        //判断集合是否为空
        if(!ids.isEmpty()){
            // 不为空，更改为已读
            messageService.readMessage(ids);
        }


        return "/site/letter-detail";
    }

    /*从会话集合中得到未读的消息id的集合*/
    public List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids=new ArrayList<>();

        if(letterList!=null){
            for (Message message : letterList) {
                //判读当前的用户是不是接收者（将未读可以变成已读状态） ,身份符合且消息状态为未读
                if(hostHolder.getUser().getId()==message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }





    public User getLetterTarget(String conservationId){
        //拆解conservationId ，得到目标用户
        String[] s = conservationId.split("_");
        int id0=Integer.parseInt(s[0]);
        int id1=Integer.parseInt(s[1]);

        if(hostHolder.getUser().getId()==id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }


    }

    //发送私信，提交信息post，是异步的，返回json
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        //根据接受者的用户名查询用户得到id
        User target = userService.findUserByName(toName);
        //判断用户是否存在
        if(target==null){
            return CommunityUtil.getJSIONString(1,"消息接收方用户不存在！");//用户不存在返回错误json信息
        }

        //构造私信信息（发送发id，接收方id，会话id，私信内容，创建时间）
        Message message=new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        //会话id小的在前，大的在后拼接
        if(hostHolder.getUser().getId() < target.getId()){
            message.setConversationId(hostHolder.getUser().getId()+"_"+target.getId());
        }else {
            message.setConversationId(target.getId()+"_"+hostHolder.getUser().getId());
        }
        message.setCreateTime(new Date());

        //加入私信
        messageService.addLetter(message);

        //给页面返回状态
        return CommunityUtil.getJSIONString(0);

    }

    //删除私信
    @RequestMapping(path = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJSIONString(0);

    }

    //系统通知列表
    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        //获得当前用户
        User user = hostHolder.getUser();

        //得到系统通知--评论
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if(message!=null){
            Map<String,Object> messageVO=new HashMap<>();
            messageVO.put("message",message);
            //将json字符串转为对象（先对里面的转义字符及进行转义）
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //json转为对象的时候，指明要转成的对象类型
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将存在data对象里面的内容抽取出来存到map里面
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            //评论数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);
            //未读评论数量
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unreadCount);
            model.addAttribute("commentNotice",messageVO);
        }


        //得到系统通知--点赞
        Message like = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if(like!=null){
            Map<String,Object> likeVO=new HashMap<>();
            likeVO.put("like",like);
            //将json字符串转为对象（先对里面的转义字符及进行转义）
            String content = HtmlUtils.htmlUnescape(like.getContent());
            //json转为对象的时候，指明要转成的对象类型
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将存在data对象里面的内容抽取出来存到map里面
            likeVO.put("user",userService.findUserById((Integer) data.get("userId")));
            likeVO.put("entityType",data.get("entityType"));
            likeVO.put("entityId",data.get("entityId"));
            likeVO.put("postId",data.get("postId"));
            //点赞数量
            int likeCount = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            likeVO.put("count",likeCount);
            //未读点赞数量
            int likeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeVO.put("unread",likeUnreadCount);
            model.addAttribute("likeNotice",likeVO);
        }


        //得到系统通知--关注
        Message follow = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);

        if(follow!=null){
            Map<String,Object> followVO=new HashMap<>();
            followVO.put("follow",follow);
            //将json字符串转为对象（先对里面的转义字符及进行转义）
            String content = HtmlUtils.htmlUnescape(follow.getContent());
            //json转为对象的时候，指明要转成的对象类型
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将存在data对象里面的内容抽取出来存到map里面
            followVO.put("user",userService.findUserById((Integer) data.get("userId")));
            followVO.put("entityType",data.get("entityType"));
            followVO.put("entityId",data.get("entityId"));
            //关注数量
            int followCount = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            followVO.put("count",followCount);
            //未读关注数量
            int followUnreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followVO.put("unread",followUnreadCount);
            model.addAttribute("followNotice",followVO);
        }


        //获得未读消息数量（私信和系统通知）
        int letterUnreadCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";

    }

    //系统通知详情
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic,Model model,Page page){
        //获得当前用户
        User user = hostHolder.getUser();

        //对分页信息设置限制，路径，总行数
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));
        //对主题进行分页查询，得到列表集合
        List<Message> noticesList = messageService.findNotices(user.getId(), topic, page.getoffset(), page.getLimit());

        //将多个数据都存在map里面
        List<Map<String,Object>> noticeListVO=new ArrayList<>();
        //判断是否非空
        if(noticesList!=null){
            for (Message message : noticesList) {
                Map<String,Object> map=new HashMap<>();
                //通知
                map.put("notice",message);
                //内容：将content的json字符串先转义再转为对象
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.findUserById(message.getFromId()));

                noticeListVO.add(map);
            }
        }
        model.addAttribute("notices",noticeListVO);

        //调用方法将未读消息变成已读
        List<Integer> ids = getLetterIds(noticesList);
        //判断集合是否为空
        if(!ids.isEmpty()){
            // 不为空，更改为已读
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}

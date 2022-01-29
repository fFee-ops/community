package com.sl.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sl.community.entity.Message;
import com.sl.community.entity.DiscussPost;
import com.sl.community.entity.Event;
import com.sl.community.service.impl.DiscussPostServiceImpl;
import com.sl.community.service.ElasticsearchService;
import com.sl.community.service.MessageService;
import com.sl.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component   //消费者
public class EventConsumer implements CommunityConstant {
    //记日志
    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    //往消息表里插数据
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    //一个方法，包含三个主题(定义常量引用主题)
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        //发了一个空消息
        if(record==null || record.value()==null){
            logger.error("发送消息为空！");
            return;
        }
        //将json消息转为对象，指定字符串对应的具体类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        //转为对象之后再判断
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }
        //内容和格式都对之后，发送站内通知
        Message message=new Message();
        //设置消息的发送者，from_id系统用户规定为1（定义为常量）
        message.setFromId(SYSTEM_USER_ID);
        //设置消息的接收者（帖子的作者）
        message.setToId(event.getEntityUserId());
        //存主题
        message.setConversationId(event.getTopic());
        //当前时间
        message.setCreateTime(new Date());
        //内容是json字符串，先存map，再转为json字符串存进入
        Map<String,Object> content=new HashMap<>();
        //事件触发者
        content.put("userId",event.getUserId());
        //实体的类型（帖子、点赞、关注）
        content.put("entityType",event.getEntityType());
        //实体的id
        content.put("entityId",event.getEntityId());
        //不方便村的字段存到content
        //判断事件对象有没有值
        if(!event.getData().isEmpty()){
            //遍历事件对象的map，将其存到content里
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }

        }
        //将content转为json字符串
        message.setContent(JSONObject.toJSONString(content));
        //存消息
        messageService.addLetter(message);
    }

    //消费发帖事件(发完帖子之后插入到es里面)
    @KafkaListener(topics = TOPIC_PUBLISH)
    public void handlePublicMessage(ConsumerRecord record){
        //发了一个空消息
        if(record==null || record.value()==null){
            logger.error("发送消息为空！");
            return;
        }
        //将json消息转为对象，指定字符串对应的具体类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        //转为对象之后再判断
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }
        //得到的消息非空，格式也没有问题，处理事件
        //从事件消息里得到帖子id，查询帖子，将其存到es里
        DiscussPost post = discussPostService.findDiscussDetail(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }


    //消费删帖事件(删除帖子之后，也删除es里面的帖子)
    @KafkaListener(topics = TOPIC_DELETE)
    public void handleDeleteMessage(ConsumerRecord record){
        //发了一个空消息
        if(record==null || record.value()==null){
            logger.error("发送消息为空！");
            return;
        }
        //将json消息转为对象，指定字符串对应的具体类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        //转为对象之后再判断
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        //发了一个空消息
        if(record==null || record.value()==null){
            logger.error("发送消息为空！");
            return;
        }
        //将json消息转为对象，指定字符串对应的具体类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        //转为对象之后再判断
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }

        //得到htmlUrl、文件名字、后缀
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String  suffix = (String) event.getData().get("suffix");
        //拼命令
        String cmd= wkImageCommand + " --quality  75 "
                +htmlUrl+" "+wkImageStorage +"/" +fileName +suffix;
        //执行命令，成功和失败都要记录日志
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功："+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败："+  e.getMessage());
        }


    }

}

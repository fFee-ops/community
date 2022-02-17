package com.sl.community.service.impl;

import com.sl.community.dao.MessageMapper;
import com.sl.community.entity.Message;
import com.sl.community.service.MessageService;
import com.sl.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 * /12 15:27
 */
@Service
public class MessageServiceImpl  implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversation(int userId, int offset, int limit){
        return messageMapper.selectConversation(userId, offset, limit);

    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);

    }

    //添加一条消息
    public int addLetter(Message message){
        //添加之前进行内容过滤（先过滤标签，在过滤敏感词）
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }


    //修改消息状态(将消息改为已读)
    public int readMessage(List<Integer> ids){
        return messageMapper.updateMessageStatus(ids, 1);
    }


    public int deleteMessage(int id) {
        //Arrays.asList()将数组转为集合，不支持基本数据类型
        return messageMapper.updateMessageStatus(Arrays.asList(new Integer[]{id}), 2);
    }

    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return  messageMapper.selectNotices(userId, topic, offset, limit);
    }
}

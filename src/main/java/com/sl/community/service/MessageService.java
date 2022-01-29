package com.sl.community.service;

import com.sl.community.entity.Message;

import java.util.List;

/**
 * Created by yazai
 * Date: 18:21 2022/1/24
 * Description:
 */
public interface MessageService {
    List<Message> findConversation(int userId, int offset, int limit);
    int findConversationCount(int userId);
    List<Message> findLetters(String conversationId,int offset,int limit);
    int findLetterCount(String conversationId);
    int findUnreadCount(int userId,String conversationId);
    int addLetter(Message message);
    int readMessage(List<Integer> ids);
    int deleteMessage(int id);
    Message findLatestNotice(int userId,String topic);
    int findNoticeCount(int userId,String topic);
    int findNoticeUnreadCount(int userId,String topic);
    List<Message> findNotices(int userId,String topic,int offset,int limit);
}

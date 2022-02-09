package com.sl.community.dao;

import com.sl.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *
 *
 * @date 2021/12/12 11:41
 */
@Mapper
public interface MessageMapper {

    //查询当前用户会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversation(int userId, int offset, int limit);

    //查询总行数
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    //conversationId是动态的，查询某个回话的未读，查询该用户的总共未读
    int selectLetterUnreadCount(int userId,String conversationId);

    //增加一条消息（回私信）
    int insertMessage(Message message);

    //修改消息状态（将未读变成已读）
    int updateMessageStatus(List<Integer> ids,int status);

    //查询某个主题下最新的通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题所包含的通知数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知的数量
    int selectNoticeUnreadCount(int userId,String topic);

    //查找某个主题下的通知集合,分页显示
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}

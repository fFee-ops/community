package com.sl.community.service;

import com.sl.community.entity.Comment;

import java.util.List;

/**
 * Created by yazai
 * Date: 18:16 2022/1/24
 * Description:
 */
public interface CommentService {
     List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit);
     int addComment(Comment comment);
     Comment findComment(int commentId);
     int findCountByEntity(int entityType,int entityId);
     List<Comment> findUserComments(int userId,int offset,int limit);
    int findUserCount(int userId);
}

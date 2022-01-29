package com.sl.community.service.impl;

import com.sl.community.entity.Comment;
import com.sl.community.service.CommentService;
import com.sl.community.util.CommunityConstant;
import com.sl.community.dao.CommentMapper;
import com.sl.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommunityConstant, CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCountByEntity(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    //增加评论,里面涉及两个DML操作，需要事务管理
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        //空值判断
        if(comment==null){
            throw new IllegalArgumentException("评论不能为空！");
        }

        //先对评论中的内容过滤，  再过滤敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //将评论插入数据库
        int rows = commentMapper.insertComment(comment);

        //更新评论的数量，（只有更新帖子的评论数量才更新，回复不更新，所以要先判断）
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateDiscussCount(comment.getEntityId(),count);
        }
        return rows;
    }

    //根据回复id查找回复
    public Comment findComment(int commentId){
        return commentMapper.selectCommentById(commentId);
    }
    //根据用户id分页查询该用户的所有回复集合
    public List<Comment> findUserComments(int userId,int offset,int limit){
        return commentMapper.selectCommentsByUser(userId, offset, limit);
    }
    //根据用户id查找回复总数
    public int findUserCount(int userId){
        return commentMapper.selectCountByUser(userId);
    }
}

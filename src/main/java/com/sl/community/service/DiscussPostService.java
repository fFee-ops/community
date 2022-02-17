package com.sl.community.service;

import com.sl.community.entity.DiscussPost;

import java.util.List;

/**
 * Created by yazai
 * Date: 18:19 2022/1/24
 * Description:
 */
public interface DiscussPostService {
    int addDiscussPort(DiscussPost discussPost);
    DiscussPost findDiscussDetail(int id);
    int findDiscussPostRows(int userId);
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode);
    int updateDiscussCount(int discussId,int commentCount);
    int updateDiscussScore(int postId,double score);
    int updateDiscussStatus(int id,int status);
    int updateDiscussType(int id,int type);
    int visibleDiscuss(int postId,int visible);
}

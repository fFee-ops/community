package com.sl.community.service;

/**
 * Created by yazai
 * Date: 18:21 2022/1/24
 * Description:
 */
public interface LikeService {
    long findEntityLikeCount(int entityType,int entityId);
    int findEntityLikeStatus(int userId,int entityType,int entityId);
    int findUserLikeCount(int userId);
    void like(int userId,int entityType,int entityId,int entityUserId);
}

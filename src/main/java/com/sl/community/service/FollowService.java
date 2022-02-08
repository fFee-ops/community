package com.sl.community.service;

import java.util.List;
import java.util.Map;

/**
 * Created by yazai
 * Date: 18:19 2022/1/24
 * Description:
 */
public interface FollowService {
    long findFolloweeCount(int userId, int entityType);

    List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

    long findFollowerCount(int entityType, int entityId);

    List<Map<String, Object>> findFollowers(int userId, int offset, int limit);

    void follow(int userId, int entityType, int entityId);

    boolean hasFollowed(int userId, int entityType, int entityId);

    void unfollow(int userId, int entityType, int entityId);
}

package com.sl.community.service;

/**
 * Created by yazai
 * Date: 13:27 2022/2/8
 * Description:
 */
public interface BlockService {
    void block(int userId, int entityType, int entityId);

    void unblock(int userId, int entityType, int entityId);

    boolean hasBlocked(int userId, int entityType, int entityId);
}

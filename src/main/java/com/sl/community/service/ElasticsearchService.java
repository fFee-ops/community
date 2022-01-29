package com.sl.community.service;

import com.sl.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

/**
 * Created by yazai
 * Date: 18:19 2022/1/24
 * Description:
 */
public interface ElasticsearchService {
    void deleteDiscussPost(int id);

    void saveDiscussPost(DiscussPost post);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}

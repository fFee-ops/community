package com.sl.community.service;

import java.util.Date;

/**
 * Created by yazai
 * Date: 18:16 2022/1/24
 * Description:
 */
public interface DataService {
    long  calculateDAU(Date start, Date end);
    long calculateUV(Date start,Date end);
    void recordDAU(int userId);
    void recordUV(String ip);
}

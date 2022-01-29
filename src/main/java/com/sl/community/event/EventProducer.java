package com.sl.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sl.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //生产者发送消息（处理事件）
    public void fireEvent(Event event){
        //将事件发布到指定的主题（主题，字符串（事件当中所有的数据）json）
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));

    }
}

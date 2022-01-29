package com.sl.community.entity;

import java.util.HashMap;
import java.util.Map;

//事件对象
public class Event {
    //事件的类型
    private String topic;
    //事件是谁触发的（张三给李四点赞，张三就是事件触发者）
    private int userId;
    //事件发生在那个实体之上（点赞，帖子）
    private int entityType;
    private int entityId;
    //实体的作者（帖子、评论的作者），通知给李四（帖子的作者）
    private int entityUserId;
    //将其他额外的数据放在map里面，具有一定的扩展性
    private Map<String,Object> data=new HashMap<>();

    public String getTopic() {
        return topic;
    }

    //调用set方法时，返回对象，调用其他方法（及连续调用方法）
    //为什么不用带参数的构造器？参数过多，且有的时候只需一个参数，这样做更灵活
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }
    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }


    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    //外界调用时只传一个key和一个v
    public Event setData(String key,Object value) {
        this.data.put(key, value);
        return this;
    }
}

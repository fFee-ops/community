package com.sl.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;
    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("learn","学习");
        kafkaProducer.sendMessage("learn","加油");
        try {
            Thread.sleep(2000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

//生产者,交给spring管理，发送消息需要我们手动去调用方法
@Component
class KafkaProducer{

    @Autowired
    private KafkaTemplate kafkaTemplate;
    //发消息方法，主题和内容

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }

}
//消费者，消费者处理消息是被动的，当队列中有消息就去处理
@Component
class KafkaConsumer{
    //要监听的主题
    @KafkaListener(topics = {"learn"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }

}


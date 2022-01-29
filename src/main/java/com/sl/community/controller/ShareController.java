package com.sl.community.controller;

import com.sl.community.entity.Event;
import com.sl.community.event.EventProducer;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    //分享的请求(异步的返回json,将要分享的功能路径传过来)
    @RequestMapping(path = "/share",method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){
        //随机生成图片的文件名
        String fileName = CommunityUtil.generateUUID();

        //异步生成长图    构建事件（主题：分享，携带参数存到map里，htmlUrl，文件名，后缀，）
        Event event=new Event();
        event.setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");
        //触发事件（处理异步事件别忘，消费事件）
        eventProducer.fireEvent(event);

        //给客户端返回访问图片的访问路径
        //将路径存到map里
        Map<String,Object> map=new HashMap<>();
        map.put("shareUrl",domain+contextPath +"/share/image/"+fileName);

        return CommunityUtil.getJSIONString(0,null,map);

    }

    //获得长图
    //返回一个图片用response处理
    @RequestMapping(path = "/share/image/{fileName}",method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //先判断参数空值
        if(StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("文件名不能为空！");
        }
        //声明输出的是什么（图片/格式）
        response.setContentType("image/png");
        //实例化文件，图片存放的路径
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        // 图片是字节，所以获取输出字节流
        try {
            OutputStream os=response.getOutputStream();//输出，就是写入其他文件
            FileInputStream fis = new FileInputStream(file);//进入，就是进去读取
            //一边读取文件，一边向外输出（读取缓冲区，游标）
            byte[] buffer=new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败： "+e.getMessage());
        }

    }


}

package com.sl.community.controller;

import com.sl.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    //打开统计页面的方法,get,post请求都可处理
    @RequestMapping(path = "/data",method ={RequestMethod.GET,RequestMethod.POST} )
    public String getDatePage(){
        return "/site/admin/data";
    }


    //页面上传的是日期的字符串，告诉服务器字符串的格式，他就可以帮你转化，
    // 利用注解@DateTimeFormat
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long calculateUV = dataService.calculateUV(start, end);
        model.addAttribute("uvResult",calculateUV);//将统计结果存到model
        //将表单的日期也存到model里面，跳转后便于页面显示
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);

        return "forward:/data";//转发（这个请求只能完成一部分，下面的部分交给这个请求去完成，即上面那个请求）
    }

    //统计DAU
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult",dau);//将统计结果存到model
        //将表单的日期也存到model里面，跳转后便于页面显示
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);

        return "forward:/data";//转发（这个请求只能完成一部分，下面的部分交给这个请求去完成，即上面那个请求）
    }




}

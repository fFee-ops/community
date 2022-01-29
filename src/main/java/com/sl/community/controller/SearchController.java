package com.sl.community.controller;

import com.sl.community.entity.DiscussPost;
import com.sl.community.entity.Page;
import com.sl.community.service.ElasticsearchService;
import com.sl.community.service.LikeService;
import com.sl.community.service.UserService;
import com.sl.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //搜索表现层(传入关键词，分页自己封装的page)
    //get请求参数不能用请求体来传，用路径后加？，用路径中的某一级来传
    //search?keyword=xxx
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索帖子集合,es里面的当前页是从0开始，我们封装的page是从1开始，减1
        org.springframework.data.domain.Page<DiscussPost> discussPosts =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        //聚合需要的数据
        List<Map<String,Object>> searchList=new ArrayList<>();

        //判断帖子搜索结果
        if(discussPosts!=null){
            //遍历搜索结果
            for (DiscussPost discussPost : discussPosts) {
                Map<String,Object> map=new HashMap<>();
                //将贴子、帖子作者、点赞数量存入map
                map.put("post",discussPost);
                map.put("user",userService.findUserById(discussPost.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));
                searchList.add(map);
            }

        }
        //将最终数据传给页面
        model.addAttribute("discussPosts",searchList);
        //关键词也传给模板页面
        model.addAttribute("keyword",keyword);

        //设置分页信息，路径、数据条数
        page.setPath("/search?keyword="+keyword);
        page.setRows(discussPosts==null?0:discussPosts.getTotalPages());

        return "/site/search";
    }
}

package com.sl.community.controller;

import com.sl.community.entity.Comment;
import com.sl.community.entity.DiscussPost;
import com.sl.community.entity.Page;
import com.sl.community.entity.User;
import com.sl.community.service.*;
import com.sl.community.service.impl.DiscussPostServiceImpl;
import com.sl.community.util.CommunityConstant;
import com.sl.community.util.CommunityUtil;
import com.sl.community.util.HostHolder;
import com.sl.community.annotation.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @date 2021/12/8 20:37
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger=LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${tencent.bucket}")
    private String bucket;

    @Value("${tencent.APPID}")
    private String APPID;

    @Value("${tencent.apCity}")
    private String apCity;

    String key = "community";


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private CommentService commentService;

    /*在登录后才能访问，加上注解拦截*/
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /*@LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        *//*需要model，将数据从模版上直接获取*//*
        //空值判断
        if(headerImage==null){
            //文件不存在，返回到设置页
            model.addAttribute("error","您没有选择图片！");
            return "/site/setting";
        }

        //文件存在，生成随机字符串命名，文件的后缀需要从文件名获取

        //从最后一个。开始截取
        String fileName = headerImage.getOriginalFilename();
        // 指定要上传的文件
        File localFile = new File(fileName);
        // 指定文件将要存放的存储桶
        String bucketName = bucket+"-"+APPID;;
        // 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
       // String key = "community";
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

        model.addAttribute("fileName",fileName);

        return "/site/setting";

    }

    //更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String filName){
        String url="https://"+bucket+".cos."+apCity+".myqcloud.com/"+key+"/"+filName;
        userService.updateHeaderUrl(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSIONString(0);
    }
*/

    //废弃
    /*在登录后才能访问，加上注解拦截*/
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //需要model，将数据从模版上直接获取
        //空值判断
        if(headerImage==null){
            //文件不存在，返回到设置页
            model.addAttribute("error","您没有选择图片！");
            return "/site/setting";
        }

        //文件存在，生成随机字符串命名，文件的后缀需要从文件名获取

        //从最后一个。开始截取
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //获得后缀名后先判断一下后缀是否合理（不为空）
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确！");
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID()+"."+suffix;

        //确定文件存放的路径
        File dest=new File(uploadPath+"/"+fileName);

        //存储文件，将当前内容写入到文件里面
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            //抛出异常，打断下面程序的执行
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！",e);
        }

        //更新当前用户的头像路径，web访问路径
        //http://localhost:8080/commnity/user/header/xxx.png
        //获取当前用户
        User user = hostHolder.getUser();
        //设置头像路径
        String headerUrl=domain+contextPath+"/user/header/"+fileName;


        //更新头像
        userService.updateHeaderUrl(user.getId(),headerUrl);

       // 重定向到首页
        return "redirect:/index";

    }
    //废弃
    /*获取头像:传入文件*/
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        /*
        响应图片
        输入流我们自己创建的，需要我们自己去关闭，输出流是spring托管，
        写在try的小括号里，会自己添加到finally里面，关闭

        * */
        //声明输出的格式
        response.setContentType("image/"+suffix);
        //返回二进制字节流，获得输出流
        try(
                OutputStream os = response.getOutputStream();
                FileInputStream fis=new FileInputStream(fileName);
                ) {
            //读文件，利用缓冲区读取，游标去读取
            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败："+e.getMessage());
        }




    }


    /*
    修改密码
    服务器检查原密码是否正确，
    若正确则将密码修改为新密码，并重定向到退出功能，强制用户重新登录。
    若错误则返回到账号设置页面，给与相应提示。
    * */

    /*在登录后才能访问，加上注解拦截*/
    @LoginRequired
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPwd,String newPwd,String quePwd){
        //参数空值判断
        if(StringUtils.isBlank(oldPwd) || StringUtils.isBlank(newPwd) || StringUtils.isBlank(quePwd)){
            model.addAttribute("passwordMsg","密码不能为空！");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        //服务器检查原密码是否正确，
        String s = CommunityUtil.md5(oldPwd+ user.getSalt());
        if(!s.equals(user.getPassword())){
            model.addAttribute("oldPwdMsg","原密码错误！");
            return "/site/setting";
        }
        //判断两次密码输入的是否一样
        if(!newPwd.equals(quePwd)){
            model.addAttribute("passwordMsg","两次密码输入不一致！");
            return "/site/setting";
        }
        newPwd=CommunityUtil.md5(newPwd+user.getSalt());
        userService.updatePassword(user.getId(),newPwd);

        return "redirect:/logout";

    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        //得到user
        User user = userService.findUserById(userId);
        //判断用户是否存在
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }
        //将用户传给页面
        model.addAttribute("user",user);
        //获得该用户被点赞的数量
        int likeCount = likeService.findUserLikeCount(userId);
        //将数据发给页面
        model.addAttribute("likeCount",likeCount);

        //查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        //当前登录用户对某用户是否已关注
        //先判断当前用户是否登录
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        //当前登录用户对某用户是否已拉黑
        //先判断当前用户是否登录
        boolean hasBlocked=false;
        if(hostHolder.getUser()!=null){
            hasBlocked = blockService.hasBlocked(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasBlocked",hasBlocked);
        return "/site/profile";
    }



    //查找某用户发布的帖子,分页显示Page，传递给前端model
    @RequestMapping(path = "/selectDiscuss/{userId}",method = RequestMethod.GET)
    public String findDiscussPost(@PathVariable("userId")int userId, Page page, Model model){
        //找用户
        User user = userService.findUserById(userId);
        //判断用户是否存在
        if(user==null){
            throw new RuntimeException("用户不存在！");
        }
        //将用户添加到model
        model.addAttribute("user",user);
        //设置分页信息
        page.setLimit(3);
        page.setPath("/user/selectDiscuss/"+userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        //查找该用户发布过的帖子
        List<DiscussPost> discussPostsList = discussPostService.findDiscussPosts(userId, page.getoffset(), page.getLimit(),0);
        //判断集合是否为空
        if (discussPostsList==null){
            return null;
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for (DiscussPost discussPost : discussPostsList) {
            Map<String,Object> map=new HashMap<>();
            map.put("discussPost",discussPost);
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
            map.put("likeCount",likeCount);

            list.add(map);

        }
        model.addAttribute("discussPostsList",list);



        //将集合返回页面
        return "/site/my-post";

    }

    //查找某用户曾经为帖子发布过的评论
    @RequestMapping(path = "/selectReply/{userId}",method = RequestMethod.GET)
    public String findComment(@PathVariable("userId")int userId,
                              Page page,Model model){
        //查找user
        User user = userService.findUserById(userId);
        //判断用户是否存在
        if(user==null){
            throw new RuntimeException("用户不存在！");
        }
        //将用户添加到model
        model.addAttribute("user",user);
        //设置分页信息
        page.setLimit(3);
        page.setPath("/user/selectComment/"+userId);
        page.setRows(commentService.findUserCount(userId));

        //查找某用户曾经为帖子发布过的评论
        List<Comment> comments = commentService.findUserComments(userId, page.getoffset(), page.getLimit());
        // 判断集合是否为空
        if(comments==null){
            return null;
        }

        List<Map<String,Object>> list=new ArrayList<>();
        for (Comment comment : comments) {
            Map<String,Object> map=new HashMap<>();
            //将评论保存起来
            map.put("comment",comment);
            //找到回复对应的帖子
            DiscussPost discussPost = discussPostService.findDiscussDetail(comment.getEntityId());
            map.put("discussPost",discussPost);

            list.add(map);
        }

        model.addAttribute("comments",list);

        return "/site/my-reply";
    }

}

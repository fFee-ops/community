package com.sl.community.util;

/**
 *
 *
 * @date 2021/12/8 10:09
 */
public interface CommunityConstant {

        int ACTIVATION_SUCCESS=0;

        int ACTIVATION_FAILED=1;

        int ACTIVATION_REPEAT=2;

        /*默认状态的登录凭证的超时时间 12h*/
        int DEFAULT_EXPIRED_SECONDS=3600*12;

        /*记住我状态下的超时时间 100天*/
        int REMEMBER_EXPIRED_SECONDS=3600*24*100;

        //实体类型：帖子的评论
        int ENTITY_TYPE_POST=1;

        //实体类型：评论的评论（回复）
        int ENTITY_TYPE_COMMENT=2;

        //实体类型：用户
        int ENTITY_TYPE_USER=3;

        //主题：评论
        String TOPIC_COMMENT="comment";

        //主题：点赞
        String TOPIC_LIKE="like";

        //主题：关注
        String TOPIC_FOLLOW="follow";

        //主题：拉黑
        String TOPIC_BLOCK="block";

        //主题：分享
        String TOPIC_SHARE="share";

        //事件主题：发帖
        String TOPIC_PUBLISH="publish";

        //事件主题：删帖
        String TOPIC_DELETE="delete";

        //系统用户id--1
        int SYSTEM_USER_ID=1;

        //权限：普通用户
        String AUTHORITY_USER="user";

        //权限：普通管理员
        String AUTHORITY_ADMIN="admin";

        //权限：版主
        String AUTHORITY_MODERATOR="moderator";

       String  tencent_secretId="AKIDczdLoZmsABvlKehW3YqxovmMfmqJFhkz";
       String  tencent_secretKey="mPdaHhKWRBpDK4Y2UAcvykV4ey2rZ9TW";
       String  tencent_bucket="pic-bed-1303913583";
       String  tencent_apCity="ap-nanjing";
       String tencent_APPID="1303913583";


}

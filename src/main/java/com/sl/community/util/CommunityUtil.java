package com.sl.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 *
 *
 * @date 2021/12/7 17:10
 */
/*工具类，静态方法方便调用*/
public class CommunityUtil {
    //生成随机字符串，便于后续调用
    public static String generateUUID(){
        //字符串由字母和横线组成，去除横线
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //注册时密码加密，MD5单向加密不解密,为了更安全考虑，随机字符串+真正密码=加密后的最终结果
    public static String md5(String key){
        //先判断字符串是否为空（空串，空格）
        if(StringUtils.isEmpty(key)){
            return null;
        }
        //加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /*服务器向浏览器返回的信息封装成json对象，再转为字符串
    * 编码、提示信息、业务数据*/
    public static String getJSIONString(int code, String msg, Map<String,Object> map){
        //创建json对象
        JSONObject json=new JSONObject();
        //封装json
        json.put("code",code);
        json.put("msg",msg);
        //判断map是否为空，遍历map
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();//json转为字符串

    }

    //上述方法的重载，方便调用
    public static String getJSIONString(int code, String msg){
        return getJSIONString(code,msg,null);

    }

    public static String getJSIONString(int code){
        return getJSIONString(code,null,null);

    }
}

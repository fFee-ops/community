package com.sl.community.util;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;

@Component
public class ConnectTencentCloud implements CommunityConstant{

    //@Value("${tencent.secretId}")
    private String secretId=tencent_secretId;

    //@Value("${tencent.secretKey}")
    private String secretKey=tencent_secretKey;

    //@Value("${tencent.bucket}")
    private String bucket=tencent_bucket;

    //@Value("${tencent.apCity}")
    private String apCity=tencent_apCity;

    protected  static COSClient cosClient;

    //构造方法初始化腾讯云客户端
    public ConnectTencentCloud(){
        //1、初始化用户身份信息
        COSCredentials cred=new BasicCOSCredentials(secretId,secretKey);
        //2、设置bucket的区域，
        //Region region=new Region(apCity);
        ClientConfig clientConfig = new ClientConfig(new Region(apCity));
        //clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
    }
}

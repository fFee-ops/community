package com.sl.community.config;

import com.sl.community.quartz.AlphaJob;
import com.sl.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//只有第一次有用，将配置读取到数据库中，以后直接从数据库中读
@Configuration
public class QuartzConfig {

    //配置JobDetail
    //@Bean  //将JobDetailFactoryBean装配到容器里
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphajob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);//任务不在运行，触发器没有了也不用删，留着
        factoryBean.setRequestsRecovery(true);//任务是不是可恢复的
        return factoryBean;
    }

    //配置触发器
   // @Bean  //我这里的参数需要JobDetail，我将上面bean的方法名传进来，这里得到的是JobDetailFactoryBean管理的对象
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);//参数名与bean名一致
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphsTriggerGroup");
        factoryBean.setRepeatInterval(3000);//执行频率
        factoryBean.setJobDataMap(new JobDataMap());//指定那个对象来存状态
        return factoryBean;
    }

    //刷新帖子分数任务
    @Bean  //将JobDetailFactoryBean装配到容器里
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);//任务不在运行，触发器没有了也不用删，留着
        factoryBean.setRequestsRecovery(true);//任务是不是可恢复的
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);//参数名与bean名一致
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setRepeatInterval(1000 * 60 *5);//执行频率
        factoryBean.setJobDataMap(new JobDataMap());//指定那个对象来存状态
        return factoryBean;
    }

}

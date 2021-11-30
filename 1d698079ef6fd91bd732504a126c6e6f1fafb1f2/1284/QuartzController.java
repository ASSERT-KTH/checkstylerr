package com.hlj.quartz.controller;

import com.hlj.quartz.quartz.Job.HelloJoTwo;
import com.hlj.quartz.quartz.service.QuartzService;
import com.hlj.quartz.quartz.Job.HelloJobOne;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  下午4:47.
 */
@RestController
public class QuartzController {


    @Autowired
    private QuartzService quartzService;


    @GetMapping("quartzStart")
    public String startNNoQuartz(){
        quartzService.startJob("0/1 * * * * ? ","job1","gropu1", HelloJobOne.class);
        quartzService.startJob("0/2 * * * * ? ","job2","gropu2", HelloJoTwo.class);

        return "定时器任务开始执行，请注意观察控制台";
    }

    @GetMapping("pauseJob")
    public String pauseJob(){
        quartzService.pauseJob("job1","gropu1");
        return "暂停一个定时器任务，请注意观察控制台";
    }


    @GetMapping("resumeJob") //shutdown关闭了，或者删除了就不能重启了
    public String resumeJob(){
        quartzService.resumeJob("job1","gropu1");
        return "暂停重启一个定时器任务，请注意观察控制台";
    }

    @GetMapping("deleteJob")
    public String deleteJob(){
        quartzService.deleteJob("job1","gropu1");
        return "删除一个定时器任务，请注意观察控制台，删除了，重启就没什么用了";
    }


    @GetMapping("startAllJob")
    public String startAllJob(){
        quartzService.startAllJob();
        return "开启定时器，这时才可以开始所有的任务，默认是开启的";
    }

    @GetMapping("shutdown")
    public String shutdown(){
        quartzService.shutdown();
        return "关闭定时器，则所有任务不能执行和创建";
    }


    /**
     * @Desc   有配置文件quartz.properties
     * @Date   2018/3/22 下午5:40.
     */

    @GetMapping("haveProperties")
    public String haveProperties() throws InterruptedException, SchedulerException {
        quartzService.haveProperties();
        return "使用配置文件-定时器开始执行，请观察控制台";
    }
}

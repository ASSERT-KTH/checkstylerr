package com.hlj.service.impl;
import com.hlj.dao.mybatis.check.QuartzDbProcessCheckMapper;
import com.hlj.data.general.AppException;
import com.hlj.data.res.check.JobDetailData;
import com.hlj.data.res.check.QuartzCheckData;
import com.hlj.service.QuartzCheckService;
import com.hlj.utils.DateHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;

/**
 * 类名称：
 * 类描述：
 * 创建人：liqingxu
 * 修改人：
 * 修改时间：2017/7/21 下午12:28
 * 修改备注：
 *
 * @version 1.0.0
 */
@Service
public class QuartzCheckServiceImpl implements QuartzCheckService {

    private Logger logger = LoggerFactory.getLogger(QuartzCheckServiceImpl.class);

    @Resource
    private QuartzDbProcessCheckMapper quartzDbProcessCheckMapper;


    @Override
    public void checkQuartzJob(Calendar calendar) throws AppException {
        Long current = Calendar.getInstance().getTimeInMillis();
        Long time = calendar.getTimeInMillis();
        List<QuartzCheckData> checkDataList = quartzDbProcessCheckMapper.findJobList(time);
        if(CollectionUtils.isEmpty(checkDataList)){
            logger.info("quartz monitor:没有延迟job,非常好!"+Calendar.getInstance().getTime());
        }else{
            for(QuartzCheckData checkData:checkDataList){
                if(current.compareTo(checkData.getNextFireTime()) > 0){
                    logger.error("jobName:"+checkData.getJobName()+",延迟:"+((current-checkData.getNextFireTime())/1000)+"秒");
                    JobDetailData detailData = quartzDbProcessCheckMapper.findJobDetailData(checkData.getJobName());
                    String desc = "";
                    if(detailData != null){
                        desc = detailData.getDescription();
                    }

                    String text = "["+checkData.getJobName()+":"+desc+"]延迟:"+processTime(current,checkData.getNextFireTime())+"尚未执行,信息发送时间:"+ DateHelper.convertDate2String(Calendar.getInstance().getTime(),DateHelper.YYYY_MM_DD_HH_MM_SS);
                    //发送邮件
                    logger.error(text);
                }
            }
        }


    }

    private String processTime(Long currtime,Long ptime){
        Long minus = (currtime - ptime)/1000;
        if(minus < 60){
            return minus +"秒";
        }else if(minus >=60 && minus < 3600){
            return (minus/60)+"分" + (minus%60)+"秒";
        }else{
            return (minus/3600) + "小时" + ((minus%3600)/60)+"分" + (minus%60)+"秒";
        }
    }

}

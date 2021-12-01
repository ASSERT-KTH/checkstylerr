package com.java110.job.task.report;

import com.java110.dto.community.CommunityDto;
import com.java110.dto.task.TaskDto;
import com.java110.intf.report.IGeneratorFeeMonthStatisticsInnerServiceSMO;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.po.reportFeeMonthStatistics.ReportFeeMonthStatisticsPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName GenerateOwnerBillTemplate
 * @Description TODO  房屋费用账单生成
 * @Author wuxw
 * @Date 2020/6/4 8:33
 * @Version 1.0
 * add by wuxw 2020/6/4
 **/
@Component
public class GenerateFeeMonthStatisticsTemplate extends TaskSystemQuartz {
    private static final Logger logger = LoggerFactory.getLogger(GenerateFeeMonthStatisticsTemplate.class);

    @Autowired
    private IGeneratorFeeMonthStatisticsInnerServiceSMO generatorFeeMonthStatisticsInnerServiceSMOImpl;


    @Override
    protected void process(TaskDto taskDto) throws Exception {

        // 获取小区
        List<CommunityDto> communityDtos = getAllCommunity();

        for (CommunityDto communityDto : communityDtos) {
            try {
                GenerateFeeMonthStatistic(taskDto, communityDto);
            } catch (Exception e) {
                logger.error("生成月报表 失败", e);
            }
        }

    }

    private void GenerateFeeMonthStatistic(TaskDto taskDto, CommunityDto communityDto) {
        ReportFeeMonthStatisticsPo reportFeeMonthStatisticsPo = new ReportFeeMonthStatisticsPo();
        reportFeeMonthStatisticsPo.setCommunityId(communityDto.getCommunityId());
        generatorFeeMonthStatisticsInnerServiceSMOImpl.generatorData(reportFeeMonthStatisticsPo);
    }


}

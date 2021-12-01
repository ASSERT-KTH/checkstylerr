package com.java110.core.smo.inspectionTask;

import com.java110.core.feign.FeignConfiguration;
import com.java110.dto.inspectionTask.InspectionTaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @ClassName IInspectionTaskInnerServiceSMO
 * @Description 活动接口类
 * @Author wuxw
 * @Date 2019/4/24 9:04
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@FeignClient(name = "community-service", configuration = {FeignConfiguration.class})
@RequestMapping("/inspectionTaskApi")
public interface IInspectionTaskInnerServiceSMO {

    /**
     * <p>查询小区楼信息</p>
     *
     * @param inspectionTaskDto 数据对象分享
     * @return InspectionTaskDto 对象数据
     */
    @RequestMapping(value = "/queryInspectionTasks", method = RequestMethod.POST)
    List<InspectionTaskDto> queryInspectionTasks(@RequestBody InspectionTaskDto inspectionTaskDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param inspectionTaskDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryInspectionTasksCount", method = RequestMethod.POST)
    int queryInspectionTasksCount(@RequestBody InspectionTaskDto inspectionTaskDto);
}

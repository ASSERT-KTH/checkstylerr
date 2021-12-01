package com.java110.core.smo.common;

import com.java110.core.feign.FeignConfiguration;
import com.java110.dto.resourceStore.ResourceOrderDto;
import com.java110.entity.audit.AuditUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "common-service", configuration = {FeignConfiguration.class})
@RequestMapping("/commonApi")
public interface IResourceEntryStoreInnerServiceSMO {


    /**
     * <p>启动流程</p>
     *
     *
     * @return CommunityDto 对象数据
     */
    @RequestMapping(value = "/startProcess", method = RequestMethod.POST)
    public ResourceOrderDto startProcess(@RequestBody ResourceOrderDto resourceOrderDto);


    /**
     *  获取用户任务
     * @param user 用户信息
     */
    @RequestMapping(value = "/getUserTasks", method = RequestMethod.POST)
    public List<ResourceOrderDto> getUserTasks(@RequestBody AuditUser user);

    /**
     * 同意
     * @param resourceOrderDto
     * @return
     */
    @RequestMapping(value = "/agreeCompleteTask", method = RequestMethod.POST)
    public boolean agreeCompleteTask(@RequestBody ResourceOrderDto resourceOrderDto);


    /**
     * 反驳
     * @param resourceOrderDto
     * @return
     */
    @RequestMapping(value = "/refuteCompleteTask", method = RequestMethod.POST)
    public boolean refuteCompleteTask(@RequestBody ResourceOrderDto resourceOrderDto);

    /**
     * 完成任务
     * @param resourceOrderDto
     */
    @RequestMapping(value = "/complete", method = RequestMethod.GET)
    public boolean complete(@RequestBody ResourceOrderDto resourceOrderDto);
}

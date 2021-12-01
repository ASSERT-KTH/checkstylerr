package com.java110.common.smo.impl;


import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.core.smo.common.IResourceEntryStoreInnerServiceSMO;
import com.java110.core.smo.store.IPurchaseApplyInnerServiceSMO;
import com.java110.dto.PageDto;
import com.java110.dto.purchaseApply.PurchaseApplyDto;
import com.java110.dto.resourceStore.ResourceOrderDto;
import com.java110.entity.audit.AuditUser;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service("resourceEntryStoreSMOImpl")
@RestController
public class ResourceEntryStoreInnerServiceSMOImpl extends BaseServiceSMO implements IResourceEntryStoreInnerServiceSMO {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IPurchaseApplyInnerServiceSMO purchaseApplyInnerServiceSMOImpl;


    /**
     * 启动流程
     *
     * @return
     */
    public ResourceOrderDto startProcess(@RequestBody ResourceOrderDto resourceOrderDto) {
        //将信息加入map,以便传入流程中
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("resourceOrderDto", resourceOrderDto);
        //开启流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("resourceEntry", variables);
        //将得到的实例流程id值赋给之前设置的变量
        String processInstanceId = processInstance.getId();
        // System.out.println("流程开启成功.......实例流程id:" + processInstanceId);

        resourceOrderDto.setProcessInstanceId(processInstanceId);

        return resourceOrderDto;
    }

    /**
     * 查询用户任务数
     *
     * @param user
     * @return
     */
    public long getUserTaskCount(@RequestBody AuditUser user) {
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey("resourceEntry");
        query.taskAssignee(user.getUserId());
        return query.count();
    }

    /**
     * 获取用户任务
     *
     * @param user 用户信息
     */
    public List<PurchaseApplyDto> getUserTasks(@RequestBody AuditUser user) {
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey("resourceEntry");
        query.taskAssignee(user.getUserId());
        query.orderByTaskCreateTime().desc();
        List<Task> list = null;
        if(user.getPage()  >=1){
            user.setPage(user.getPage()-1);
        }
        if (user.getPage() != PageDto.DEFAULT_PAGE) {
            list = query.listPage(user.getPage(), user.getRow());
        }else{
            list = query.list();
        }

//        List<PurchaseApplyDto> purchaseApplyDtos = new ArrayList<>();
//
//        for (Task task : list) {
//            String id = task.getId();
//            //System.out.println("tasks:" + JSONObject.toJSONString(task));
//            PurchaseApplyDto purchaseApplyDto = (PurchaseApplyDto) taskService.getVariable(id, "purchaseApplyDto");
//            purchaseApplyDto.setTaskId(id);
//            purchaseApplyDto.setProcessInstanceId(task.getProcessInstanceId());
//            purchaseApplyDtos.add(purchaseApplyDto);
//        }
//        return purchaseApplyDtos;

        List<String> applyOrderIds = new ArrayList<>();
        Map<String, String> taskBusinessKeyMap = new HashMap<>();
        for (Task task : list) {
            String processInstanceId = task.getProcessInstanceId();
            //3.使用流程实例，查询
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //4.使用流程实例对象获取BusinessKey
            String business_key = pi.getBusinessKey();
            applyOrderIds.add(business_key);
            taskBusinessKeyMap.put(business_key, task.getId());
        }

        if (applyOrderIds == null || applyOrderIds.size() == 0) {
            return new ArrayList<>();
        }

        //查询 投诉信息
        PurchaseApplyDto purchaseApplyDto = new PurchaseApplyDto();
        purchaseApplyDto.setStoreId(user.getStoreId());
        purchaseApplyDto.setApplyOrderIds(applyOrderIds.toArray(new String[applyOrderIds.size()]));
        List<PurchaseApplyDto> tmpPurchaseApplyDtos = purchaseApplyInnerServiceSMOImpl.queryPurchaseApplyAndDetails(purchaseApplyDto);

        for (PurchaseApplyDto tmpPurchaseApplyDto : tmpPurchaseApplyDtos) {
            tmpPurchaseApplyDto.setTaskId(taskBusinessKeyMap.get(tmpPurchaseApplyDto.getApplyOrderId()));
        }
        return tmpPurchaseApplyDtos;
    }

    public boolean agreeCompleteTask(@RequestBody ResourceOrderDto resourceOrderDto) {
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("auditCode", resourceOrderDto.getAuditCode());
        taskService.complete(resourceOrderDto.getTaskId(), variables);
        return true;
    }

    public boolean refuteCompleteTask(@RequestBody ResourceOrderDto resourceOrderDto) {
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("auditCode", resourceOrderDto.getAuditCode());
        taskService.complete(resourceOrderDto.getTaskId(), variables);
        return true;
    }

    /**
     * 审核 当前任务
     *
     * @param resourceOrderDto 资源订单
     * @return
     */
    public boolean complete(@RequestBody ResourceOrderDto resourceOrderDto) {
        TaskService taskService = processEngine.getTaskService();

        taskService.complete(resourceOrderDto.getTaskId());
        return true;
    }


}

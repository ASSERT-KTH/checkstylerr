package com.java110.common.smo.impl;


import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.dto.PageDto;
import com.java110.dto.auditMessage.AuditMessageDto;
import com.java110.dto.purchaseApply.PurchaseApplyDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.workflow.WorkflowDto;
import com.java110.entity.audit.AuditUser;
import com.java110.intf.common.IAuditUserInnerServiceSMO;
import com.java110.intf.common.IGoodCollectionUserInnerServiceSMO;
import com.java110.intf.common.IWorkflowInnerServiceSMO;
import com.java110.intf.store.IComplaintInnerServiceSMO;
import com.java110.intf.store.IPurchaseApplyInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class GoodCollectionUserInnerServiceSMOImpl extends BaseServiceSMO implements IGoodCollectionUserInnerServiceSMO {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;


    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IComplaintInnerServiceSMO complaintInnerServiceSMOImpl;

    @Autowired
    private IPurchaseApplyInnerServiceSMO purchaseApplyInnerServiceSMOImpl;

    @Autowired
    private IAuditUserInnerServiceSMO auditUserInnerServiceSMOImpl;

    @Autowired
    private IWorkflowInnerServiceSMO workflowInnerServiceSMOImpl;


    /**
     * 启动流程
     *
     * @return
     */
    public PurchaseApplyDto startProcess(@RequestBody PurchaseApplyDto purchaseApplyDto) {
        //将信息加入map,以便传入流程中
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("purchaseApplyDto", purchaseApplyDto);
        variables.put("nextAuditStaffId", purchaseApplyDto.getStaffId());
        variables.put("userId", purchaseApplyDto.getCurrentUserId());
        variables.put("startUserId", purchaseApplyDto.getCurrentUserId());
        //开启流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(getWorkflowDto(purchaseApplyDto.getStoreId()), purchaseApplyDto.getApplyOrderId(), variables);
//        //将得到的实例流程id值赋给之前设置的变量
        String processInstanceId = processInstance.getId();
//        // System.out.println("流程开启成功.......实例流程id:" + processInstanceId);
//
        purchaseApplyDto.setProcessInstanceId(processInstanceId);
        //autoFinishFirstTask(purchaseApplyDto);
        return purchaseApplyDto;
    }


    private String getWorkflowDto(String storeId) {
        //开启流程
        //WorkflowDto.DEFAULT_PROCESS + workflowDto.getFlowId()
        WorkflowDto workflowDto = new WorkflowDto();
        workflowDto.setFlowType(WorkflowDto.FLOW_TYPE_COLLECTION);
        workflowDto.setStoreId(storeId);
        List<WorkflowDto> workflowDtos = workflowInnerServiceSMOImpl.queryWorkflows(workflowDto);

        Assert.listOnlyOne(workflowDtos, "未找到 采购流程或找到多条");

        WorkflowDto tmpWorkflowDto = workflowDtos.get(0);
        if (StringUtil.isEmpty(tmpWorkflowDto.getProcessDefinitionKey())) {
            throw new IllegalArgumentException("流程还未部署");
        }
        return WorkflowDto.DEFAULT_PROCESS + tmpWorkflowDto.getFlowId();
    }

    /**
     * 自动提交第一步
     */
    private void autoFinishFirstTask(PurchaseApplyDto purchaseApplyDto) {
        Task task = null;
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(purchaseApplyDto.getCurrentUserId()).active();
        List<Task> todoList = query.list();//获取申请人的待办任务列表

        for (Task tmp : todoList) {
            if (tmp.getProcessInstanceId().equals(purchaseApplyDto.getProcessInstanceId())) {
                task = tmp;//获取当前流程实例，当前申请人的待办任务
                break;
            }
        }
        Assert.notNull(task, "未找到当前用户任务userId = " + purchaseApplyDto.getCurrentUserId());
        purchaseApplyDto.setTaskId(task.getId());
        purchaseApplyDto.setAuditCode("10000");
        purchaseApplyDto.setAuditMessage("提交");
        completeTask(purchaseApplyDto);

    }

    /**
     * 查询用户任务数
     *
     * @param user
     * @return
     */
    public long getUserTaskCount(@RequestBody AuditUser user) {
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey(getWorkflowDto(user.getStoreId()));
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
        TaskQuery query = taskService.createTaskQuery().processDefinitionKey(getWorkflowDto(user.getStoreId()));
        query.taskAssignee(user.getUserId());
        query.orderByTaskCreateTime().desc();
        List<Task> list = null;
        if (user.getPage() >= 1) {
            user.setPage(user.getPage() - 1);
        }
        if (user.getPage() != PageDto.DEFAULT_PAGE) {
            list = query.listPage(user.getPage(), user.getRow());
        } else {
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


    /**
     * 查询用户任务数
     *
     * @param user
     * @return
     */
    public long getUserHistoryTaskCount(@RequestBody AuditUser user) {
        HistoryService historyService = processEngine.getHistoryService();
//        Query query = historyService.createHistoricTaskInstanceQuery()
//                .processDefinitionKey("complaint")
//                .taskAssignee(user.getUserId());

        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(getWorkflowDto(user.getStoreId()))
                .taskAssignee(user.getUserId());
        if (!StringUtil.isEmpty(user.getAuditLink()) && "START".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaint");
        } else if (!StringUtil.isEmpty(user.getAuditLink()) && "AUDIT".equals(user.getAuditLink())) {
            historicTaskInstanceQuery.taskName("complaitDealUser");
        }

        Query query = historicTaskInstanceQuery;
        return query.count();
    }

    /**
     * 获取用户审批的任务
     *
     * @param user 用户信息
     */
    public List<PurchaseApplyDto> getUserHistoryTasks(@RequestBody AuditUser user) {
        HistoryService historyService = processEngine.getHistoryService();

        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(getWorkflowDto(user.getStoreId()))
                .taskAssignee(user.getUserId());
//        if (!StringUtil.isEmpty(user.getAuditLink()) && "START".equals(user.getAuditLink())) {
//            historicTaskInstanceQuery.taskName("complaint");
//        } else if (!StringUtil.isEmpty(user.getAuditLink()) && "AUDIT".equals(user.getAuditLink())) {
//            historicTaskInstanceQuery.taskName("complaitDealUser");
//        }

        Query query = historicTaskInstanceQuery.orderByHistoricTaskInstanceStartTime().desc();

        List<HistoricTaskInstance> list = null;
        if (user.getPage() != PageDto.DEFAULT_PAGE) {
            list = query.listPage((user.getPage() - 1) * user.getRow(), user.getRow());
        } else {
            list = query.list();
        }

        List<String> applyOrderIds = new ArrayList<>();
        Map<String, String> taskBusinessKeyMap = new HashMap<>();

        for (HistoricTaskInstance task : list) {
            String processInstanceId = task.getProcessInstanceId();
            //3.使用流程实例，查询
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //4.使用流程实例对象获取BusinessKey
            String business_key = pi.getBusinessKey();
            applyOrderIds.add(business_key);
            taskBusinessKeyMap.put(business_key, task.getId());
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


    public boolean completeTask(@RequestBody PurchaseApplyDto purchaseApplyDto) {
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(purchaseApplyDto.getTaskId()).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        Authentication.setAuthenticatedUserId(purchaseApplyDto.getCurrentUserId());
        taskService.addComment(purchaseApplyDto.getTaskId(), processInstanceId, purchaseApplyDto.getAuditMessage());
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("auditCode", purchaseApplyDto.getAuditCode());
        variables.put("currentUserId", purchaseApplyDto.getCurrentUserId());
        variables.put("flag", "1200".equals(purchaseApplyDto.getAuditCode()) ? "false" : "true");
        variables.put("startUserId", purchaseApplyDto.getStartUserId());
        taskService.complete(purchaseApplyDto.getTaskId(), variables);

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (pi == null) {
            return true;
        }
        return false;
    }

    public List<AuditMessageDto> getAuditMessage(@RequestBody PurchaseApplyDto purchaseApplyDto) {

        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(purchaseApplyDto.getTaskId()).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        List<AuditMessageDto> auditMessageDtos = new ArrayList<>();
        if (comments == null || comments.size() < 1) {
            return auditMessageDtos;
        }
        AuditMessageDto messageDto = null;
        for (Comment comment : comments) {
            messageDto = new AuditMessageDto();
            messageDto.setCreateTime(comment.getTime());
            messageDto.setUserId(comment.getUserId());
            messageDto.setMessage(comment.getFullMessage());
        }

        return auditMessageDtos;
    }

    /**
     * 获取任务当前处理人
     *
     * @param purchaseApplyDto
     * @return
     */
    public PurchaseApplyDto getTaskCurrentUser(@RequestBody PurchaseApplyDto purchaseApplyDto) {

        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(purchaseApplyDto.getApplyOrderId()).singleResult();

        if (task == null) {
            purchaseApplyDto.setStaffId("");
            purchaseApplyDto.setStaffName("");
            purchaseApplyDto.setStaffTel("");
            return purchaseApplyDto;
        }

        String userId = task.getAssignee();
        List<UserDto> users = userInnerServiceSMOImpl.getUserInfo(new String[]{userId});

        if (users == null || users.size() == 0) {
            purchaseApplyDto.setStaffId("");
            purchaseApplyDto.setStaffName("");
            purchaseApplyDto.setStaffTel("");
            return purchaseApplyDto;
        }

        purchaseApplyDto.setCurrentUserId(userId);
        purchaseApplyDto.setStaffName(users.get(0).getName());
        purchaseApplyDto.setStaffTel(users.get(0).getTel());
        return purchaseApplyDto;

    }


    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public IComplaintInnerServiceSMO getComplaintInnerServiceSMOImpl() {
        return complaintInnerServiceSMOImpl;
    }

    public void setComplaintInnerServiceSMOImpl(IComplaintInnerServiceSMO complaintInnerServiceSMOImpl) {
        this.complaintInnerServiceSMOImpl = complaintInnerServiceSMOImpl;
    }
}

package com.hlj.proj.service.flow.base.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hlj.proj.data.dao.mybatis.manager.flow.ScfFlowDefinitionManager;
import com.hlj.proj.data.dao.mybatis.manager.flow.ScfFlowNodeManager;
import com.hlj.proj.data.dao.mybatis.manager.flow.ScfFlowRecordManager;
import com.hlj.proj.data.pojo.flow.*;
import com.hlj.proj.exception.BusinessException;
import com.hlj.proj.service.flow.service.enums.FlowNodeTypeEnum;
import com.hlj.proj.service.spring.SpringContextHolder;
import com.hlj.proj.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class ProcessDefinition {


    /**
    /**
     * 通过流程节点名从数据库中初始化一个流程
     */
    public static Process initProcess(String flowCode) {
        //获取流程定义
        ScfFlowDefinitionManager scfFlowDefinitionManager = SpringContextHolder.getBean(ScfFlowDefinitionManager.class);
        ScfFlowDefinitionQuery scfFlowDefinitionQuery = new ScfFlowDefinitionQuery();
        scfFlowDefinitionQuery.setFlowCode(flowCode);
        ScfFlowDefinition definition = scfFlowDefinitionManager.findByQueryContion(scfFlowDefinitionQuery);

        //创建对应的审批进程实例
        String instantsNo = UUID.randomUUID().toString().replace("-","");
        List<FlowNode> nodes = getNodeList(instantsNo , definition.getFlowDefinition());
        Process process = new Process();
        process.setInstantsNo(instantsNo);
        process.setFlowCode(flowCode);
        process.setFlowName(definition.getFlowName());
        process.setNodes(nodes);
        process.setSept(new AtomicInteger(1));
        return process;
    }


    /**
     * 流程暂停的时候，通过通过流程编号来获取正在进行的节点记录进程
     * 1、通过通过流程编号 获取暂停中的FlowRecord流程节点记录
     * 2、组装数据库暂停的流程节点到Process
     * 3、根据流程FlowCode编号，找到该流程所有的FlowNode节点
     */
    public static Process ofSuspendProcess(String instantsNo){

        // 1、通过通过流程编号 获取暂停中的流程节点
        ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
        ScfFlowRecordQuery scfFlowRecordQuery = new ScfFlowRecordQuery();
        scfFlowRecordQuery.setInstantsNo(instantsNo);
        scfFlowRecordQuery.setStatus(Result.StatusEnum.Suspend.getCode());
        ScfFlowRecord oldFlowRecord = scfFlowRecordManager.findByQueryContion(scfFlowRecordQuery);
        if(oldFlowRecord == null){
            throw new BusinessException("找不到对应的流程(" + instantsNo + ")");
        }

        //2、根据流程FlowCode编号，找到该流程所有的FlowNode节点
        String flowCode = oldFlowRecord.getFlowCode();
        ScfFlowDefinitionManager scfFlowDefinitionManager = SpringContextHolder.getBean(ScfFlowDefinitionManager.class);
        ScfFlowDefinitionQuery scfFlowDefinitionQuery = new ScfFlowDefinitionQuery();
        scfFlowDefinitionQuery.setFlowCode(flowCode);
        ScfFlowDefinition scfFlowDefinition = scfFlowDefinitionManager.findByQueryContion(scfFlowDefinitionQuery);
        String flowDefinitionString = scfFlowDefinition.getFlowDefinition();
        List<FlowNode> nodes = getNodeList( instantsNo , flowDefinitionString);

        //3、组装数据库暂停的流程节点到Process
        Process process = new Process();
        process.setInstantsNo(instantsNo);
        process.setFlowCode(flowCode);
        process.setFlowName(oldFlowRecord.getFlowName());
        process.setNodes( nodes);
        process.setSept(new AtomicInteger(oldFlowRecord.getSept()));
        return process;
    }


    private static List<FlowNode> getNodeList(String instantsNo , String flowDefinitionString) {
        List<String> flowDefinitions  = JsonUtils.toObject(flowDefinitionString, new TypeReference<LinkedList<String>>() { });
        ScfFlowNodeManager scfFlowNodeManager = SpringContextHolder.getBean(ScfFlowNodeManager.class);
        List<FlowNode> list = new LinkedList<>();
        for (int i = 0; i < flowDefinitions.size(); i++) {
            String nodeCode = flowDefinitions.get(i);
            ScfFlowNodeQuery scfFlowNodeQuery = new ScfFlowNodeQuery();
            scfFlowNodeQuery.setNodeCode(nodeCode);
            //获取流程节点
            ScfFlowNode scfFlowNode = scfFlowNodeManager.findByQueryContion(scfFlowNodeQuery);
            if(scfFlowNode == null){
                throw new BusinessException("找不到对应的流程节点(" + nodeCode + ")");
            }
            String nodeType = scfFlowNode.getNodeType();
            FlowNode flowNode = null;
            switch(FlowNodeTypeEnum.toEnum(nodeType)){
                case ServiceNode:
                    flowNode = SpringContextHolder.getBean(nodeCode, FlowNode.class);
                    break;
                case AuditNode:
                    AuditorProcess auditorProcess =  AuditorProcess.initAuditProcess(scfFlowNode.getNodeCode(),scfFlowNode.getAuditors(), instantsNo, i);
                    AuditorFlowNode auditorFlowNode = new AuditorFlowNode();
                    auditorFlowNode.setAuditorProcess(auditorProcess);
                    flowNode = auditorFlowNode;
                    break;
                default:
                    throw new BusinessException("节点类型");
            }
            flowNode.setNodeCode(scfFlowNode.getNodeCode());
            flowNode.setNodeName(scfFlowNode.getNodeName());
            flowNode.setNodeType(nodeType);
            list.add(flowNode);
        }
        return list;
    }




}

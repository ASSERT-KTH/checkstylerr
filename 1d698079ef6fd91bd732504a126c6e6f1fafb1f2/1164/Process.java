package com.hlj.proj.service.flow.base.entity;

import com.hlj.proj.data.dao.mybatis.manager.flow.ScfFlowRecordManager;
import com.hlj.proj.data.pojo.flow.ScfFlowRecord;
import com.hlj.proj.data.pojo.flow.ScfFlowRecordQuery;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.exception.BusinessException;
import com.hlj.proj.service.spring.SpringContextHolder;
import com.hlj.proj.utils.EmptyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Data
public class Process<F extends FlowNode> {

    /**
     * 流程实例编号
     */
    private String instantsNo;

    /**
     * 流程编号
     */
    private String flowCode;
    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程节点链条
     */
    private List<F> nodes;

    /**
     * 流程执行步骤号
     */
    private AtomicInteger sept;


    /**
     * 流程开启
     */
    public void startFlow(String data, IdentityInfoDTO identityInfoDTO) {
        saveInitFlow(identityInfoDTO);
        nextFlow(instantsNo, data, identityInfoDTO);
    }


    /**
     * 初始化保存第一个流程节点 并设置为暂停
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void saveInitFlow(IdentityInfoDTO identityInfoDTO) {
        //查询当前流程是否已经开启
        //执行第一个流程
        log.info("流程初始化 {}, 实例编号：{}", flowName, instantsNo);
        //记录表放入第一个步骤：待处理
        if (!EmptyUtil.isEmpty(nodes)) {
            FlowNode flowNode = nodes.get(0);
            ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
            ScfFlowRecord scfFlowRecord = new ScfFlowRecord();
            scfFlowRecord.setFlowName(flowName);
            scfFlowRecord.setInstantsNo(instantsNo);
            scfFlowRecord.setFlowCode(flowCode);
            scfFlowRecord.setNodeCode(flowNode.getNodeCode());
            scfFlowRecord.setNodeName(flowNode.getNodeName());
            scfFlowRecord.setSept(sept.intValue());
            scfFlowRecord.setStatus(Result.StatusEnum.Suspend.getCode());
            scfFlowRecord.setCreateName(identityInfoDTO == null ? null : identityInfoDTO.getRealName());
            scfFlowRecord.setCreateUser(identityInfoDTO == null ? null : identityInfoDTO.getUserId());
            scfFlowRecordManager.insertSelective(scfFlowRecord);
        }
    }


    /**
     * 流转到下一个节点
     * 1、sept 是步揍，从1开始 ，而List<F> nodes 则是对象，index从0开始
     * 2、查询流程在该节点是否有记录，且步骤对应，状态为待处理
     * 3、执行该节点的逻辑
     * 4、
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void nextFlow(String instantsNo, String data, IdentityInfoDTO identityInfo) {
        //1、当步揍小于等于节点的个数的时候就要执行
        if (sept.intValue() <= nodes.size()) {
            int index = sept.intValue() - 1;
            F node = nodes.get(index);
            String nodeName = node.getNodeName();

            //2、查询流程在该节点是否有记录，且步骤对应，状态为待处理
            //执行业务节点
            ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
            ScfFlowRecordQuery scfFlowRecordQuery = new ScfFlowRecordQuery();
            scfFlowRecordQuery.setInstantsNo(instantsNo);
            scfFlowRecordQuery.setSept(sept.intValue());
            scfFlowRecordQuery.setStatus(Result.StatusEnum.Suspend.getCode());
            ScfFlowRecord flowRecord = scfFlowRecordManager.findByQueryContion(scfFlowRecordQuery);
            if (flowRecord == null) {
                log.error("执行时找不到对应的流程记录，instantsNo：{},sept: {}", instantsNo, sept);
                throw new BusinessException("执行时找不到对应的流程记录");
            }

            //3、执行该节点的逻辑
            log.info("执行流程名 {}, 执行第{}步：{}，实例编号：{}", flowName, sept, nodeName, instantsNo);
            Result deal = node.deal(instantsNo, data, identityInfo);
            switch (deal.status) {
                case Success:
                    log.info("执行流程名 {}, 执行第{}步：{}，结果：成功, 实例编号：{}", flowName, sept, nodeName, instantsNo);
                    //将数据库状态修复为成功
                    //创建新的节点
                    successFlow();
                    if (sept.intValue() <= nodes.size()) {
                        nextFlow(instantsNo, data, identityInfo);
                    }
                    break;
                case Fail:
                    log.info("执行流程名 {}, 执行第{}步：{}，结果：失败, 实例编号：{}", flowName, sept, nodeName, instantsNo);
                    failFlow(instantsNo, data, identityInfo);
                    break;
                case Suspend:
                    log.info("执行流程名 {}, 执行第{}步：{}，结果：暂停, 实例编号：{}", flowName, sept, nodeName, instantsNo);
                    break;
                default:
                    break;
            }
        }

    }


    /**
     * 流程成功结束
     */
    public void successFlow() {
        F nodeOld = nodes.get(sept.intValue() - 1);
        String nodeNameOld = nodeOld.getNodeName();
        log.info("执行流程 {}, 实例编号：{}, 执行第{}步完成：{}", flowName, instantsNo, sept, nodeNameOld);
        ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
        ScfFlowRecordQuery scfFlowRecordQuery = new ScfFlowRecordQuery();
        scfFlowRecordQuery.setInstantsNo(instantsNo);
        scfFlowRecordQuery.setSept(sept.intValue());
        scfFlowRecordQuery.setStatus(Result.StatusEnum.Suspend.getCode());
        ScfFlowRecord scfFlowRecordOld = scfFlowRecordManager.findByQueryContion(scfFlowRecordQuery);
        scfFlowRecordOld.setStatus(Result.StatusEnum.Success.getCode());
        scfFlowRecordManager.updateSelective(scfFlowRecordOld);
        //创建新的记录
        if (sept.intValue() < nodes.size()) {
            F node = nodes.get(sept.intValue());
            ScfFlowRecord scfFlowRecord = new ScfFlowRecord();
            scfFlowRecord.setFlowName(flowName);
            scfFlowRecord.setInstantsNo(instantsNo);
            scfFlowRecord.setNodeCode(node.getNodeCode());
            scfFlowRecord.setNodeName(node.getNodeName());
            scfFlowRecord.setFlowCode(flowCode);
            scfFlowRecord.setCreateUser(scfFlowRecordOld.getCreateUser());
            scfFlowRecord.setCreateTime(scfFlowRecordOld.getCreateTime());
            scfFlowRecord.setCreateName(scfFlowRecordOld.getCreateName());
            sept.incrementAndGet();
            scfFlowRecord.setSept(sept.intValue());
            scfFlowRecord.setStatus(Result.StatusEnum.Suspend.getCode());
            scfFlowRecordManager.insertSelective(scfFlowRecord);
        } else {
            sept.incrementAndGet();
        }
    }


    /**
     * 失败流程调用
     */
    public void failFlow(String instantsNo, String data, IdentityInfoDTO identityInfo) {
        int index = sept.intValue() - 1;
        for (int i = index; i >= 0; i--) {
            F f = nodes.get(i);
            if (f != null) {
                f.fail(instantsNo, data, identityInfo);
                log.error("失败流程发起-----执行节点：{}", i);
            }
        }
        //讲暂停的节点，失败
        ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
        ScfFlowRecordQuery scfFlowRecordQuery = new ScfFlowRecordQuery();
        scfFlowRecordQuery.setInstantsNo(instantsNo);
        scfFlowRecordQuery.setSept(sept.intValue());
        scfFlowRecordQuery.setStatus(Result.StatusEnum.Suspend.getCode());
        ScfFlowRecord scfFlowRecordOld = scfFlowRecordManager.findByQueryContion(scfFlowRecordQuery);
        scfFlowRecordOld.setStatus(Result.StatusEnum.Fail.getCode());
        scfFlowRecordManager.updateSelective(scfFlowRecordOld);
    }
}

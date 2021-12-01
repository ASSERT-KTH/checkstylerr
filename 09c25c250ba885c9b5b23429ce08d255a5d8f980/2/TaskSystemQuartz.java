package com.java110.job.quartz;

import com.java110.core.smo.community.ICommunityInnerServiceSMO;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.task.TaskDto;
import com.java110.dto.taskAttr.TaskAttrDto;
import com.java110.job.dao.ITaskServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
public abstract class TaskSystemQuartz {

    protected static final Logger logger = LoggerFactory.getLogger(TaskSystemQuartz.class);

    @Autowired
    private ITaskServiceDao taskServiceDaoImpl;

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;


    public void initTask() {

    }

    /**
     * 启动任务
     *
     * @param taskDto
     */
    public void startTask(TaskDto taskDto) throws Exception {

        Map info = new HashMap();
        info.put("taskId", taskDto.getTaskId());
        List<Map> taskInfos = taskServiceDaoImpl.getTaskInfo(info);
        if (taskInfos == null || taskInfos.size() < 1) {
            return;
        }

        // 这么做是为了，单线程调用，防止多线程导致数据重复处理
        if (!"002".equals(taskInfos.get(0).get("state"))) {
            return;
        }

        String taskId = taskDto.getTaskId();

        if (logger.isDebugEnabled()) {
            logger.debug("---【TaskSystemQuartz.startFtpTask】：任务【" + taskId + "】开始运行！", taskId);
        }

        try {
            // 1.0空方法，让子类去实现
            prepare(taskDto);

            // 3.0核心业务处理逻辑，需要子类去实现
            process(taskDto);

            // 5.0空方法，让子类去实现
            after(taskDto);
        } catch (Exception ex) {

            // 接续向外抛出去
            logger.error("处理出现问题：", ex);
            return;
        }

    }


    /**
     * 主要业务处理（上传下载）,让子类去实现
     *
     * @param taskDto
     */
    protected abstract void process(TaskDto taskDto) throws Exception;

    /**
     * 空方法，如果在事前过程处理前，还需要做一定的处理，需要子类重写这个方法，实现业务逻辑
     *
     * @param taskDto
     */
    protected void prepare(TaskDto taskDto) {

    }

    /**
     * 空方法，如果在事后过程处理完后，还需要做一定的处理，需要子类重写这个方法，实现业务逻辑
     *
     * @param taskDto
     */
    protected void after(TaskDto taskDto) {

    }

    /**
     * 查询小区信息
     *
     * @return
     */
    protected List<CommunityDto> getAllCommunity() {
        CommunityDto communityDto = new CommunityDto();
        communityDto.setState("1100"); //审核过的小区
        List<CommunityDto> communityDtos = communityInnerServiceSMOImpl.queryCommunitys(communityDto);
        return communityDtos;
    }

    /**
     * 获取当前属性
     * @param taskDto
     * @param specCd
     * @return
     */
    protected TaskAttrDto getCurTaskAttr(TaskDto taskDto, String specCd) {
        List<TaskAttrDto> taskAttrDtos = taskDto.getTaskAttr();
        for (TaskAttrDto taskAttrDto : taskAttrDtos) {
            if (specCd.equals(taskAttrDto.getSpecCd())) {
                return taskAttrDto;
            }
        }

        return null;
    }
}

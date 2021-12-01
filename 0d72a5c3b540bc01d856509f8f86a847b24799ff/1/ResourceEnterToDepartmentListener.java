package com.java110.common.activity.resourceEnter;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.smo.auditUser.IAuditUserInnerServiceSMO;
import com.java110.dto.auditUser.AuditUserDto;
import com.java110.dto.resourceStore.ResourceOrderDto;
import com.java110.entity.audit.AuditUser;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.BeanConvertUtil;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.List;

/**
 * 部门领导 监听类
 */
public class ResourceEnterToDepartmentListener implements TaskListener {

    private IAuditUserInnerServiceSMO auditUserInnerServiceSMOImpl;

    @Override
    public void notify(DelegateTask delegateTask) {

        auditUserInnerServiceSMOImpl = ApplicationContextFactory.getBean("auditUserInnerServiceSMOImpl", IAuditUserInnerServiceSMO.class);
        AuditUserDto auditUserDto = new AuditUserDto();
        ResourceOrderDto resourceOrderDto = (ResourceOrderDto)delegateTask.getVariable("resourceOrderDto");
        auditUserDto.setStoreId(resourceOrderDto.getStoreId());
        auditUserDto.setObjCode("resourceEntry");
        auditUserDto.setAuditLink("809001");
        List<AuditUserDto> auditUserDtos = auditUserInnerServiceSMOImpl.queryAuditUsers(auditUserDto);

        for (AuditUserDto tmpAuditUser : auditUserDtos) {
            AuditUser auditUser = BeanConvertUtil.covertBean(tmpAuditUser, AuditUser.class);

            delegateTask.setVariable(auditUser.getUserId(), auditUser);

        }
    }
}

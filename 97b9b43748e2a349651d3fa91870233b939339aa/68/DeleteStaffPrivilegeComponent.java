package com.java110.web.components.staff;


import com.java110.core.context.IPageData;
import com.java110.web.smo.IStaffServiceSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.awt.geom.IllegalPathStateException;

@Component("deleteStaffPrivilege")
public class DeleteStaffPrivilegeComponent {


    @Autowired
    private IStaffServiceSMO staffServiceSMOImpl;

    public ResponseEntity<String> delete(IPageData pd){

        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity =  staffServiceSMOImpl.deleteStaffPrivilege(pd);
        }catch (Exception e){
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }finally {
            return responseEntity;
        }
    }


    public IStaffServiceSMO getStaffServiceSMOImpl() {
        return staffServiceSMOImpl;
    }

    public void setStaffServiceSMOImpl(IStaffServiceSMO staffServiceSMOImpl) {
        this.staffServiceSMOImpl = staffServiceSMOImpl;
    }
}

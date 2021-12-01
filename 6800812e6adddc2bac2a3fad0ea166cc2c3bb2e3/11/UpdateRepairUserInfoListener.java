package com.java110.community.listener.repair;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.community.dao.IRepairUserServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改报修派单信息 侦听
 *
 * 处理节点
 * 1、businessRepairUser:{} 报修派单基本信息节点
 * 2、businessRepairUserAttr:[{}] 报修派单属性信息节点
 * 3、businessRepairUserPhoto:[{}] 报修派单照片信息节点
 * 4、businessRepairUserCerdentials:[{}] 报修派单证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateRepairUserInfoListener")
@Transactional
public class UpdateRepairUserInfoListener extends AbstractRepairUserBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateRepairUserInfoListener.class);
    @Autowired
    private IRepairUserServiceDao repairUserServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_REPAIR_USER;
    }

    /**
     * business过程
     * @param dataFlowContext 上下文对象
     * @param business 业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessRepairUser 节点
        if(data.containsKey("businessRepairUser")){
            //处理 businessRepairUser 节点
            if(data.containsKey("businessRepairUser")){
                Object _obj = data.get("businessRepairUser");
                JSONArray businessRepairUsers = null;
                if(_obj instanceof JSONObject){
                    businessRepairUsers = new JSONArray();
                    businessRepairUsers.add(_obj);
                }else {
                    businessRepairUsers = (JSONArray)_obj;
                }
                //JSONObject businessRepairUser = data.getJSONObject("businessRepairUser");
                for (int _repairUserIndex = 0; _repairUserIndex < businessRepairUsers.size();_repairUserIndex++) {
                    JSONObject businessRepairUser = businessRepairUsers.getJSONObject(_repairUserIndex);
                    doBusinessRepairUser(business, businessRepairUser);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("ruId", businessRepairUser.getString("ruId"));
                    }
                }
            }
        }
    }


    /**
     * business to instance 过程
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //报修派单信息
        List<Map> businessRepairUserInfos = repairUserServiceDaoImpl.getBusinessRepairUserInfo(info);
        if( businessRepairUserInfos != null && businessRepairUserInfos.size() >0) {
            for (int _repairUserIndex = 0; _repairUserIndex < businessRepairUserInfos.size();_repairUserIndex++) {
                Map businessRepairUserInfo = businessRepairUserInfos.get(_repairUserIndex);
                flushBusinessRepairUserInfo(businessRepairUserInfo,StatusConstant.STATUS_CD_VALID);
                repairUserServiceDaoImpl.updateRepairUserInfoInstance(businessRepairUserInfo);
                if(businessRepairUserInfo.size() == 1) {
                    dataFlowContext.addParamOut("ruId", businessRepairUserInfo.get("ru_id"));
                }
            }
        }

    }

    /**
     * 撤单
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {

        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId",bId);
        info.put("statusCd",StatusConstant.STATUS_CD_VALID);
        Map delInfo = new HashMap();
        delInfo.put("bId",business.getbId());
        delInfo.put("operate",StatusConstant.OPERATE_DEL);
        //报修派单信息
        List<Map> repairUserInfo = repairUserServiceDaoImpl.getRepairUserInfo(info);
        if(repairUserInfo != null && repairUserInfo.size() > 0){

            //报修派单信息
            List<Map> businessRepairUserInfos = repairUserServiceDaoImpl.getBusinessRepairUserInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessRepairUserInfos == null || businessRepairUserInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（repairUser），程序内部异常,请检查！ "+delInfo);
            }
            for (int _repairUserIndex = 0; _repairUserIndex < businessRepairUserInfos.size();_repairUserIndex++) {
                Map businessRepairUserInfo = businessRepairUserInfos.get(_repairUserIndex);
                flushBusinessRepairUserInfo(businessRepairUserInfo,StatusConstant.STATUS_CD_VALID);
                repairUserServiceDaoImpl.updateRepairUserInfoInstance(businessRepairUserInfo);
            }
        }

    }



    /**
     * 处理 businessRepairUser 节点
     * @param business 总的数据节点
     * @param businessRepairUser 报修派单节点
     */
    private void doBusinessRepairUser(Business business,JSONObject businessRepairUser){

        Assert.jsonObjectHaveKey(businessRepairUser,"ruId","businessRepairUser 节点下没有包含 ruId 节点");

        if(businessRepairUser.getString("ruId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"ruId 错误，不能自动生成（必须已经存在的ruId）"+businessRepairUser);
        }
        //自动保存DEL
        autoSaveDelBusinessRepairUser(business,businessRepairUser);

        businessRepairUser.put("bId",business.getbId());
        businessRepairUser.put("operate", StatusConstant.OPERATE_ADD);
        //保存报修派单信息
        repairUserServiceDaoImpl.saveBusinessRepairUserInfo(businessRepairUser);

    }




    public IRepairUserServiceDao getRepairUserServiceDaoImpl() {
        return repairUserServiceDaoImpl;
    }

    public void setRepairUserServiceDaoImpl(IRepairUserServiceDao repairUserServiceDaoImpl) {
        this.repairUserServiceDaoImpl = repairUserServiceDaoImpl;
    }



}

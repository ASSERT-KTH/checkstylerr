package com.java110.community.listener.visit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.common.constant.BusinessTypeConstant;
import com.java110.common.constant.ResponseConstant;
import com.java110.common.constant.StatusConstant;
import com.java110.common.exception.ListenerExecuteException;
import com.java110.common.util.Assert;
import com.java110.community.dao.IVisitServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改访客信息信息 侦听
 *
 * 处理节点
 * 1、businessVisit:{} 访客信息基本信息节点
 * 2、businessVisitAttr:[{}] 访客信息属性信息节点
 * 3、businessVisitPhoto:[{}] 访客信息照片信息节点
 * 4、businessVisitCerdentials:[{}] 访客信息证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateVisitInfoListener")
@Transactional
public class UpdateVisitInfoListener extends AbstractVisitBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateVisitInfoListener.class);
    @Autowired
    private IVisitServiceDao visitServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_VISIT;
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

        //处理 businessVisit 节点
        if(data.containsKey("businessVisit")){
            //处理 businessVisit 节点
            if(data.containsKey("businessVisit")){
                Object _obj = data.get("businessVisit");
                JSONArray businessVisits = null;
                if(_obj instanceof JSONObject){
                    businessVisits = new JSONArray();
                    businessVisits.add(_obj);
                }else {
                    businessVisits = (JSONArray)_obj;
                }
                //JSONObject businessVisit = data.getJSONObject("businessVisit");
                for (int _visitIndex = 0; _visitIndex < businessVisits.size();_visitIndex++) {
                    JSONObject businessVisit = businessVisits.getJSONObject(_visitIndex);
                    doBusinessVisit(business, businessVisit);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("vId", businessVisit.getString("vId"));
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

        //访客信息信息
        List<Map> businessVisitInfos = visitServiceDaoImpl.getBusinessVisitInfo(info);
        if( businessVisitInfos != null && businessVisitInfos.size() >0) {
            for (int _visitIndex = 0; _visitIndex < businessVisitInfos.size();_visitIndex++) {
                Map businessVisitInfo = businessVisitInfos.get(_visitIndex);
                flushBusinessVisitInfo(businessVisitInfo,StatusConstant.STATUS_CD_VALID);
                visitServiceDaoImpl.updateVisitInfoInstance(businessVisitInfo);
                if(businessVisitInfo.size() == 1) {
                    dataFlowContext.addParamOut("vId", businessVisitInfo.get("v_id"));
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
        //访客信息信息
        List<Map> visitInfo = visitServiceDaoImpl.getVisitInfo(info);
        if(visitInfo != null && visitInfo.size() > 0){

            //访客信息信息
            List<Map> businessVisitInfos = visitServiceDaoImpl.getBusinessVisitInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessVisitInfos == null || businessVisitInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（visit），程序内部异常,请检查！ "+delInfo);
            }
            for (int _visitIndex = 0; _visitIndex < businessVisitInfos.size();_visitIndex++) {
                Map businessVisitInfo = businessVisitInfos.get(_visitIndex);
                flushBusinessVisitInfo(businessVisitInfo,StatusConstant.STATUS_CD_VALID);
                visitServiceDaoImpl.updateVisitInfoInstance(businessVisitInfo);
            }
        }

    }



    /**
     * 处理 businessVisit 节点
     * @param business 总的数据节点
     * @param businessVisit 访客信息节点
     */
    private void doBusinessVisit(Business business,JSONObject businessVisit){

        Assert.jsonObjectHaveKey(businessVisit,"vId","businessVisit 节点下没有包含 vId 节点");

        if(businessVisit.getString("vId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"vId 错误，不能自动生成（必须已经存在的vId）"+businessVisit);
        }
        //自动保存DEL
        autoSaveDelBusinessVisit(business,businessVisit);

        businessVisit.put("bId",business.getbId());
        businessVisit.put("operate", StatusConstant.OPERATE_ADD);
        //保存访客信息信息
        visitServiceDaoImpl.saveBusinessVisitInfo(businessVisit);

    }




    public IVisitServiceDao getVisitServiceDaoImpl() {
        return visitServiceDaoImpl;
    }

    public void setVisitServiceDaoImpl(IVisitServiceDao visitServiceDaoImpl) {
        this.visitServiceDaoImpl = visitServiceDaoImpl;
    }



}

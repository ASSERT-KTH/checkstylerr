package com.java110.community.listener.inspectionPoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.community.dao.IInspectionServiceDao;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
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
 * 删除巡检点信息 侦听
 *
 * 处理节点
 * 1、businessInspection:{} 巡检点基本信息节点
 * 2、businessInspectionAttr:[{}] 巡检点属性信息节点
 * 3、businessInspectionPhoto:[{}] 巡检点照片信息节点
 * 4、businessInspectionCerdentials:[{}] 巡检点证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteInspectionInfoListener")
@Transactional
public class DeleteInspectionInfoListener extends AbstractInspectionBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(DeleteInspectionInfoListener.class);
    @Autowired
    IInspectionServiceDao inspectionServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_INSPECTION;
    }

    /**
     * 根据删除信息 查出Instance表中数据 保存至business表 （状态写DEL） 方便撤单时直接更新回去
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessInspection 节点
        if(data.containsKey("businessInspectionPoint")){
            //处理 businessInspection 节点
            if(data.containsKey("businessInspectionPoint")){
                Object _obj = data.get("businessInspectionPoint");
                JSONArray businessInspections = null;
                if(_obj instanceof JSONObject){
                    businessInspections = new JSONArray();
                    businessInspections.add(_obj);
                }else {
                    businessInspections = (JSONArray)_obj;
                }
                //JSONObject businessInspection = data.getJSONObject("businessInspection");
                for (int _inspectionIndex = 0; _inspectionIndex < businessInspections.size();_inspectionIndex++) {
                    JSONObject businessInspection = businessInspections.getJSONObject(_inspectionIndex);
                    doBusinessInspection(business, businessInspection);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("inspectionId", businessInspection.getString("inspectionId"));
                    }
                }
            }
        }


    }

    /**
     * 删除 instance数据
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");

        //巡检点信息
        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_DEL);

        //巡检点信息
        List<Map> businessInspectionInfos = inspectionServiceDaoImpl.getBusinessInspectionInfo(info);
        if( businessInspectionInfos != null && businessInspectionInfos.size() >0) {
            for (int _inspectionIndex = 0; _inspectionIndex < businessInspectionInfos.size();_inspectionIndex++) {
                Map businessInspectionInfo = businessInspectionInfos.get(_inspectionIndex);
                flushBusinessInspectionInfo(businessInspectionInfo,StatusConstant.STATUS_CD_INVALID);
                inspectionServiceDaoImpl.updateInspectionInfoInstance(businessInspectionInfo);
                dataFlowContext.addParamOut("inspectionId",businessInspectionInfo.get("inspection_id"));
            }
        }

    }

    /**
     * 撤单
     * 从business表中查询到DEL的数据 将instance中的数据更新回来
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId",bId);
        info.put("statusCd",StatusConstant.STATUS_CD_INVALID);

        Map delInfo = new HashMap();
        delInfo.put("bId",business.getbId());
        delInfo.put("operate",StatusConstant.OPERATE_DEL);
        //巡检点信息
        List<Map> inspectionInfo = inspectionServiceDaoImpl.getInspectionInfo(info);
        if(inspectionInfo != null && inspectionInfo.size() > 0){

            //巡检点信息
            List<Map> businessInspectionInfos = inspectionServiceDaoImpl.getBusinessInspectionInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessInspectionInfos == null ||  businessInspectionInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（inspection），程序内部异常,请检查！ "+delInfo);
            }
            for (int _inspectionIndex = 0; _inspectionIndex < businessInspectionInfos.size();_inspectionIndex++) {
                Map businessInspectionInfo = businessInspectionInfos.get(_inspectionIndex);
                flushBusinessInspectionInfo(businessInspectionInfo,StatusConstant.STATUS_CD_VALID);
                inspectionServiceDaoImpl.updateInspectionInfoInstance(businessInspectionInfo);
            }
        }
    }



    /**
     * 处理 businessInspection 节点
     * @param business 总的数据节点
     * @param businessInspection 巡检点节点
     */
    private void doBusinessInspection(Business business,JSONObject businessInspection){

        Assert.jsonObjectHaveKey(businessInspection,"inspectionId","businessInspection 节点下没有包含 inspectionId 节点");

        if(businessInspection.getString("inspectionId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"inspectionId 错误，不能自动生成（必须已经存在的inspectionId）"+businessInspection);
        }
        //自动插入DEL
        autoSaveDelBusinessInspection(business,businessInspection);
    }

    public IInspectionServiceDao getInspectionServiceDaoImpl() {
        return inspectionServiceDaoImpl;
    }

    public void setInspectionServiceDaoImpl(IInspectionServiceDao inspectionServiceDaoImpl) {
        this.inspectionServiceDaoImpl = inspectionServiceDaoImpl;
    }
}

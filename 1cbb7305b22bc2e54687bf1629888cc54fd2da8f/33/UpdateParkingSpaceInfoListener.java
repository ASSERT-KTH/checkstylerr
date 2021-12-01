package com.java110.community.listener.parkingSpace;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.common.constant.BusinessTypeConstant;
import com.java110.common.constant.ResponseConstant;
import com.java110.common.constant.StatusConstant;
import com.java110.common.exception.ListenerExecuteException;
import com.java110.common.util.Assert;
import com.java110.community.dao.IParkingSpaceServiceDao;
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
 * 修改停车位信息 侦听
 * <p>
 * 处理节点
 * 1、businessParkingSpace:{} 停车位基本信息节点
 * 2、businessParkingSpaceAttr:[{}] 停车位属性信息节点
 * 3、businessParkingSpacePhoto:[{}] 停车位照片信息节点
 * 4、businessParkingSpaceCerdentials:[{}] 停车位证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateParkingSpaceInfoListener")
@Transactional
public class UpdateParkingSpaceInfoListener extends AbstractParkingSpaceBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateParkingSpaceInfoListener.class);
    @Autowired
    private IParkingSpaceServiceDao parkingSpaceServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_PARKING_SPACE;
    }

    /**
     * business过程
     *
     * @param dataFlowContext 上下文对象
     * @param business        业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Assert.notEmpty(data, "没有datas 节点，或没有子节点需要处理");

        //处理 businessParkingSpace 节点
        if (data.containsKey("businessParkingSpace")) {
            //处理 businessParkingSpace 节点
            if (data.containsKey("businessParkingSpace")) {
                Object _obj = data.get("businessParkingSpace");
                JSONArray businessParkingSpaces = null;
                if (_obj instanceof JSONObject) {
                    businessParkingSpaces = new JSONArray();
                    businessParkingSpaces.add(_obj);
                } else {
                    businessParkingSpaces = (JSONArray) _obj;
                }
                //JSONObject businessParkingSpace = data.getJSONObject("businessParkingSpace");
                for (int _parkingSpaceIndex = 0; _parkingSpaceIndex < businessParkingSpaces.size(); _parkingSpaceIndex++) {
                    JSONObject businessParkingSpace = businessParkingSpaces.getJSONObject(_parkingSpaceIndex);
                    doBusinessParkingSpace(business, businessParkingSpace);
                    if (_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("psId", businessParkingSpace.getString("psId"));
                    }
                }
            }
        }
    }


    /**
     * business to instance 过程
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_ADD);

        //停车位信息
        List<Map> businessParkingSpaceInfos = parkingSpaceServiceDaoImpl.getBusinessParkingSpaceInfo(info);
        if (businessParkingSpaceInfos != null && businessParkingSpaceInfos.size() > 0) {
            for (int _parkingSpaceIndex = 0; _parkingSpaceIndex < businessParkingSpaceInfos.size(); _parkingSpaceIndex++) {
                Map businessParkingSpaceInfo = businessParkingSpaceInfos.get(_parkingSpaceIndex);
                flushBusinessParkingSpaceInfo(businessParkingSpaceInfo, StatusConstant.STATUS_CD_VALID);
                parkingSpaceServiceDaoImpl.updateParkingSpaceInfoInstance(businessParkingSpaceInfo);
                if (businessParkingSpaceInfo.size() == 1) {
                    dataFlowContext.addParamOut("psId", businessParkingSpaceInfo.get("ps_id"));
                }
            }
        }

    }

    /**
     * 撤单
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {

        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId", bId);
        info.put("statusCd", StatusConstant.STATUS_CD_VALID);
        Map delInfo = new HashMap();
        delInfo.put("bId", business.getbId());
        delInfo.put("operate", StatusConstant.OPERATE_DEL);
        //停车位信息
        List<Map> parkingSpaceInfo = parkingSpaceServiceDaoImpl.getParkingSpaceInfo(info);
        if (parkingSpaceInfo != null && parkingSpaceInfo.size() > 0) {

            //停车位信息
            List<Map> businessParkingSpaceInfos = parkingSpaceServiceDaoImpl.getBusinessParkingSpaceInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessParkingSpaceInfos == null || businessParkingSpaceInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（parkingSpace），程序内部异常,请检查！ " + delInfo);
            }
            for (int _parkingSpaceIndex = 0; _parkingSpaceIndex < businessParkingSpaceInfos.size(); _parkingSpaceIndex++) {
                Map businessParkingSpaceInfo = businessParkingSpaceInfos.get(_parkingSpaceIndex);
                flushBusinessParkingSpaceInfo(businessParkingSpaceInfo, StatusConstant.STATUS_CD_VALID);
                parkingSpaceServiceDaoImpl.updateParkingSpaceInfoInstance(businessParkingSpaceInfo);
            }
        }

    }


    /**
     * 处理 businessParkingSpace 节点
     *
     * @param business             总的数据节点
     * @param businessParkingSpace 停车位节点
     */
    private void doBusinessParkingSpace(Business business, JSONObject businessParkingSpace) {

        Assert.jsonObjectHaveKey(businessParkingSpace, "psId", "businessParkingSpace 节点下没有包含 psId 节点");

        if (businessParkingSpace.getString("psId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "psId 错误，不能自动生成（必须已经存在的psId）" + businessParkingSpace);
        }
        //自动保存DEL
        autoSaveDelBusinessParkingSpace(business, businessParkingSpace);

        businessParkingSpace.put("bId", business.getbId());
        businessParkingSpace.put("operate", StatusConstant.OPERATE_ADD);
        //保存停车位信息
        parkingSpaceServiceDaoImpl.saveBusinessParkingSpaceInfo(businessParkingSpace);

    }


    public IParkingSpaceServiceDao getParkingSpaceServiceDaoImpl() {
        return parkingSpaceServiceDaoImpl;
    }

    public void setParkingSpaceServiceDaoImpl(IParkingSpaceServiceDao parkingSpaceServiceDaoImpl) {
        this.parkingSpaceServiceDaoImpl = parkingSpaceServiceDaoImpl;
    }


}

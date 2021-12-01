package com.java110.user.listener.car;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.common.constant.BusinessTypeConstant;
import com.java110.common.constant.ResponseConstant;
import com.java110.common.constant.StatusConstant;
import com.java110.common.exception.ListenerExecuteException;
import com.java110.common.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.user.dao.IOwnerCarServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除车辆管理信息 侦听
 * <p>
 * 处理节点
 * 1、businessOwnerCar:{} 车辆管理基本信息节点
 * 2、businessOwnerCarAttr:[{}] 车辆管理属性信息节点
 * 3、businessOwnerCarPhoto:[{}] 车辆管理照片信息节点
 * 4、businessOwnerCarCerdentials:[{}] 车辆管理证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteOwnerCarInfoListener")
@Transactional
public class DeleteOwnerCarInfoListener extends AbstractOwnerCarBusinessServiceDataFlowListener {

    private  static Logger logger = LoggerFactory.getLogger(DeleteOwnerCarInfoListener.class);
    @Autowired
    IOwnerCarServiceDao ownerCarServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_OWNER_CAR;
    }

    /**
     * 根据删除信息 查出Instance表中数据 保存至business表 （状态写DEL） 方便撤单时直接更新回去
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Assert.notEmpty(data, "没有datas 节点，或没有子节点需要处理");

        //处理 businessOwnerCar 节点
        if (data.containsKey("businessOwnerCar")) {
            //处理 businessOwnerCar 节点
            if (data.containsKey("businessOwnerCar")) {
                Object _obj = data.get("businessOwnerCar");
                JSONArray businessOwnerCars = null;
                if (_obj instanceof JSONObject) {
                    businessOwnerCars = new JSONArray();
                    businessOwnerCars.add(_obj);
                } else {
                    businessOwnerCars = (JSONArray) _obj;
                }
                //JSONObject businessOwnerCar = data.getJSONObject("businessOwnerCar");
                for (int _ownerCarIndex = 0; _ownerCarIndex < businessOwnerCars.size(); _ownerCarIndex++) {
                    JSONObject businessOwnerCar = businessOwnerCars.getJSONObject(_ownerCarIndex);
                    doBusinessOwnerCar(business, businessOwnerCar);
                    if (_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("carId", businessOwnerCar.getString("carId"));
                    }
                }
            }
        }


    }

    /**
     * 删除 instance数据
     *
     * @param dataFlowContext 数据对象
     * @param business        当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");

        //车辆管理信息
        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_DEL);

        //车辆管理信息
        List<Map> businessOwnerCarInfos = ownerCarServiceDaoImpl.getBusinessOwnerCarInfo(info);
        if (businessOwnerCarInfos != null && businessOwnerCarInfos.size() > 0) {
            for (int _ownerCarIndex = 0; _ownerCarIndex < businessOwnerCarInfos.size(); _ownerCarIndex++) {
                Map businessOwnerCarInfo = businessOwnerCarInfos.get(_ownerCarIndex);
                flushBusinessOwnerCarInfo(businessOwnerCarInfo, StatusConstant.STATUS_CD_INVALID);
                ownerCarServiceDaoImpl.updateOwnerCarInfoInstance(businessOwnerCarInfo);
                dataFlowContext.addParamOut("carId", businessOwnerCarInfo.get("car_id"));
            }
        }

    }

    /**
     * 撤单
     * 从business表中查询到DEL的数据 将instance中的数据更新回来
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
        info.put("statusCd", StatusConstant.STATUS_CD_INVALID);

        Map delInfo = new HashMap();
        delInfo.put("bId", business.getbId());
        delInfo.put("operate", StatusConstant.OPERATE_DEL);
        //车辆管理信息
        List<Map> ownerCarInfo = ownerCarServiceDaoImpl.getOwnerCarInfo(info);
        if (ownerCarInfo != null && ownerCarInfo.size() > 0) {

            //车辆管理信息
            List<Map> businessOwnerCarInfos = ownerCarServiceDaoImpl.getBusinessOwnerCarInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessOwnerCarInfos == null || businessOwnerCarInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（ownerCar），程序内部异常,请检查！ " + delInfo);
            }
            for (int _ownerCarIndex = 0; _ownerCarIndex < businessOwnerCarInfos.size(); _ownerCarIndex++) {
                Map businessOwnerCarInfo = businessOwnerCarInfos.get(_ownerCarIndex);
                flushBusinessOwnerCarInfo(businessOwnerCarInfo, StatusConstant.STATUS_CD_VALID);
                ownerCarServiceDaoImpl.updateOwnerCarInfoInstance(businessOwnerCarInfo);
            }
        }
    }


    /**
     * 处理 businessOwnerCar 节点
     *
     * @param business         总的数据节点
     * @param businessOwnerCar 车辆管理节点
     */
    private void doBusinessOwnerCar(Business business, JSONObject businessOwnerCar) {

        Assert.jsonObjectHaveKey(businessOwnerCar, "carId", "businessOwnerCar 节点下没有包含 carId 节点");

        if (businessOwnerCar.getString("carId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "carId 错误，不能自动生成（必须已经存在的carId）" + businessOwnerCar);
        }
        //自动插入DEL
        autoSaveDelBusinessOwnerCar(business, businessOwnerCar);
    }

    public IOwnerCarServiceDao getOwnerCarServiceDaoImpl() {
        return ownerCarServiceDaoImpl;
    }

    public void setOwnerCarServiceDaoImpl(IOwnerCarServiceDao ownerCarServiceDaoImpl) {
        this.ownerCarServiceDaoImpl = ownerCarServiceDaoImpl;
    }
}

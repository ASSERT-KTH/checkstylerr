package com.java110.user.listener.ownerRoomRel;

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
import com.java110.user.dao.IOwnerRoomRelServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除业主房屋信息 侦听
 * <p>
 * 处理节点
 * 1、businessOwnerRoomRel:{} 业主房屋基本信息节点
 * 2、businessOwnerRoomRelAttr:[{}] 业主房屋属性信息节点
 * 3、businessOwnerRoomRelPhoto:[{}] 业主房屋照片信息节点
 * 4、businessOwnerRoomRelCerdentials:[{}] 业主房屋证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteOwnerRoomRelInfoListener")
@Transactional
public class DeleteOwnerRoomRelInfoListener extends AbstractOwnerRoomRelBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(DeleteOwnerRoomRelInfoListener.class);
    @Autowired
    IOwnerRoomRelServiceDao ownerRoomRelServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_OWNER_ROOM_REL;
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

        //处理 businessOwnerRoomRel 节点
        if (data.containsKey("businessOwnerRoomRel")) {
            //处理 businessOwnerRoomRel 节点
            if (data.containsKey("businessOwnerRoomRel")) {
                Object _obj = data.get("businessOwnerRoomRel");
                JSONArray businessOwnerRoomRels = null;
                if (_obj instanceof JSONObject) {
                    businessOwnerRoomRels = new JSONArray();
                    businessOwnerRoomRels.add(_obj);
                } else {
                    businessOwnerRoomRels = (JSONArray) _obj;
                }
                //JSONObject businessOwnerRoomRel = data.getJSONObject("businessOwnerRoomRel");
                for (int _ownerRoomRelIndex = 0; _ownerRoomRelIndex < businessOwnerRoomRels.size(); _ownerRoomRelIndex++) {
                    JSONObject businessOwnerRoomRel = businessOwnerRoomRels.getJSONObject(_ownerRoomRelIndex);
                    doBusinessOwnerRoomRel(business, businessOwnerRoomRel);
                    if (_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("relId", businessOwnerRoomRel.getString("rel_id"));
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

        //业主房屋信息
        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_DEL);

        //业主房屋信息
        List<Map> businessOwnerRoomRelInfos = ownerRoomRelServiceDaoImpl.getBusinessOwnerRoomRelInfo(info);
        if (businessOwnerRoomRelInfos != null && businessOwnerRoomRelInfos.size() > 0) {
            for (int _ownerRoomRelIndex = 0; _ownerRoomRelIndex < businessOwnerRoomRelInfos.size(); _ownerRoomRelIndex++) {
                Map businessOwnerRoomRelInfo = businessOwnerRoomRelInfos.get(_ownerRoomRelIndex);
                flushBusinessOwnerRoomRelInfo(businessOwnerRoomRelInfo, StatusConstant.STATUS_CD_INVALID);
                ownerRoomRelServiceDaoImpl.updateOwnerRoomRelInfoInstance(businessOwnerRoomRelInfo);
                dataFlowContext.addParamOut("relId", businessOwnerRoomRelInfo.get("rel_id"));
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
        //业主房屋信息
        List<Map> ownerRoomRelInfo = ownerRoomRelServiceDaoImpl.getOwnerRoomRelInfo(info);
        if (ownerRoomRelInfo != null && ownerRoomRelInfo.size() > 0) {

            //业主房屋信息
            List<Map> businessOwnerRoomRelInfos = ownerRoomRelServiceDaoImpl.getBusinessOwnerRoomRelInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessOwnerRoomRelInfos == null || businessOwnerRoomRelInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（ownerRoomRel），程序内部异常,请检查！ " + delInfo);
            }
            for (int _ownerRoomRelIndex = 0; _ownerRoomRelIndex < businessOwnerRoomRelInfos.size(); _ownerRoomRelIndex++) {
                Map businessOwnerRoomRelInfo = businessOwnerRoomRelInfos.get(_ownerRoomRelIndex);
                flushBusinessOwnerRoomRelInfo(businessOwnerRoomRelInfo, StatusConstant.STATUS_CD_VALID);
                ownerRoomRelServiceDaoImpl.updateOwnerRoomRelInfoInstance(businessOwnerRoomRelInfo);
            }
        }
    }


    /**
     * 处理 businessOwnerRoomRel 节点
     *
     * @param business             总的数据节点
     * @param businessOwnerRoomRel 业主房屋节点
     */
    private void doBusinessOwnerRoomRel(Business business, JSONObject businessOwnerRoomRel) {

        Assert.jsonObjectHaveKey(businessOwnerRoomRel, "relId", "businessOwnerRoomRel 节点下没有包含 relId 节点");

        if (businessOwnerRoomRel.getString("relId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "relId 错误，不能自动生成（必须已经存在的relId）" + businessOwnerRoomRel);
        }
        //自动插入DEL
        autoSaveDelBusinessOwnerRoomRel(business, businessOwnerRoomRel);
    }

    public IOwnerRoomRelServiceDao getOwnerRoomRelServiceDaoImpl() {
        return ownerRoomRelServiceDaoImpl;
    }

    public void setOwnerRoomRelServiceDaoImpl(IOwnerRoomRelServiceDao ownerRoomRelServiceDaoImpl) {
        this.ownerRoomRelServiceDaoImpl = ownerRoomRelServiceDaoImpl;
    }
}

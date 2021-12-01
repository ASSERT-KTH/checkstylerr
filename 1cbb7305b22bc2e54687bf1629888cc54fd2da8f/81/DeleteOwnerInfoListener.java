package com.java110.user.listener.owner;

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
import com.java110.user.dao.IOwnerServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除业主信息 侦听
 * <p>
 * 处理节点
 * 1、businessOwner:{} 业主基本信息节点
 * 2、businessOwnerAttr:[{}] 业主属性信息节点
 * 3、businessOwnerPhoto:[{}] 业主照片信息节点
 * 4、businessOwnerCerdentials:[{}] 业主证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteOwnerInfoListener")
@Transactional
public class DeleteOwnerInfoListener extends AbstractOwnerBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(DeleteOwnerInfoListener.class);
    @Autowired
    IOwnerServiceDao ownerServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_OWNER_INFO;
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

        //处理 businessOwner 节点
        if (data.containsKey("businessOwner")) {
            //处理 businessOwner 节点
            if (data.containsKey("businessOwner")) {
                Object _obj = data.get("businessOwner");
                JSONArray businessOwners = null;
                if (_obj instanceof JSONObject) {
                    businessOwners = new JSONArray();
                    businessOwners.add(_obj);
                } else {
                    businessOwners = (JSONArray) _obj;
                }
                //JSONObject businessOwner = data.getJSONObject("businessOwner");
                for (int _ownerIndex = 0; _ownerIndex < businessOwners.size(); _ownerIndex++) {
                    JSONObject businessOwner = businessOwners.getJSONObject(_ownerIndex);
                    doBusinessOwner(business, businessOwner);
                    if (_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("memberId", businessOwner.getString("member_id"));
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

        //业主信息
        Map info = new HashMap();
        info.put("bId", business.getbId());
        info.put("operate", StatusConstant.OPERATE_DEL);

        //业主信息
        List<Map> businessOwnerInfos = ownerServiceDaoImpl.getBusinessOwnerInfo(info);
        if (businessOwnerInfos != null && businessOwnerInfos.size() > 0) {
            for (int _ownerIndex = 0; _ownerIndex < businessOwnerInfos.size(); _ownerIndex++) {
                Map businessOwnerInfo = businessOwnerInfos.get(_ownerIndex);
                flushBusinessOwnerInfo(businessOwnerInfo, StatusConstant.STATUS_CD_INVALID);
                ownerServiceDaoImpl.updateOwnerInfoInstance(businessOwnerInfo);
                dataFlowContext.addParamOut("memberId", businessOwnerInfo.get("member_id"));
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
        //业主信息
        List<Map> ownerInfo = ownerServiceDaoImpl.getOwnerInfo(info);
        if (ownerInfo != null && ownerInfo.size() > 0) {

            //业主信息
            List<Map> businessOwnerInfos = ownerServiceDaoImpl.getBusinessOwnerInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessOwnerInfos == null || businessOwnerInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（owner），程序内部异常,请检查！ " + delInfo);
            }
            for (int _ownerIndex = 0; _ownerIndex < businessOwnerInfos.size(); _ownerIndex++) {
                Map businessOwnerInfo = businessOwnerInfos.get(_ownerIndex);
                flushBusinessOwnerInfo(businessOwnerInfo, StatusConstant.STATUS_CD_VALID);
                ownerServiceDaoImpl.updateOwnerInfoInstance(businessOwnerInfo);
            }
        }
    }


    /**
     * 处理 businessOwner 节点
     *
     * @param business      总的数据节点
     * @param businessOwner 业主节点
     */
    private void doBusinessOwner(Business business, JSONObject businessOwner) {

        Assert.jsonObjectHaveKey(businessOwner, "memberId", "businessOwner 节点下没有包含 memberId 节点");

        if (businessOwner.getString("memberId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "memberId 错误，不能自动生成（必须已经存在的ownerId）" + businessOwner);
        }
        //自动插入DEL
        autoSaveDelBusinessOwner(business, businessOwner);
    }

    public IOwnerServiceDao getOwnerServiceDaoImpl() {
        return ownerServiceDaoImpl;
    }

    public void setOwnerServiceDaoImpl(IOwnerServiceDao ownerServiceDaoImpl) {
        this.ownerServiceDaoImpl = ownerServiceDaoImpl;
    }
}

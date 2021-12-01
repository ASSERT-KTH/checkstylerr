package com.java110.fee.listener.attrs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.fee.dao.IFeeAttrServiceDao;
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
 * 修改费用属性信息 侦听
 * <p>
 * 处理节点
 * 1、businessFeeAttr:{} 费用属性基本信息节点
 * 2、businessFeeAttrAttr:[{}] 费用属性属性信息节点
 * 3、businessFeeAttrPhoto:[{}] 费用属性照片信息节点
 * 4、businessFeeAttrCerdentials:[{}] 费用属性证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateFeeAttrInfoListener")
@Transactional
public class UpdateFeeAttrInfoListener extends AbstractFeeAttrBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateFeeAttrInfoListener.class);
    @Autowired
    private IFeeAttrServiceDao feeAttrServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_FEE_INFO;
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


        //处理 businessFeeAttr 节点
        if (data.containsKey(BusinessTypeConstant.BUSINESS_TYPE_UPDATE_FEE_INFO)) {
            Object _obj = data.get(BusinessTypeConstant.BUSINESS_TYPE_UPDATE_FEE_INFO);
            JSONArray businessFeeAttrs = null;
            if (_obj instanceof JSONObject) {
                businessFeeAttrs = new JSONArray();
                businessFeeAttrs.add(_obj);
            } else {
                businessFeeAttrs = (JSONArray) _obj;
            }
            //JSONObject businessFeeAttr = data.getJSONObject("businessFeeAttr");
            for (int _feeAttrIndex = 0; _feeAttrIndex < businessFeeAttrs.size(); _feeAttrIndex++) {
                JSONObject businessFeeAttr = businessFeeAttrs.getJSONObject(_feeAttrIndex);
                doBusinessFeeAttr(business, businessFeeAttr);
                if (_obj instanceof JSONObject) {
                    dataFlowContext.addParamOut("attrId", businessFeeAttr.getString("attrId"));
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

        //费用属性信息
        List<Map> businessFeeAttrInfos = feeAttrServiceDaoImpl.getBusinessFeeAttrInfo(info);
        if (businessFeeAttrInfos != null && businessFeeAttrInfos.size() > 0) {
            for (int _feeAttrIndex = 0; _feeAttrIndex < businessFeeAttrInfos.size(); _feeAttrIndex++) {
                Map businessFeeAttrInfo = businessFeeAttrInfos.get(_feeAttrIndex);
                flushBusinessFeeAttrInfo(businessFeeAttrInfo, StatusConstant.STATUS_CD_VALID);
                feeAttrServiceDaoImpl.updateFeeAttrInfoInstance(businessFeeAttrInfo);
                if (businessFeeAttrInfo.size() == 1) {
                    dataFlowContext.addParamOut("attrId", businessFeeAttrInfo.get("attr_id"));
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
        //费用属性信息
        List<Map> feeAttrInfo = feeAttrServiceDaoImpl.getFeeAttrInfo(info);
        if (feeAttrInfo != null && feeAttrInfo.size() > 0) {

            //费用属性信息
            List<Map> businessFeeAttrInfos = feeAttrServiceDaoImpl.getBusinessFeeAttrInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessFeeAttrInfos == null || businessFeeAttrInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（feeAttr），程序内部异常,请检查！ " + delInfo);
            }
            for (int _feeAttrIndex = 0; _feeAttrIndex < businessFeeAttrInfos.size(); _feeAttrIndex++) {
                Map businessFeeAttrInfo = businessFeeAttrInfos.get(_feeAttrIndex);
                flushBusinessFeeAttrInfo(businessFeeAttrInfo, StatusConstant.STATUS_CD_VALID);
                feeAttrServiceDaoImpl.updateFeeAttrInfoInstance(businessFeeAttrInfo);
            }
        }

    }


    /**
     * 处理 businessFeeAttr 节点
     *
     * @param business        总的数据节点
     * @param businessFeeAttr 费用属性节点
     */
    private void doBusinessFeeAttr(Business business, JSONObject businessFeeAttr) {

        Assert.jsonObjectHaveKey(businessFeeAttr, "attrId", "businessFeeAttr 节点下没有包含 attrId 节点");

        if (businessFeeAttr.getString("attrId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "attrId 错误，不能自动生成（必须已经存在的attrId）" + businessFeeAttr);
        }
        //自动保存DEL
        autoSaveDelBusinessFeeAttr(business, businessFeeAttr);

        businessFeeAttr.put("bId", business.getbId());
        businessFeeAttr.put("operate", StatusConstant.OPERATE_ADD);
        //保存费用属性信息
        feeAttrServiceDaoImpl.saveBusinessFeeAttrInfo(businessFeeAttr);

    }


    public IFeeAttrServiceDao getFeeAttrServiceDaoImpl() {
        return feeAttrServiceDaoImpl;
    }

    public void setFeeAttrServiceDaoImpl(IFeeAttrServiceDao feeAttrServiceDaoImpl) {
        this.feeAttrServiceDaoImpl = feeAttrServiceDaoImpl;
    }


}

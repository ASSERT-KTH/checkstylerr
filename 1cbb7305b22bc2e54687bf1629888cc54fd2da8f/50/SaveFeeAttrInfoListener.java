package com.java110.fee.listener.attrs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.common.constant.BusinessTypeConstant;
import com.java110.common.constant.StatusConstant;
import com.java110.common.util.Assert;
import com.java110.fee.dao.IFeeAttrServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.entity.center.Business;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存 费用属性信息 侦听
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("saveFeeAttrInfoListener")
@Transactional
public class SaveFeeAttrInfoListener extends AbstractFeeAttrBusinessServiceDataFlowListener{

    private static Logger logger = LoggerFactory.getLogger(SaveFeeAttrInfoListener.class);

    @Autowired
    private IFeeAttrServiceDao feeAttrServiceDaoImpl;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_SAVE_FEE_INFO;
    }

    /**
     * 保存费用属性信息 business 表中
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();
        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessFeeAttr 节点
        if(data.containsKey("businessFeeAttr")){
            Object bObj = data.get("businessFeeAttr");
            JSONArray businessFeeAttrs = null;
            if(bObj instanceof JSONObject){
                businessFeeAttrs = new JSONArray();
                businessFeeAttrs.add(bObj);
            }else {
                businessFeeAttrs = (JSONArray)bObj;
            }
            //JSONObject businessFeeAttr = data.getJSONObject("businessFeeAttr");
            for (int bFeeAttrIndex = 0; bFeeAttrIndex < businessFeeAttrs.size();bFeeAttrIndex++) {
                JSONObject businessFeeAttr = businessFeeAttrs.getJSONObject(bFeeAttrIndex);
                doBusinessFeeAttr(business, businessFeeAttr);
                if(bObj instanceof JSONObject) {
                    dataFlowContext.addParamOut("attrId", businessFeeAttr.getString("attrId"));
                }
            }
        }
    }

    /**
     * business 数据转移到 instance
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //费用属性信息
        List<Map> businessFeeAttrInfo = feeAttrServiceDaoImpl.getBusinessFeeAttrInfo(info);
        if( businessFeeAttrInfo != null && businessFeeAttrInfo.size() >0) {
            reFreshShareColumn(info, businessFeeAttrInfo.get(0));
            feeAttrServiceDaoImpl.saveFeeAttrInfoInstance(info);
            if(businessFeeAttrInfo.size() == 1) {
                dataFlowContext.addParamOut("attrId", businessFeeAttrInfo.get(0).get("attr_id"));
            }
        }
    }


    /**
     * 刷 分片字段
     *
     * @param info         查询对象
     * @param businessInfo 小区ID
     */
    private void reFreshShareColumn(Map info, Map businessInfo) {

        if (info.containsKey("communityId")) {
            return;
        }

        if (!businessInfo.containsKey("community_id")) {
            return;
        }

        info.put("communityId", businessInfo.get("community_id"));
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
        Map paramIn = new HashMap();
        paramIn.put("bId",bId);
        paramIn.put("statusCd",StatusConstant.STATUS_CD_INVALID);
        //费用属性信息
        List<Map> feeAttrInfo = feeAttrServiceDaoImpl.getFeeAttrInfo(info);
        if(feeAttrInfo != null && feeAttrInfo.size() > 0){
            reFreshShareColumn(paramIn, feeAttrInfo.get(0));
            feeAttrServiceDaoImpl.updateFeeAttrInfoInstance(paramIn);
        }
    }



    /**
     * 处理 businessFeeAttr 节点
     * @param business 总的数据节点
     * @param businessFeeAttr 费用属性节点
     */
    private void doBusinessFeeAttr(Business business,JSONObject businessFeeAttr){

        Assert.jsonObjectHaveKey(businessFeeAttr,"attrId","businessFeeAttr 节点下没有包含 attrId 节点");

        if(businessFeeAttr.getString("attrId").startsWith("-")){
            //刷新缓存
            //flushFeeAttrId(business.getDatas());

            businessFeeAttr.put("attrId",GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_attrId));

        }

        businessFeeAttr.put("bId",business.getbId());
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

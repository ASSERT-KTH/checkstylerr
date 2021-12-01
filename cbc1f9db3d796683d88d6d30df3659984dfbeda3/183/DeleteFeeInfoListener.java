package com.java110.fee.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.fee.dao.IFeeServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除费用信息 侦听
 *
 * 处理节点
 * 1、businessFee:{} 费用基本信息节点
 * 2、businessFeeAttr:[{}] 费用属性信息节点
 * 3、businessFeePhoto:[{}] 费用照片信息节点
 * 4、businessFeeCerdentials:[{}] 费用证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteFeeInfoListener")
@Transactional
public class DeleteFeeInfoListener extends AbstractFeeBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(DeleteFeeInfoListener.class);
    @Autowired
    IFeeServiceDao feeServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_FEE_INFO;
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

        //处理 businessFee 节点
        if(data.containsKey("businessFee")){
            //处理 businessFee 节点
            if(data.containsKey("businessFee")){
                Object _obj = data.get("businessFee");
                JSONArray businessFees = null;
                if(_obj instanceof JSONObject){
                    businessFees = new JSONArray();
                    businessFees.add(_obj);
                }else {
                    businessFees = (JSONArray)_obj;
                }
                //JSONObject businessFee = data.getJSONObject("businessFee");
                for (int _feeIndex = 0; _feeIndex < businessFees.size();_feeIndex++) {
                    JSONObject businessFee = businessFees.getJSONObject(_feeIndex);
                    doBusinessFee(business, businessFee);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("feeId", businessFee.getString("feeId"));
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

        //费用信息
        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_DEL);

        //费用信息
        List<Map> businessFeeInfos = feeServiceDaoImpl.getBusinessFeeInfo(info);
        if( businessFeeInfos != null && businessFeeInfos.size() >0) {
            for (int _feeIndex = 0; _feeIndex < businessFeeInfos.size();_feeIndex++) {
                Map businessFeeInfo = businessFeeInfos.get(_feeIndex);
                flushBusinessFeeInfo(businessFeeInfo,StatusConstant.STATUS_CD_INVALID);
                feeServiceDaoImpl.updateFeeInfoInstance(businessFeeInfo);
                dataFlowContext.addParamOut("feeId",businessFeeInfo.get("fee_id"));
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
        //费用信息
        List<Map> feeInfo = feeServiceDaoImpl.getFeeInfo(info);
        if(feeInfo != null && feeInfo.size() > 0){

            //费用信息
            List<Map> businessFeeInfos = feeServiceDaoImpl.getBusinessFeeInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessFeeInfos == null ||  businessFeeInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（fee），程序内部异常,请检查！ "+delInfo);
            }
            for (int _feeIndex = 0; _feeIndex < businessFeeInfos.size();_feeIndex++) {
                Map businessFeeInfo = businessFeeInfos.get(_feeIndex);
                flushBusinessFeeInfo(businessFeeInfo,StatusConstant.STATUS_CD_VALID);
                feeServiceDaoImpl.updateFeeInfoInstance(businessFeeInfo);
            }
        }
    }



    /**
     * 处理 businessFee 节点
     * @param business 总的数据节点
     * @param businessFee 费用节点
     */
    private void doBusinessFee(Business business,JSONObject businessFee){

        Assert.jsonObjectHaveKey(businessFee,"feeId","businessFee 节点下没有包含 feeId 节点");

        if(businessFee.getString("feeId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"feeId 错误，不能自动生成（必须已经存在的feeId）"+businessFee);
        }
        //自动插入DEL
        autoSaveDelBusinessFee(business,businessFee);
    }

    public IFeeServiceDao getFeeServiceDaoImpl() {
        return feeServiceDaoImpl;
    }

    public void setFeeServiceDaoImpl(IFeeServiceDao feeServiceDaoImpl) {
        this.feeServiceDaoImpl = feeServiceDaoImpl;
    }
}

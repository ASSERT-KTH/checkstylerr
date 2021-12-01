package com.java110.fee.listener.config;

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
import com.java110.fee.dao.IFeeConfigServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除费用配置信息 侦听
 *
 * 处理节点
 * 1、businessFeeConfig:{} 费用配置基本信息节点
 * 2、businessFeeConfigAttr:[{}] 费用配置属性信息节点
 * 3、businessFeeConfigPhoto:[{}] 费用配置照片信息节点
 * 4、businessFeeConfigCerdentials:[{}] 费用配置证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E5%88%A0%E9%99%A4%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("deleteFeeConfigInfoListener")
@Transactional
public class DeleteFeeConfigInfoListener extends AbstractFeeConfigBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(DeleteFeeConfigInfoListener.class);
    @Autowired
    IFeeConfigServiceDao feeConfigServiceDaoImpl;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_DELETE_FEE_CONFIG;
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

        //处理 businessFeeConfig 节点
        if(data.containsKey("businessFeeConfig")){
            //处理 businessFeeConfig 节点
            if(data.containsKey("businessFeeConfig")){
                Object _obj = data.get("businessFeeConfig");
                JSONArray businessFeeConfigs = null;
                if(_obj instanceof JSONObject){
                    businessFeeConfigs = new JSONArray();
                    businessFeeConfigs.add(_obj);
                }else {
                    businessFeeConfigs = (JSONArray)_obj;
                }
                //JSONObject businessFeeConfig = data.getJSONObject("businessFeeConfig");
                for (int _feeConfigIndex = 0; _feeConfigIndex < businessFeeConfigs.size();_feeConfigIndex++) {
                    JSONObject businessFeeConfig = businessFeeConfigs.getJSONObject(_feeConfigIndex);
                    doBusinessFeeConfig(business, businessFeeConfig);
                    if(_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("configId", businessFeeConfig.getString("configId"));
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

        //费用配置信息
        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_DEL);

        //费用配置信息
        List<Map> businessFeeConfigInfos = feeConfigServiceDaoImpl.getBusinessFeeConfigInfo(info);
        if( businessFeeConfigInfos != null && businessFeeConfigInfos.size() >0) {
            for (int _feeConfigIndex = 0; _feeConfigIndex < businessFeeConfigInfos.size();_feeConfigIndex++) {
                Map businessFeeConfigInfo = businessFeeConfigInfos.get(_feeConfigIndex);
                flushBusinessFeeConfigInfo(businessFeeConfigInfo,StatusConstant.STATUS_CD_INVALID);
                feeConfigServiceDaoImpl.updateFeeConfigInfoInstance(businessFeeConfigInfo);
                dataFlowContext.addParamOut("configId",businessFeeConfigInfo.get("config_id"));
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
        //费用配置信息
        List<Map> feeConfigInfo = feeConfigServiceDaoImpl.getFeeConfigInfo(info);
        if(feeConfigInfo != null && feeConfigInfo.size() > 0){

            //费用配置信息
            List<Map> businessFeeConfigInfos = feeConfigServiceDaoImpl.getBusinessFeeConfigInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessFeeConfigInfos == null ||  businessFeeConfigInfos.size() == 0){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（feeConfig），程序内部异常,请检查！ "+delInfo);
            }
            for (int _feeConfigIndex = 0; _feeConfigIndex < businessFeeConfigInfos.size();_feeConfigIndex++) {
                Map businessFeeConfigInfo = businessFeeConfigInfos.get(_feeConfigIndex);
                flushBusinessFeeConfigInfo(businessFeeConfigInfo,StatusConstant.STATUS_CD_VALID);
                feeConfigServiceDaoImpl.updateFeeConfigInfoInstance(businessFeeConfigInfo);
            }
        }
    }



    /**
     * 处理 businessFeeConfig 节点
     * @param business 总的数据节点
     * @param businessFeeConfig 费用配置节点
     */
    private void doBusinessFeeConfig(Business business,JSONObject businessFeeConfig){

        Assert.jsonObjectHaveKey(businessFeeConfig,"configId","businessFeeConfig 节点下没有包含 configId 节点");

        if(businessFeeConfig.getString("configId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"configId 错误，不能自动生成（必须已经存在的configId）"+businessFeeConfig);
        }
        //自动插入DEL
        autoSaveDelBusinessFeeConfig(business,businessFeeConfig);
    }

    public IFeeConfigServiceDao getFeeConfigServiceDaoImpl() {
        return feeConfigServiceDaoImpl;
    }

    public void setFeeConfigServiceDaoImpl(IFeeConfigServiceDao feeConfigServiceDaoImpl) {
        this.feeConfigServiceDaoImpl = feeConfigServiceDaoImpl;
    }
}

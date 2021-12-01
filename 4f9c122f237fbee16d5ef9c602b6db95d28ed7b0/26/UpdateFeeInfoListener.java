package com.java110.fee.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.smo.order.IOrderInnerServiceSMO;
import com.java110.dto.order.BusinessDto;
import com.java110.fee.dao.IFeeDetailServiceDao;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.lock.DistributedLock;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.Business;
import com.java110.fee.dao.IFeeServiceDao;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.NumberUtils;

import java.util.*;

/**
 * 修改费用信息 侦听
 * <p>
 * 处理节点
 * 1、businessFee:{} 费用基本信息节点
 * 2、businessFeeAttr:[{}] 费用属性信息节点
 * 3、businessFeePhoto:[{}] 费用照片信息节点
 * 4、businessFeeCerdentials:[{}] 费用证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateFeeInfoListener")
@Transactional
public class UpdateFeeInfoListener extends AbstractFeeBusinessServiceDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateFeeInfoListener.class);

    @Autowired
    private IOrderInnerServiceSMO orderInnerServiceSMOImpl;

    @Autowired
    private IFeeServiceDao feeServiceDaoImpl;

    @Autowired
    private IFeeDetailServiceDao feeDetailServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
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

        //处理 businessFee 节点
        if (data.containsKey("businessFee")) {
            //处理 businessFee 节点
            if (data.containsKey("businessFee")) {
                Object _obj = data.get("businessFee");
                JSONArray businessFees = null;
                if (_obj instanceof JSONObject) {
                    businessFees = new JSONArray();
                    businessFees.add(_obj);
                } else {
                    businessFees = (JSONArray) _obj;
                }
                //JSONObject businessFee = data.getJSONObject("businessFee");
                for (int _feeIndex = 0; _feeIndex < businessFees.size(); _feeIndex++) {
                    JSONObject businessFee = businessFees.getJSONObject(_feeIndex);
                    doBusinessFee(business, businessFee);
                    if (_obj instanceof JSONObject) {
                        dataFlowContext.addParamOut("feeId", businessFee.getString("feeId"));
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
        //费用信息
        List<Map> businessFeeInfos = feeServiceDaoImpl.getBusinessFeeInfo(info);

        //查询同订单 缴费记录bId
        BusinessDto businessDto = new BusinessDto();
        businessDto.setbId(business.getbId());
        businessDto.setBusinessTypeCd("610100030001");
        List<BusinessDto> businessDtos = orderInnerServiceSMOImpl.querySameOrderBusiness(businessDto);
        Assert.listOnlyOne(businessDtos, "存在多条缴费记录或没有");

        //查询费用明细过程表
        Map feeDetailInfo = new HashMap();
        feeDetailInfo.put("bId", businessDtos.get(0).getbId());
        feeDetailInfo.put("operate", "ADD");
        List<Map> feeDetails = feeDetailServiceDaoImpl.getBusinessFeeDetailInfo(feeDetailInfo);
        Assert.listOnlyOne(feeDetails, "business表中存在多条缴费记录或没有");
        String cyclesStr = feeDetails.get(0).get("cycles").toString();
        double cycles = Double.parseDouble(cyclesStr);

        Map feeMap = null;
        if (businessFeeInfos != null && businessFeeInfos.size() > 0) {
            for (int _feeIndex = 0; _feeIndex < businessFeeInfos.size(); _feeIndex++) {
                Map businessFeeInfo = businessFeeInfos.get(_feeIndex);
                //开始锁代码
                String requestId = DistributedLock.getLockUUID();
                String key = this.getClass().getSimpleName() + businessFeeInfo.get("fee_id");
                try {
                    DistributedLock.waitGetDistributedLock(key, requestId);
                    //这里考虑并发问题
                    feeMap = new HashMap();
                    feeMap.put("feeId", businessFeeInfo.get("fee_id"));
                    feeMap.put("communityId", businessFeeInfo.get("community_id"));
                    feeMap.put("statusCd", "0");
                    List<Map> feeInfo = feeServiceDaoImpl.getFeeInfo(feeMap);
                    Assert.listOnlyOne(feeInfo, "查询到多条数据或未查询到数据" + feeMap);
                    //根据当前的结束时间 修改
                    Date endTime = (Date) feeInfo.get(0).get("end_time");
                    if(cycles > 0) {
                        Calendar endCalender = Calendar.getInstance();
                        endCalender.setTime(endTime);
                        if(StringUtil.isNumber(cyclesStr)) {
                            endCalender.add(Calendar.MONTH, new Double(cycles).intValue());
                        }else{
                            int hours = new Double(cycles * DateUtil.getCurrentMonthDay() * 24).intValue();
                            endCalender.add(Calendar.HOUR, hours);
                        }
                        businessFeeInfo.put("end_time", endCalender.getTime());
                    }
                    flushBusinessFeeInfo(businessFeeInfo, StatusConstant.STATUS_CD_VALID);
                    feeServiceDaoImpl.updateFeeInfoInstance(businessFeeInfo);
                    if (businessFeeInfo.size() == 1) {
                        dataFlowContext.addParamOut("feeId", businessFeeInfo.get("fee_id"));
                    }
                } finally {
                    DistributedLock.releaseDistributedLock(requestId, key);
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
        //费用信息
        List<Map> feeInfo = feeServiceDaoImpl.getFeeInfo(info);
        if (feeInfo != null && feeInfo.size() > 0) {

            //费用信息
            List<Map> businessFeeInfos = feeServiceDaoImpl.getBusinessFeeInfo(delInfo);
            //除非程序出错了，这里不会为空
            if (businessFeeInfos == null || businessFeeInfos.size() == 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR, "撤单失败（fee），程序内部异常,请检查！ " + delInfo);
            }
            for (int _feeIndex = 0; _feeIndex < businessFeeInfos.size(); _feeIndex++) {
                Map businessFeeInfo = businessFeeInfos.get(_feeIndex);
                flushBusinessFeeInfo(businessFeeInfo, StatusConstant.STATUS_CD_VALID);
                feeServiceDaoImpl.updateFeeInfoInstance(businessFeeInfo);
            }
        }

    }


    /**
     * 处理 businessFee 节点
     *
     * @param business    总的数据节点
     * @param businessFee 费用节点
     */
    private void doBusinessFee(Business business, JSONObject businessFee) {

        Assert.jsonObjectHaveKey(businessFee, "feeId", "businessFee 节点下没有包含 feeId 节点");

        if (businessFee.getString("feeId").startsWith("-")) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "feeId 错误，不能自动生成（必须已经存在的feeId）" + businessFee);
        }
        //自动保存DEL
        autoSaveDelBusinessFee(business, businessFee);

        businessFee.put("bId", business.getbId());
        businessFee.put("operate", StatusConstant.OPERATE_ADD);
        //保存费用信息
        feeServiceDaoImpl.saveBusinessFeeInfo(businessFee);

    }


    public IFeeServiceDao getFeeServiceDaoImpl() {
        return feeServiceDaoImpl;
    }

    public void setFeeServiceDaoImpl(IFeeServiceDao feeServiceDaoImpl) {
        this.feeServiceDaoImpl = feeServiceDaoImpl;
    }


    public IOrderInnerServiceSMO getOrderInnerServiceSMOImpl() {
        return orderInnerServiceSMOImpl;
    }

    public void setOrderInnerServiceSMOImpl(IOrderInnerServiceSMO orderInnerServiceSMOImpl) {
        this.orderInnerServiceSMOImpl = orderInnerServiceSMOImpl;
    }
}

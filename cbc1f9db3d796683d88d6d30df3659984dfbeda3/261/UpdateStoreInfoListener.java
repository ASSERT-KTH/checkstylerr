package com.java110.store.listener;

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
import com.java110.store.dao.IStoreServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改商户信息 侦听
 *
 * 处理节点
 * 1、businessStore:{} 商户基本信息节点
 * 2、businessStoreAttr:[{}] 商户属性信息节点
 * 3、businessStorePhoto:[{}] 商户照片信息节点
 * 4、businessStoreCerdentials:[{}] 商户证件信息节点
 * 协议地址 ：https://github.com/java110/MicroCommunity/wiki/%E4%BF%AE%E6%94%B9%E5%95%86%E6%88%B7%E4%BF%A1%E6%81%AF-%E5%8D%8F%E8%AE%AE
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("updateStoreInfoListener")
@Transactional
public class UpdateStoreInfoListener extends AbstractStoreBusinessServiceDataFlowListener {

    private final static Logger logger = LoggerFactory.getLogger(UpdateStoreInfoListener.class);
    @Autowired
    IStoreServiceDao storeServiceDaoImpl;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_UPDATE_STORE_INFO;
    }

    /**
     * business过程
     * @param dataFlowContext
     * @param business
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessStore 节点
        if(data.containsKey("businessStore")){
            JSONObject businessStore = data.getJSONObject("businessStore");
            doBusinessStore(business,businessStore);
            dataFlowContext.addParamOut("storeId",businessStore.getString("storeId"));
        }

        if(data.containsKey("businessStoreAttr")){
            JSONArray businessStoreAttrs = data.getJSONArray("businessStoreAttr");
            doSaveBusinessStoreAttrs(business,businessStoreAttrs);
        }

        if(data.containsKey("businessStorePhoto")){
            JSONArray businessStorePhotos = data.getJSONArray("businessStorePhoto");
            doBusinessStorePhoto(business,businessStorePhotos);
        }

        if(data.containsKey("businessStoreCerdentials")){
            JSONArray businessStoreCerdentialses = data.getJSONArray("businessStoreCerdentials");
            doBusinessStoreCerdentials(business,businessStoreCerdentialses);
        }
    }


    /**
     * business to instance 过程
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {

        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //商户信息
        Map businessStoreInfo = storeServiceDaoImpl.getBusinessStoreInfo(info);
        if( businessStoreInfo != null && !businessStoreInfo.isEmpty()) {
            flushBusinessStoreInfo(businessStoreInfo,StatusConstant.STATUS_CD_VALID);
            storeServiceDaoImpl.updateStoreInfoInstance(businessStoreInfo);
            dataFlowContext.addParamOut("storeId",businessStoreInfo.get("store_id"));
        }
        //商户属性
        List<Map> businessStoreAttrs = storeServiceDaoImpl.getBusinessStoreAttrs(info);
        if(businessStoreAttrs != null && businessStoreAttrs.size() > 0) {
            for(Map businessStoreAttr : businessStoreAttrs) {
                flushBusinessStoreAttr(businessStoreAttr,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStoreAttrInstance(businessStoreAttr);
            }
        }
        //商户照片
        List<Map> businessStorePhotos = storeServiceDaoImpl.getBusinessStorePhoto(info);
        if(businessStorePhotos != null && businessStorePhotos.size() >0){
            for(Map businessStorePhoto : businessStorePhotos) {
                flushBusinessStorePhoto(businessStorePhoto,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStorePhotoInstance(businessStorePhoto);
            }
        }
        //商户证件
        List<Map> businessStoreCerdentialses = storeServiceDaoImpl.getBusinessStoreCerdentials(info);
        if(businessStoreCerdentialses != null && businessStoreCerdentialses.size()>0){
            for(Map businessStoreCerdentials : businessStoreCerdentialses) {
                flushBusinessStoreCredentials(businessStoreCerdentials,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStoreCerdentailsInstance(businessStoreCerdentials);
            }
        }
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
        Map delInfo = new HashMap();
        delInfo.put("bId",business.getbId());
        delInfo.put("operate",StatusConstant.OPERATE_DEL);
        //商户信息
        Map storeInfo = storeServiceDaoImpl.getStoreInfo(info);
        if(storeInfo != null && !storeInfo.isEmpty()){

            //商户信息
            Map businessStoreInfo = storeServiceDaoImpl.getBusinessStoreInfo(delInfo);
            //除非程序出错了，这里不会为空
            if(businessStoreInfo == null || businessStoreInfo.isEmpty()){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败（store），程序内部异常,请检查！ "+delInfo);
            }

            flushBusinessStoreInfo(businessStoreInfo,StatusConstant.STATUS_CD_VALID);
            storeServiceDaoImpl.updateStoreInfoInstance(businessStoreInfo);
            dataFlowContext.addParamOut("storeId",storeInfo.get("store_id"));
        }

        //商户属性
        List<Map> storeAttrs = storeServiceDaoImpl.getStoreAttrs(info);
        if(storeAttrs != null && storeAttrs.size()>0){

            List<Map> businessStoreAttrs = storeServiceDaoImpl.getBusinessStoreAttrs(delInfo);
            //除非程序出错了，这里不会为空
            if(businessStoreAttrs == null || businessStoreAttrs.size() ==0 ){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败(store_attr)，程序内部异常,请检查！ "+delInfo);
            }
            for(Map businessStoreAttr : businessStoreAttrs) {
                flushBusinessStoreAttr(businessStoreAttr,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStoreAttrInstance(businessStoreAttr);
            }
        }

        //商户照片
        List<Map> storePhotos = storeServiceDaoImpl.getStorePhoto(info);
        if(storePhotos != null && storePhotos.size()>0){
            List<Map> businessStorePhotos = storeServiceDaoImpl.getBusinessStorePhoto(delInfo);
            //除非程序出错了，这里不会为空
            if(businessStorePhotos == null || businessStorePhotos.size() ==0 ){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败(store_photo)，程序内部异常,请检查！ "+delInfo);
            }
            for(Map businessStorePhoto : businessStorePhotos) {
                flushBusinessStorePhoto(businessStorePhoto,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStorePhotoInstance(businessStorePhoto);
            }
        }

        //商户属性
        List<Map> storeCerdentialses = storeServiceDaoImpl.getStoreCerdentials(info);
        if(storeCerdentialses != null && storeCerdentialses.size()>0){
            List<Map> businessStoreCerdentialses = storeServiceDaoImpl.getBusinessStoreCerdentials(delInfo);
            //除非程序出错了，这里不会为空
            if(businessStoreCerdentialses == null || businessStoreCerdentialses.size() ==0 ){
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_INNER_ERROR,"撤单失败(store_cerdentials)，程序内部异常,请检查！ "+delInfo);
            }
            for(Map businessStoreCerdentials : businessStoreCerdentialses) {
                flushBusinessStoreCredentials(businessStoreCerdentials,StatusConstant.STATUS_CD_VALID);
                storeServiceDaoImpl.updateStoreCerdentailsInstance(businessStoreCerdentials);
            }
        }
    }

    /**
     * 保存商户照片
     * @param business 业务对象
     * @param businessStorePhotos 商户照片
     */
    private void doBusinessStorePhoto(Business business, JSONArray businessStorePhotos) {


        for(int businessStorePhotoIndex = 0 ;businessStorePhotoIndex < businessStorePhotos.size();businessStorePhotoIndex++) {
            JSONObject businessStorePhoto = businessStorePhotos.getJSONObject(businessStorePhotoIndex);
            Assert.jsonObjectHaveKey(businessStorePhoto, "storeId", "businessStorePhoto 节点下没有包含 storeId 节点");

            if (businessStorePhoto.getString("storePhotoId").startsWith("-")) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"storePhotoId 错误，不能自动生成（必须已经存在的storePhotoId）"+businessStorePhoto);
            }

            //自动保存DEL信息
            autoSaveDelBusinessStorePhoto(business,businessStorePhoto);

            businessStorePhoto.put("bId", business.getbId());
            businessStorePhoto.put("operate", StatusConstant.OPERATE_ADD);
            //保存商户信息
            storeServiceDaoImpl.saveBusinessStorePhoto(businessStorePhoto);
        }
    }

    /**
     * 处理 businessStore 节点
     * @param business 总的数据节点
     * @param businessStore 商户节点
     */
    private void doBusinessStore(Business business,JSONObject businessStore){

        Assert.jsonObjectHaveKey(businessStore,"storeId","businessStore 节点下没有包含 storeId 节点");

        if(businessStore.getString("storeId").startsWith("-")){
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"storeId 错误，不能自动生成（必须已经存在的storeId）"+businessStore);
        }
        //自动保存DEL
        autoSaveDelBusinessStore(business,businessStore);

        businessStore.put("bId",business.getbId());
        businessStore.put("operate", StatusConstant.OPERATE_ADD);
        //保存商户信息
        storeServiceDaoImpl.saveBusinessStoreInfo(businessStore);

    }



    /**
     * 保存商户属性信息
     * @param business 当前业务
     * @param businessStoreAttrs 商户属性
     */
    private void doSaveBusinessStoreAttrs(Business business,JSONArray businessStoreAttrs){
        JSONObject data = business.getDatas();


        for(int storeAttrIndex = 0 ; storeAttrIndex < businessStoreAttrs.size();storeAttrIndex ++){
            JSONObject storeAttr = businessStoreAttrs.getJSONObject(storeAttrIndex);
            Assert.jsonObjectHaveKey(storeAttr,"attrId","businessStoreAttr 节点下没有包含 attrId 节点");
            if(storeAttr.getString("attrId").startsWith("-")){
                throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"attrId 错误，不能自动生成（必须已经存在的attrId）"+storeAttr);
            }
            //自动保存DEL数据
            autoSaveDelBusinessStoreAttr(business,storeAttr);

            storeAttr.put("bId",business.getbId());
            storeAttr.put("storeId",storeAttr.getString("storeId"));
            storeAttr.put("operate", StatusConstant.OPERATE_ADD);

            storeServiceDaoImpl.saveBusinessStoreAttr(storeAttr);
        }
    }


    /**
     * 保存 商户证件 信息
     * @param business 当前业务
     * @param businessStoreCerdentialses 商户证件
     */
    private void doBusinessStoreCerdentials(Business business, JSONArray businessStoreCerdentialses) {

        for(int businessStoreCerdentialsIndex = 0 ; businessStoreCerdentialsIndex < businessStoreCerdentialses.size() ; businessStoreCerdentialsIndex ++) {
            JSONObject businessStoreCerdentials = businessStoreCerdentialses.getJSONObject(businessStoreCerdentialsIndex);
            Assert.jsonObjectHaveKey(businessStoreCerdentials, "storeId", "businessStorePhoto 节点下没有包含 storeId 节点");

            if (businessStoreCerdentials.getString("storeCerdentialsId").startsWith("-")) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR,"storePhotoId 错误，不能自动生成（必须已经存在的storePhotoId）"+businessStoreCerdentials);
            }

            autoSaveDelBusinessStoreCerdentials(business,businessStoreCerdentials);

            businessStoreCerdentials.put("bId", business.getbId());
            businessStoreCerdentials.put("operate", StatusConstant.OPERATE_ADD);
            //保存商户信息
            storeServiceDaoImpl.saveBusinessStoreCerdentials(businessStoreCerdentials);
        }
    }

    public IStoreServiceDao getStoreServiceDaoImpl() {
        return storeServiceDaoImpl;
    }

    public void setStoreServiceDaoImpl(IStoreServiceDao storeServiceDaoImpl) {
        this.storeServiceDaoImpl = storeServiceDaoImpl;
    }



}

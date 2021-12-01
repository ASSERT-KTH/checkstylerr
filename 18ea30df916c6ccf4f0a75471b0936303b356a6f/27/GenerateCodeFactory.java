package com.java110.core.factory;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.MappingConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.GenerateCodeException;
import com.java110.utils.exception.ResponseErrorException;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.core.smo.code.ICodeApi;
import org.springframework.web.client.RestTemplate;

import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生成序列工具类
 * Created by wuxw on 2017/2/27.
 */
public class GenerateCodeFactory {

    private static final long ONE_STEP = 1000000;
    private static final Lock LOCK = new ReentrantLock();
    private static short lastCount = 1;
    private static int count = 0;
    private static final String first = "10";
    //10+yyyymmdd+八位序列
    public static final String CODE_PREFIX_oId = "10";
    public static final String CODE_PREFIX_bId = "20";
    public static final String CODE_PREFIX_attrId = "11";
    public static final String CODE_PREFIX_transactionId = "1000001";
    public static final String CODE_PREFIX_pageTransactionId = "1000002";
    public static final String CODE_PREFIX_dataFlowId = "2000";
    public static final String CODE_PREFIX_userId = "30";
    public static final String CODE_PREFIX_storeId = "40";
    public static final String CODE_PREFIX_storePhotoId = "41";
    public static final String CODE_PREFIX_storeCerdentialsId = "42";
    public static final String CODE_PREFIX_memberStoreId = "43";
    public static final String CODE_PREFIX_propertyStoreId = "44";
    public static final String CODE_PREFIX_storeUserId = "45";
    public static final String CODE_PREFIX_shopId = "50";
    public static final String CODE_PREFIX_shopAttrId = "51";
    public static final String CODE_PREFIX_shopPhotoId = "52";
    public static final String CODE_PREFIX_shopAttrParamId = "53";
    public static final String CODE_PREFIX_shopPreferentialId = "54";
    public static final String CODE_PREFIX_shopDescId = "55";
    public static final String CODE_PREFIX_shopCatalogId = "56";
    public static final String CODE_PREFIX_buyId = "57";
    public static final String CODE_PREFIX_buyAttrId = "58";
    public static final String CODE_PREFIX_commentId = "60";
    public static final String CODE_PREFIX_subCommentId = "61";
    public static final String CODE_PREFIX_subCommentAttrId = "62";
    public static final String CODE_PREFIX_commentPhotoId = "63";
    public static final String CODE_PREFIX_commentScoreId = "64";
    public static final String CODE_PREFIX_communityId = "70";
    public static final String CODE_PREFIX_communityPhotoId = "71";
    public static final String CODE_PREFIX_communityMemberId = "72";
    public static final String CODE_PREFIX_feeId = "90";
    public static final String CODE_PREFIX_detailId = "91";
    public static final String CODE_PREFIX_configId = "92";
    public static final String CODE_PREFIX_propertyUserId = "93";
    public static final String CODE_PREFIX_propertyFeeId = "94";
    public static final String CODE_PREFIX_houseId = "95";
    public static final String CODE_PREFIX_pgId = "600";

    public static final String CODE_PREFIX_floorId = "73";
    public static final String CODE_PREFIX_unitId = "74";
    public static final String CODE_PREFIX_roomId = "75";
    public static final String CODE_PREFIX_roomAttrId = "76";
    public static final String CODE_PREFIX_ownerId = "77";
    public static final String CODE_PREFIX_ownerRoomRelId = "78";
    public static final String CODE_PREFIX_psId = "79";
    public static final String CODE_PREFIX_carId = "80";
    //测试用列
    public static final String CODE_PREFIX_demoId = "90";
    public static final String CODE_PREFIX_noticeId = "96";

    public static final String CODE_PREFIX_HCJOBId = "96";
    //BUSINESSTYPE
    public static final String CODE_PREFIX_id = "99";
    public static final String CODE_PREFIX_service_id = "98";

    public static final String MENU_GROUP = "80";
    public static final String MENU = "70";
    public static final String BASE_PRIVILEGE = "50";
    public static final String CODE_PREFIX_vId = "11";
    public static final String CODE_PREFIX_file_id = "81";
    public static final String CODE_PREFIX_repairId = "82";
    public static final String CODE_PREFIX_ruId = "83";
    public static final String CODE_PREFIX_orgId = "84";
    public static final String CODE_PREFIX_relId = "84";
    public static final String CODE_PREFIX_resId = "85";
    public static final String CODE_PREFIX_auditUserId = "86";
    public static final String CODE_PREFIX_auditMessageId = "87";
    public static final String CODE_PREFIX_complaintId = "88";
    public static final String CODE_PREFIX_machineId = "89";
    public static final String CODE_PREFIX_machineTranslateId = "90";
    public static final String CODE_PREFIX_fileRelId = "91";
    public static final String CODE_PREFIX_machineRecordId = "92";
    public static final String CODE_PREFIX_applicationKeyId = "93";
    public static final String CODE_PREFIX_msgId = "94";
    public static final String CODE_PREFIX_msgReadId = "95";
    public static final String CODE_PREFIX_advertId = "96";
    public static final String CODE_PREFIX_advertItemId = "97";
    public static final String CODE_PREFIX_appUserId = "98";
    public static final String CODE_PREFIX_activitiesId = "99";
    public static final String CODE_PREFIX_paId = "10";
    public static final String CODE_PREFIX_bwId = "11";
    public static final String CODE_PREFIX_inoutId = "12";
    public static final String CODE_PREFIX_inspectionId = "13";
    public static final String CODE_PREFIX_inspectionRouteId = "50";



    /**
     * 只有在不调用服务生成ID时有用
     */
    private static Map<String, String> prefixMap = null;

    static {
        prefixMap = new HashMap<String, String>();
        //10+yyyymmdd+八位序列
        prefixMap.put("oId", "10");
        //（20+yyyymmdd+八位序列）
        prefixMap.put("bId", "20");
        //（11+yyyymmdd+八位序列）
        prefixMap.put("attrId", "11");
        prefixMap.put("transactionId", "1000001");
        prefixMap.put("pageTransactionId", "1000002");
        prefixMap.put("dataFlowId", "2000");
        prefixMap.put("userId", "30");
        prefixMap.put("storeId", "40");
        prefixMap.put("storePhotoId", "41");
        prefixMap.put("storeCerdentialsId", "42");
        prefixMap.put("memberStoreId", "43");
        prefixMap.put("propertyStoreId", "44");
        prefixMap.put("storeUserId", "45");
        prefixMap.put("shopId", "50");
        prefixMap.put("shopAttrId", "51");
        prefixMap.put("shopPhotoId", "52");
        prefixMap.put("shopAttrParamId", "53");
        prefixMap.put("shopPreferentialId", "54");
        prefixMap.put("shopDescId", "55");
        prefixMap.put("shopCatalogId", "56");
        prefixMap.put("buyId", "57");
        prefixMap.put("buyAttrId", "58");
        prefixMap.put("commentId", "60");
        prefixMap.put("subCommentId", "61");
        prefixMap.put("subCommentAttrId", "62");
        prefixMap.put("commentPhotoId", "63");
        prefixMap.put("commentScoreId", "64");
        prefixMap.put("communityId", "70");
        prefixMap.put("communityPhotoId", "71");
        prefixMap.put("communityMemberId", "72");
        prefixMap.put("agentId", "80");
        prefixMap.put("agentPhotoId", "81");
        prefixMap.put("agentCerdentialsId", "82");
        prefixMap.put("agentUserId", "83");
        prefixMap.put("propertyId", "90");
        prefixMap.put("propertyPhotoId", "91");
        prefixMap.put("propertyCerdentialsId", "92");
        prefixMap.put("propertyUserId", "93");
        prefixMap.put("propertyFeeId", "94");
        prefixMap.put("houseId", "95");
        prefixMap.put("pgId", "600");
    }

    private static String PLATFORM_CODE = "0001";

    @SuppressWarnings("finally")
    public static String nextId(String idLength) {
        LOCK.lock();
        try {
            if (lastCount == ONE_STEP) {
                lastCount = 1;
            }
            count = lastCount++;
        } finally {
            LOCK.unlock();
            return getRandom() + String.format(idLength, count);
        }
    }

    public static String nextId() {
        return nextId("%04d");
    }

    /**
     * 获取交易流水ID
     *
     * @return
     */
    public static String getTransactionId() {

        //从内存中获取平台随机码

        return prefixMap.get("transactionId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId();
    }

    /**
     * 获取内部平台 交易流水
     *
     * @return
     * @throws NoSuchObjectException
     */
    public static String getInnerTransactionId() throws Exception {
        return codeApi().generateCode(prefixMap.get("transactionId"));
    }

    /**
     * 获取交易流水ID
     *
     * @return
     */
    public static String getPageTransactionId() {

        //从内存中获取平台随机码

        return prefixMap.get("pageTransactionId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId();
    }

    /**
     * pgId生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getGeneratorId(String prefix) throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefix + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefix);
    }

    public static String getOId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("oId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        return getCode(prefixMap.get("oId"));
    }


    /**
     * 查询Code
     *
     * @param prefix
     * @return
     * @throws GenerateCodeException
     */
    private static String getCode(String prefix) throws GenerateCodeException {
        //调用服务
        String code = "-1";
        try {
            String responseMessage = restTemplate().postForObject(MappingCache.getValue(MappingConstant.KEY_CODE_PATH),
                    createCodeRequestJson(getTransactionId(), prefix, prefix).toJSONString(), String.class);

            if (ResponseConstant.RESULT_CODE_ERROR.equals(responseMessage)) {
                throw new ResponseErrorException(ResponseConstant.RESULT_CODE_ERROR, "生成oId编码失败");
            }
            Assert.jsonObjectHaveKey(responseMessage, "code", "编码生成系统 返回报文错误" + responseMessage);

            JSONObject resJson = JSONObject.parseObject(responseMessage);

            if (!ResponseConstant.RESULT_CODE_SUCCESS.equals(resJson.getString("code"))) {
                throw new ResponseErrorException(resJson.getString("code"), "生成oId编码失败 "
                        + resJson.getString("message"));
            }
            code = resJson.getString("id");
        } catch (Exception e) {
            throw new GenerateCodeException(ResponseConstant.RESULT_CODE_ERROR, e.getMessage());
        } finally {
            return code;
        }

    }

    public static String getBId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("bId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("bId"));
    }

    public static String getAttrId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("attrId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("attrId"));
    }

    /**
     * 生成dataFlowId
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getDataFlowId() throws GenerateCodeException {

        return UUID.randomUUID().toString().replace("-", "").toLowerCase();

    }

    public static String getUserId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("userId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("userId"));
    }


    public static String getStoreId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("storeId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("storeId"));
    }

    public static String getMemberStoreId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("memberStoreId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("memberStoreId"));
    }


    public static String getStorePhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("storePhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("storePhotoId"));
    }

    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getStoreCerdentialsId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("storeCerdentialsId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("storeCerdentialsId"));
    }


    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getStoreUserId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("storeUserId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("storeUserId"));
    }


    /**
     * 获取小区ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommunityId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("communityId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("communityId"));
    }


    /**
     * 获取小区照片ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommunityPhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("communityPhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("communityPhotoId"));
    }

    /**
     * 生成小区成员ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommunityMemberId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("communityMemberId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("communityMemberId"));
    }

    /**
     * 获取小区ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getAgentId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("agentId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("agentId"));
    }


    /**
     * 获取小区照片ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getAgentPhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("agentPhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("agentPhotoId"));
    }

    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getAgentCerdentialsId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("agentCerdentialsId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("agentCerdentialsId"));
    }


    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getAgentUserId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("agentUserId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("agentUserId"));
    }


    /**
     * 获取小区ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getPropertyId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("propertyId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("propertyId"));
    }


    /**
     * 获取小区照片ID
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getPropertyPhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("propertyPhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("propertyPhotoId"));
    }

    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getPropertyCerdentialsId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("propertyCerdentialsId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("propertyCerdentialsId"));
    }


    /**
     * @return
     * @throws GenerateCodeException
     */
    public static String getPropertyUserId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("propertyUserId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("propertyUserId"));
    }

    /**
     * 物业费用ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getPropertyFeeId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("propertyFeeId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H + nextId("%04d"));
        }

        return getCode(prefixMap.get("propertyFeeId"));
    }

    /**
     * 住户ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getHouseId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("houseId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H + nextId("%04d"));
        }

        return getCode(prefixMap.get("houseId"));
    }


    /**
     * 商品ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopId"));
    }

    /**
     * 商品属性ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopAttrId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopAttrId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopAttrId"));
    }

    /**
     * 商品优惠ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopPreferentialId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopPreferentialId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopPreferentialId"));
    }


    /**
     * 商品属性参数ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopAttrParamId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopAttrParamId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopAttrParamId"));
    }

    /**
     * 商品属性参数ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommentPhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("commentPhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("commentPhotoId"));
    }


    /**
     * 商品属性ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopPhotoId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopPhotoId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopPhotoId"));
    }

    /**
     * 商品描述ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopDescId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopDescId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopDescId"));
    }

    /**
     * 商品目录ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopCatalogId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("shopCatalogId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("shopCatalogId"));
    }

    /**
     * 商品buyID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopBuyId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("buyId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("buyId"));
    }

    /**
     * 商品buyAttrID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getShopBuyAttrId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("buyAttrId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("buyAttrId"));
    }

    /**
     * 评论ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommentId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("commentId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("commentId"));
    }

    /**
     * 评论ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getSubCommentId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("subCommentId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("subCommentId"));
    }

    /**
     * 评论ID生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getSubCommentAttrId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("subCommentAttrId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("subCommentAttrId"));
    }

    /**
     * commentScoreId生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getCommentScoreId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("commentScoreId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("commentScoreId"));
    }

    /**
     * pgId生成
     *
     * @return
     * @throws GenerateCodeException
     */
    public static String getPgId() throws GenerateCodeException {
        if (!MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_NEED_INVOKE_GENERATE_ID))) {
            return prefixMap.get("pgId") + DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_H) + nextId("%04d");
        }
        //调用服务
        return getCode(prefixMap.get("pgId"));
    }


    /**
     * 获取restTemplate
     *
     * @return
     * @throws NoSuchObjectException
     */
    private static RestTemplate restTemplate() throws NoSuchObjectException {

        Object bean = ApplicationContextFactory.getBean("restTemplate");

        if (bean == null) {
            throw new NoSuchObjectException("没有找到restTemplate对象，请核实");
        }

        return (RestTemplate) bean;
    }

    /**
     * 获取codeApi
     *
     * @return
     * @throws NoSuchObjectException
     */
    private static ICodeApi codeApi() throws NoSuchObjectException {

        Object bean = ApplicationContextFactory.getBean(ICodeApi.class.getName());

        if (bean == null) {
            throw new NoSuchObjectException("codeApi，请核实");
        }

        return (ICodeApi) bean;
    }


    /**
     * ID生成请求报文
     *
     * @param transactionId
     * @return
     */
    private static JSONObject createCodeRequestJson(String transactionId, String prefix, String name) {
        JSONObject paramOut = JSONObject.parseObject("{}");
        paramOut.put("transactionId", transactionId);
        paramOut.put("prefix", prefix);
        paramOut.put("name", name);
        paramOut.put("requestTime", DateUtil.getNowDefault());
        return paramOut;
    }

    /**
     * 获取随机数
     *
     * @return
     */
    private static String getRandom() {
        Random random = new Random();
        String result = "";
        for (int i = 0; i < 4; i++) {
            result += random.nextInt(10);
        }
        return result;
    }
}

package com.fintech.scf.common.enums;


import java.util.Arrays;
import java.util.List;

/**
 * @Desc: 推广位类型
 * @Date:  2018/6/1 上午10:09.
 */

public interface   EnumExample {

     enum EnumItem   implements EnumExample {

        今日推送(1,"今日推送"),
        首页推荐(2,"首页推荐"),
        人气排行(3,"人气排行"),
        品牌馆(4,"品牌馆"),
        夜市疯抢(5,"夜市疯抢"),
        _99包邮(6,"_99包邮"),
        新人免单(7,"新人免单"),
        商城搜索(8,"商城搜索"),
        文章扫码(9,"文章扫码"),


        未知(99,"未知")
        ;

        public Integer status;
        public String des;

        EnumItem(Integer status, String des) {
            this.status = status;
            this.des = des;
        }

        public Integer getStatus() {
            return status;
        }
        public String getDes() {
            return des;
        }

        public EnumItem value(String enumName){
            return valueOf( enumName ) ;
        }

        public static boolean checkExist( Integer status){
            for (EnumItem value : EnumItem.values()){
                if (value.status == status){
                    return true;
                }
            }
            return false;
        }


        public static EnumItem getStatus(Integer status){
            for (EnumItem value : EnumItem.values()){
                if (value.status == status){
                    return value;
                }
            }
            return EnumItem.未知;
        }

        public static String getDes(Integer status){
            for (EnumItem value : EnumItem.values()){
                if (value.status == status){
                    return value.des;
                }
            }
            return EnumItem.未知.des;
        }

        public static List<EnumItem> getTypeList(){
            return Arrays.asList(values());
        }

    }


    enum FDDInterface implements FDDEnum {

        FDD_PER_CA_RG("FDD_PER_CA_RG", "个人申请CA证书"),
        FDD_UP_COT_DOC("FDD_UP_COT_DOC", "合同文档上传"),
        FDD_MANUAL_SIGN("FDD_MANUAL_SIGN", "手动签章"),
        FDD_AUTO_SIGN("FDD_AUTO_SIGN", "自动签章"),
        FDD_QUERY_SIGN_REST("FDD_QUERY_SIGN_REST", "查询签章结果"),
        FDD_DOWNLOAD_COT("FDD_DOWNLOAD_COT", "下载合同");


        private String code;
        private String desc;

        FDDInterface(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public static FDDInterface toEnum(String code) {
            for (FDDInterface item : FDDInterface.values()) {
                if (item.getCode().equals(code)) {
                    return item;
                }
            }

            return null;
        }
    }


}

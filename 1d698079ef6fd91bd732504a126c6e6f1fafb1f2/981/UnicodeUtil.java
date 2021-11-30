package com.duodian.youhui.admin.utils;

/**
 * @Desc: 订单接口的时候 9月21日 出现返回接口为Unicode编码的情况，我开始添加的
 *
{"msg":"\u64cd\u4f5c\u6210\u529f","error":0,"data":null}
{"msg":"操作成功","error":0,"data":null}

 * @Author HealerJean
 * @Date 2018/9/21  下午7:19.
 */
public class UnicodeUtil {

    public static void main(String[] args) {
        String cn = "全部中文";
        System.out.println(cnToUnicode(cn));
        String unicode = "\\u5168\\u90e8\\u4e2d\\u6587";
        System.out.println(unicodeToCn(unicode));

        String someUnicode = "{\"msg\":\"\\u64cd\\u4f5c\\u6210\\u529f\",\"error\":0,\"data\":[{\"adzone_id\":\"1782066627\",\"adzone_name\":\"\\u85cf\\u5b9d\\u9601\",\"alipay_total_price\":\"18.0000\",\"auction_category\":\"\\u7f8e\\u5986-\\u7f8e\\u5bb9\\u62a4\\u80a4\",\"commission\":\"0.00\",\"commission_rate\":\"1.0000\",\"create_time\":\"2018-09-21 19:31:47\",\"income_rate\":\"0.3100\",\"item_num\":1,\"item_title\":\"\\u7f8e\\u533b\\u751f\\u9664\\u87a8\\u8ff7\\u8fed\\u9999\\u6d17\\u9762\\u5976\\u5973\\u7537\\u5b66\\u751f\\u63a7\\u6cb9\\u75d8\\u75d8\\u6e05\\u6d01\\u53bb\\u87a8\\u9ed1\\u5934\\u7c89\\u523a\\u6d01\\u9762\\u4e73\",\"num_iid\":532235400390,\"order_type\":\"\\u6dd8\\u5b9d\",\"pay_price\":\"0.00\",\"price\":\"69.00\",\"pub_share_pre_fee\":\"5.58\",\"seller_nick\":\"\\u5e7f\\u5dde\\u7f8e\\u533b\\u751f\\u751f\\u7269\\u79d1\\u6280\",\"seller_shop_title\":\"\\u7f8e\\u533b\\u751f\\u5b98\\u65b9\\u4f01\\u4e1a\\u5e97\",\"site_id\":\"46766627\",\"site_name\":\"\\u5c0f\\u5f53\\u4f18\\u60e0\\u5238\",\"subsidy_fee\":\"0\",\"subsidy_rate\":\"0.0000\",\"subsidy_type\":\"-1\",\"terminal_type\":\"2\",\"tk3rd_type\":\"--\",\"tk_status\":12,\"total_commission_fee\":\"0\",\"total_commission_rate\":\"0.3100\",\"trade_id\":224003948316112915,\"trade_parent_id\":224003948316112915}]}";
        System.out.println("一部分英文，一部分 unicode编码"+decode(someUnicode));

        String normal = "{\"msg\":\"操作成功\",\"error\":0,\"data\":[{\"adzone_id\":\"1782066627\",\"adzone_name\":\"藏宝阁\",\"alipay_total_price\":\"18.0000\",\"auction_category\":\"美妆-美容护肤\",\"commission\":\"0.00\",\"commission_rate\":\"1.0000\",\"create_time\":\"2018-09-21 19:31:47\",\"income_rate\":\"0.3100\",\"item_num\":1,\"item_title\":\"美医生除螨迷迭香洗面奶女男学生控油痘痘清洁去螨黑头粉刺洁面乳\",\"num_iid\":532235400390,\"order_type\":\"淘宝\",\"pay_price\":\"0.00\",\"price\":\"69.00\",\"pub_share_pre_fee\":\"5.58\",\"seller_nick\":\"广州美医生生物科技\",\"seller_shop_title\":\"美医生官方企业店\",\"site_id\":\"46766627\",\"site_name\":\"小当优惠券\",\"subsidy_fee\":\"0\",\"subsidy_rate\":\"0.0000\",\"subsidy_type\":\"-1\",\"terminal_type\":\"2\",\"tk3rd_type\":\"--\",\"tk_status\":12,\"total_commission_fee\":\"0\",\"total_commission_rate\":\"0.3100\",\"trade_id\":224003948316112915,\"trade_parent_id\":224003948316112915}]}";
        System.out.println("一部分英文，一部分 中文编码"+decode(normal));




    }


    /**
     * 不管是中文还是unicode编码，不管里面夹杂着什么都输出 正确的中文
     * @param unicodeStr
     * @return
     */
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5)
                        && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
                        .charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(
                                unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }




    /**
     * 中文，必须是中文
     * unicode 转 中文，只能转化全部都是unicode编码的
     * @param unicode
     * @return
     */
    public  static String unicodeToCn(String unicode) {
        /** 以 \ u 分割，因为java注释也能识别unicode，因此中间加了一个空格*/
        String[] strs = unicode.split("\\\\u");
        String returnStr = "";
        // 由于unicode字符串以 \ u 开头，因此分割出的第一个字符是""。
        for (int i = 1; i < strs.length; i++) {
            returnStr += (char) Integer.valueOf(strs[i], 16).intValue();
        }
        return returnStr;
    }

    /**
     * 必须是中文
     * 中文转Unicode
     * @param cn
     * @return
     */
    public static String cnToUnicode(String cn) {
        char[] chars = cn.toCharArray();
        String returnStr = "";
        for (int i = 0; i < chars.length; i++) {
            returnStr += "\\u" + Integer.toString(chars[i], 16);
        }
        return returnStr;
    }



}

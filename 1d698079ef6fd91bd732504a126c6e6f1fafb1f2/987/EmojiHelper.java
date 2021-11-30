package com.duodian.youhui.admin.utils;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;

/**
 * @Desc: emoji工具类
 * @Date:  2018/9/12 下午3:06.
 */

public class EmojiHelper {

    /**
     * 检测是否有emoji字符
     * @param source
     * @return 一旦含有就抛出
     */
    public static String processRemoveEmoji(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        return EmojiParser.removeAllEmojis(source);
    }


    /**
     * 将emoji字符进行处理成数据库可以观察的字符串，而不是乱码
     * @param source
     * @return
     */
    public static String parseToAliases(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        return EmojiParser.parseToAliases(source);
    }




}

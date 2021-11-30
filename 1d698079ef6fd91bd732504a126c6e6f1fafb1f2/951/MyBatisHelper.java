//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.duodian.admore.dao.helper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public class MyBatisHelper {
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_LIMIT = "limit";

    public MyBatisHelper() {
    }

    public static Map<String, Object> mergeParameterMap(Object... parameter) {
        if (parameter.length % 2 != 0) {
            throw new IllegalArgumentException("parameter须为key-value对应参数");
        } else {
            Map<String, Object> map = new HashMap();

            for(int i = 0; i < parameter.length; i += 2) {
                map.put(parameter[i].toString(), parameter[i + 1]);
            }

            return map;
        }
    }

    public static Map<String, Object> mergeParameterMap(Pageable pageable, Object... parameter) {
        if (parameter.length % 2 != 0) {
            throw new IllegalArgumentException("parameter须为key-value对应参数");
        } else {
            Map<String, Object> map = new HashMap();
            map.put("offset", pageable.getOffset());
            map.put("limit", pageable.getPageSize());

            for(int i = 0; i < parameter.length; i += 2) {
                map.put(parameter[i].toString(), parameter[i + 1]);
            }

            return map;
        }
    }
}

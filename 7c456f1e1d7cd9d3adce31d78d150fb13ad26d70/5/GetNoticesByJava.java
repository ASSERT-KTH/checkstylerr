package com.java110.service.develop.notice;

import com.alibaba.fastjson.JSONObject;
import com.java110.service.context.DataQuery;
import com.java110.service.develop.IDevelop;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName GetNoticesByJava
 * @Description TODO
 * @Author wuxw
 * @Date 2019/9/8 14:31
 * @Version 1.0
 * add by wuxw 2019/9/8
 **/
public class GetNoticesByJava implements IDevelop {
    @Override
    public JSONObject execute(DataQuery dataQuery) {
        JSONObject params = dataQuery.getRequestParams();
        List sqlParams = new ArrayList();

        String sql = "SELECT nn.`notice_id` noticeId," +
                "nn.`title`," +
                "nn.`notice_type_cd` noticeTypeCd," +
                "nn.`context`," +
                "nn.`community_id` communityId," +
                "nn.`user_id` userId," +
                "nn.`start_time` startTime " +
                "FROM n_notice nn\n" +
                "WHERE nn.status_cd = '0'" ;
        if(params.containsKey("noticeId") && !StringUtils.isEmpty(params.getString("noticeId"))){
            sql += "and nn.`notice_id` = ? ";
            sqlParams.add(params.get("noticeId"));
        }
        if(params.containsKey("communityId") && !StringUtils.isEmpty(params.getString("communityId"))){
            sql += "and nn.`community_id` = ? ";
            sqlParams.add(params.get("communityId"));
        }
        if(params.containsKey("userId") && !StringUtils.isEmpty(params.getString("userId"))){
            sql += "and nn.`user_id` = ? ";
            sqlParams.add(params.get("userId"));
        }
        if(params.containsKey("title") && !StringUtils.isEmpty(params.getString("title"))){
            sql += "and nn.`notice_id` LIKE CONCAT('%',?,'%') ";
            sqlParams.add(params.get("title"));
        }

        List outParam = dataQuery.queryDataBySql(sql,sqlParams);

        JSONObject outObj = new JSONObject();
        outObj.put("notices", outParam);

        return outObj;
    }
}

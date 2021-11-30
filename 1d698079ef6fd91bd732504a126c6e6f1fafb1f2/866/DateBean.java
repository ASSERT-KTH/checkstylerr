package com.hlj.proj.dto;

import com.hlj.proj.utils.JsonUtils;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@ApiModel(description = "json测试实体")
@ToString
public class DateBean {


        private String name ;
        private Date date ;


    public static void main(String[] args) {
        DateBean dateBean = new DateBean();
        dateBean.setDate(new Date());
        dateBean.setName("HealerJean");
        System.out.println(JsonUtils.toJsonString(dateBean));
    }


}

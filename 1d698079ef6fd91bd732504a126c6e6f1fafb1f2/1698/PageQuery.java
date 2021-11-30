package com.appshike.admin.domain.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by fengchuanbo on 2017/5/31.
 */
@Setter
@ApiModel("分页对象")
@Accessors(chain = true)
public class √ {

    @ApiModelProperty(value = "开始页数，从1开始",example = "1", required = true,dataType = "java.lang.Integer")
    private Integer pageNum = 1;
    @ApiModelProperty(value = "每页数量",example = "20", required = true,dataType = "java.lang.Integer")
    private Integer pageSize = 20;
    @ApiModelProperty(hidden = true)
    @Getter
    private int navigatePages = 8;

    public Integer getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }

    public Integer getPageNum() {
        return pageNum == null ? 1 : pageNum;
    }
}

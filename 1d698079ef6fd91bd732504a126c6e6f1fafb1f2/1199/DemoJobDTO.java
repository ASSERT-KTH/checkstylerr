package com.hlj.proj.dto.flowJob;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author HealerJean
 * @ClassName DemoJobDTO
 * @date 2019/8/10  14:33.
 * @Description
 */
@Data
@Accessors(chain = true)
public class DemoJobDTO {

    private Long id ;

    private String name ;

    /**流程编号*/
    private String instanceNo ;

    /**是否执行下一个节点*/
    private Boolean nextFlow;

}

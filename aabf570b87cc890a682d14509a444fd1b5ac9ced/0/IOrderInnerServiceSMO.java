package com.java110.core.smo.order;

import com.java110.core.feign.FeignConfiguration;
import com.java110.dto.order.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @ClassName IOrderInnerServiceSMO
 * @Description 组织接口类
 * @Author wuxw
 * @Date 2019/4/24 9:04
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
/*@FeignClient(name = "order-service", configuration = {FeignConfiguration.class})
@RequestMapping("/orderApi")*/
public interface IOrderInnerServiceSMO {

    /**
     * <p>查询小区楼信息</p>
     *
     * @param orderDto 数据对象分享
     * @return OrderDto 对象数据
     */
    @RequestMapping(value = "/queryOrders", method = RequestMethod.POST)
    List<OrderDto> queryOrders(@RequestBody OrderDto orderDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param orderDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryOrdersCount", method = RequestMethod.POST)
    int queryOrdersCount(@RequestBody OrderDto orderDto);


    /**
     * <p>查询上级组织信息</p>
     *
     * @param orderDto 数据对象分享
     * @return OrderDto 对象数据
     */
    @RequestMapping(value = "/queryOwenrOrders", method = RequestMethod.POST)
    List<OrderDto> queryOwenrOrders(@RequestBody OrderDto orderDto);
}

package com.hlj.proj.utils;

import com.hlj.proj.common.page.PageDTO;
import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.common.result.PageListResult;
import com.hlj.proj.data.pojo.demo.DemoEntity;
import com.hlj.proj.data.pojo.demo.DemoEntityQuery;
import com.hlj.proj.dto.Demo.DemoDTO;

import java.util.List;


/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName BeanUtils
 * @date 2019/6/13  20:08.
 * @Description
 */
public class BeanUtils {


    public static <T> PageDTO<T> toPageDTO(PageListResult pageView, List<T> datas) {
        if (pageView == null && (datas == null || datas.isEmpty())) {
            return null;
        } else if (pageView == null) {
            return new PageDTO<>(datas);
        } else if (pageView.getPagenation() == null) {
            return new PageDTO<>(datas);
        } else {
            Pagenation pagenation = pageView.getPagenation();
            return new PageDTO(pagenation.getPageNo(), pagenation.getPageSize(), pagenation.getItemCount(),
                    pagenation.getPageCount(), datas);
        }
    }




    public static DemoEntity dtoToDemo(DemoDTO demoDTO){
        DemoEntity result = new DemoEntity();
        result.setId(demoDTO.getId());
        result.setName(demoDTO.getName());
        result.setPhone(demoDTO.getPhone());
        result.setEmail(demoDTO.getEmail());
        result.setAge(demoDTO.getAge());
        result.setDelFlag(demoDTO.getDelFlag());
        result.setCreateUser(demoDTO.getCreateUser());
        result.setCreateName(demoDTO.getCreateName());
        result.setCreateTime(demoDTO.getCreateTime());
        result.setUpdateUser(demoDTO.getUpdateUser());
        result.setUpdateName(demoDTO.getUpdateName());
        result.setUpdateTime(demoDTO.getUpdateTime());
        return result ;
    }

    public static DemoDTO demoToDTO(DemoEntity demoEntity){
        DemoDTO dto = new DemoDTO() ;
        if(demoEntity !=null){
            dto.setId(demoEntity.getId());
            dto.setName(demoEntity.getName());
            dto.setAge(demoEntity.getAge());
            dto.setPhone(demoEntity.getPhone());
            dto.setEmail(demoEntity.getEmail());
            dto.setDelFlag(demoEntity.getDelFlag());
            dto.setCreateUser(demoEntity.getCreateUser());
            dto.setCreateName(demoEntity.getCreateName());
            dto.setCreateTime(demoEntity.getCreateTime());
            dto.setUpdateUser(demoEntity.getUpdateUser());
            dto.setUpdateName(demoEntity.getUpdateName());
            dto.setUpdateTime(demoEntity.getUpdateTime());
            dto.setPage(null);
            dto.setPageSize(null);
            dto.setPageNo(null);
        }
        return dto ;
    }

    public static DemoEntityQuery dtoToDemoQuery(DemoDTO demoDTO){
        DemoEntityQuery query = new DemoEntityQuery();
        query.setName(demoDTO.getName());
        query.setAge(demoDTO.getAge());
        query.setPhone(demoDTO.getPhone());
        query.setEmail(demoDTO.getEmail());
        query.setDelFlag(demoDTO.getDelFlag());
        query.setCreateUser(demoDTO.getCreateUser());
        query.setCreateName(demoDTO.getCreateName());
        query.setUpdateUser(demoDTO.getUpdateUser());
        query.setUpdateName(demoDTO.getUpdateName());
        query.setPageNo(demoDTO.getPageNo());
        query.setPageSize(demoDTO.getPageSize());
        return query ;
    }
}

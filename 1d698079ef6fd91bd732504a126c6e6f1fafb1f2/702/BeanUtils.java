package com.hlj.proj.utils;

import com.hlj.proj.common.page.PageDTO;
import com.hlj.proj.data.dao.mybatis.demo.query.DemoEntityQuery;
import com.hlj.proj.data.pojo.demo.DemoEntity;
import com.hlj.proj.dto.Demo.DemoDTO;
import org.springframework.data.domain.Page;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName BeanUtils
 * @date 2019/6/13  20:08.
 * @Description
 */
public class BeanUtils {


    public static<T> PageDTO<T> toPageDTO(Page<T> page){

            if(page==null||page.getContent()==null||page.getContent().size()==0){
                return new PageDTO(null);
            }
            return new PageDTO(page.getNumber(),page.getSize(),page.getSize(),page.getTotalPages(),page.getContent()) ;
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
        if(demoDTO.getPage()){
            query.setPageSize(demoDTO.getPageSize());
            query.setPageNo(demoDTO.getPageNo());
        }else {
            query.setPageSize(null);
            query.setPageNo(null);
        }
        return query ;
    }
}

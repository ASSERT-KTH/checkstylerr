package com.hlj.moudle.mappedBy.Service;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/4  下午2:07.
 * 类描述：
 */
public interface MappedByService {

    void noMappedBy();


    /**
     * 测试懒加载和急加载
     * @param departmentId
     * @param EmployeeId
     */
    void fetch(Integer departmentId, Integer EmployeeId);


}

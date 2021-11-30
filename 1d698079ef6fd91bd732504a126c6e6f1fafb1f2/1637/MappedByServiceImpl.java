package com.hlj.moudle.mappedBy.Service.impl;

import com.hlj.dao.db.DepartmentRepository;
import com.hlj.dao.db.EmployeeRepository;
import com.hlj.entity.db.demo.Department;
import com.hlj.entity.db.demo.Employee;
import com.hlj.moudle.mappedBy.Service.MappedByService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/4  下午2:07.
 * 类描述：
 */
@Service
public class MappedByServiceImpl implements MappedByService {


    @Resource
    private DepartmentRepository departmentRepository ;

    @Resource
    private EmployeeRepository employeeRepository ;

    @Override
    public void noMappedBy() {


        //创建两个员工对象
        Employee employee1 = new Employee();
        employee1.setEname("张三");
        employee1.setPhone("13111111111");
        Employee employee2 = new Employee();
        employee2.setEname("李四");
        employee2.setPhone("18523222222");


        //创建一个部门对象
        Department department = new Department();
        department.setDname("研发部");

        //设置对象关联
      department.getEmployeeList().add(employee1);
      department.getEmployeeList().add(employee2);

        employee1.setDepartment(department);
        employee2.setDepartment(department);

        //这个必须放在下面 两个employee1 employee2 保存前面，网络上说的使用的session控制的（也可以保存成功，但是会有多出一些update更新语句问题），
        // 这里直接是jpa控制的，不会出现上面的问题，必须先保存一的一方，再保存多的一方。如果先保存多的一方会报错，很明显外键不存在怎么保存
        departmentRepository.save(department);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);


    }

    @Override
    public void fetch(Integer departmentId, Integer EmployeeId) {

      Department department = departmentRepository.findOne(departmentId);

      Employee employee = employeeRepository.findOne(EmployeeId);

        System.out.println(employee);
    }
}

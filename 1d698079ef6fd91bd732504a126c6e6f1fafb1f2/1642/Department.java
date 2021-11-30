package com.hlj.entity.db.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/4  上午11:18.
 * 类描述：
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "department")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Department {

    @Id
    @GeneratedValue(generator="_native")
    @GenericGenerator(name="_native", strategy="native")
    private Integer id; //ID

    private String dname;


    @OneToMany(mappedBy = "department") //department 一对多
//  @JoinColumn(name="departmentId") //有了上面的这个就不能使用了，会冲突
    private List<Employee> employeeList = new ArrayList<>();


}

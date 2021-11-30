package com.hlj.entity.db.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/4  上午11:21.
 * 类描述：
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "employee")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employee {

    @Id
    @GeneratedValue(generator="_native")
    @GenericGenerator(name="_native", strategy="native")
    private Integer id ;

    @Column(length=20)
    private String ename; //员工姓名

    @Column(length=20)
    private String phone; //电话

    @ManyToOne //多对一
    @JoinColumn(name="departmentId")
    private Department department; //所属部门


}

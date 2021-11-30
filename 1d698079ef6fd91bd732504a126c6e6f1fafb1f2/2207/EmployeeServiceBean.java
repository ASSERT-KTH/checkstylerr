package cn.itcast.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.bean.Employee;
import cn.itcast.service.EmployeeService;

@Service @Transactional
public class EmployeeServiceBean implements EmployeeService{
	@Resource SessionFactory factory;

	public void delete(String... usernames) {
		for(String username : usernames){
			factory.getCurrentSession().delete(factory.getCurrentSession().load(Employee.class, username));
		}
	}

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public Employee find(String username) {
		return (Employee)factory.getCurrentSession().get(Employee.class, username);
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public List<Employee> list() {		
		return factory.getCurrentSession().createQuery("from Employee").list();
	}
    
	public void save(Employee employee) {
		factory.getCurrentSession().persist(employee);
	}

	public void update(Employee employee) {
		factory.getCurrentSession().merge(employee);
	}

}

package com.hlj.domain;

import java.util.HashSet;
import java.util.Set;

public class Department implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	//通过这个集合表明一个学生可以选择多门课程，反过来一门课程可以被多次选择
		private Set <StudentMany> studentManys;

	public Set<StudentMany> getStudentManys() {
			return studentManys;
		}
		public void setStudentManys(Set<StudentMany> studentManys) {
			this.studentManys = studentManys;
		}
	public Integer getId() {
		return id;
	} 
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
